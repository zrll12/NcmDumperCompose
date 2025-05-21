package cc.vastsea.zrll.ncmdumpercompose.utils

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import cc.vastsea.zrll.ncmdumpercompose.model.MusicLyrics
import cc.vastsea.zrll.ncmdumpercompose.model.NCMMetadata
import cc.vastsea.zrll.ncmdumpercompose.utils.AppUtils.aesDecrypt
import cc.vastsea.zrll.ncmdumpercompose.utils.AppUtils.toIntLE
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object NcmUtils {
    private const val MAGIC_VALUE = "CTENFDAM"
    private val CORE_KEY = byteArrayOf(
        0x68,
        0x7A,
        0x48,
        0x52,
        0x41,
        0x6D,
        0x73,
        0x6F,
        0x35,
        0x6B,
        0x49,
        0x6E,
        0x62,
        0x61,
        0x78,
        0x57
    )
    private val MATA_KEY = byteArrayOf(
        0x23,
        0x31,
        0x34,
        0x6C,
        0x6A,
        0x6B,
        0x5F,
        0x21,
        0x5C,
        0x5D,
        0x26,
        0x30,
        0x55,
        0x3C,
        0x27,
        0x28
    )

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun dumpNCM(
        inputStream: InputStream,
        fileName: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        context: Context
    ) {
        kotlin.runCatching {
            val magicBytes = ByteArray(8)
            inputStream.read(magicBytes)
            check(String(magicBytes, Charsets.UTF_8) == MAGIC_VALUE) {
                "Invalid magic value"
            }
            inputStream.skip(2)
            val rc4KeyEncSizeBytes = ByteArray(4)
            inputStream.read(rc4KeyEncSizeBytes)
            val rc4KeyEncSize = rc4KeyEncSizeBytes.toIntLE()
            val rc4KeyEncBytes = ByteArray(rc4KeyEncSize)
            inputStream.read(rc4KeyEncBytes)
            for (i in rc4KeyEncBytes.indices) {
                rc4KeyEncBytes[i] = rc4KeyEncBytes[i] xor 0x64
            }
            val rc4Key = aesDecrypt(encryptedData = rc4KeyEncBytes, CORE_KEY)
            val metadataSizeBytes = ByteArray(4)
            inputStream.read(metadataSizeBytes)
            val metadataSize = metadataSizeBytes.toIntLE()
            val metadataBytes = ByteArray(metadataSize)
            inputStream.read(metadataBytes)
            for (i in metadataBytes.indices) {
                metadataBytes[i] = metadataBytes[i] xor 0x63
            }
            val metadata =
                aesDecrypt(Base64.decode(metadataBytes, 22, metadataBytes.size), MATA_KEY)
            val musicMetadata = (decodeMetadata(metadata))
            inputStream.skip(5)
            val artworkFrameBytes = ByteArray(4)
            inputStream.read(artworkFrameBytes)
            val artworkSizeBytes = ByteArray(4)
            inputStream.read(artworkSizeBytes)
            val artworkSize = artworkSizeBytes.toIntLE()
            val artwork = ByteArray(artworkSizeBytes.toIntLE())
            if (artworkSize > 0) {
                inputStream.read(artwork)
            }
            inputStream.skip(artworkFrameBytes.toIntLE() - artworkSize.toLong())
            saveAudioFile(inputStream, fileName, musicMetadata, rc4Key, artwork, context)
        }.onFailure {
            it.printStackTrace()
            inputStream.close()
            onFailure()
        }.onSuccess {
            onSuccess()
        }
    }

    private fun decodeMetadata(metadata: ByteArray): NCMMetadata {
        val withUnknownKeys = Json { ignoreUnknownKeys = true }
        return withUnknownKeys.decodeFromString<NCMMetadata>(
            String(
                metadata.copyOfRange(
                    6,
                    metadata.size
                ), Charsets.UTF_8
            )
        )
    }

    private fun decodeLyric(lyricResponse: String): MusicLyrics? {
        val withUnknownKeys = Json { ignoreUnknownKeys = true }
        if (lyricResponse.contains("nolyric"))
            return null
        return withUnknownKeys.decodeFromString<MusicLyrics>(
            lyricResponse
        )
    }

    private suspend fun saveAudioFile(
        inputStream: InputStream,
        fileName: String,
        musicMetadata: NCMMetadata,
        rc4Key: ByteArray,
        artworkByte: ByteArray,
        context: Context
    ) = withContext(
        Dispatchers.IO
    ) {
        val buffer = ByteArray(0x8000)
        var length: Int
        val rc4Utils = RC4Utils()
        rc4Utils.initializeSBox(rc4Key.copyOfRange(17, rc4Key.size))

        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/${musicMetadata.format}")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
            put(
                MediaStore.Audio.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_MUSIC}/NcmDumperCompose"
            )
        }


        val uri =
            context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let { songUri ->
            context.contentResolver.openOutputStream(songUri)?.use { outputStream ->
                while (inputStream.read(buffer).also { length = it } > 0) {
                    rc4Utils.processData(buffer, length)
                    outputStream.write(buffer, 0, length)
                }
            }
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                writeAudioTag(pfd, musicMetadata, artworkByte)
            }
            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            context.contentResolver.update(songUri, values, null, null)
        }
        inputStream.close()
    }

    private suspend fun writeAudioTag(
        pfd: ParcelFileDescriptor,
        musicMetadata: NCMMetadata,
        artworkByte: ByteArray
    ) {
        val lyrics = HttpUtils().lyrics(musicMetadata.musicId.toString())
        val decodeLyric = lyrics?.let { decodeLyric(it) }
        val metadata = TagLib.getMetadata(fd = pfd.dup().detachFd(), readPictures = false)!!
        val newMetadata = metadata.propertyMap.apply {
            this["TITLE"] = arrayOf(musicMetadata.musicName)
            this["ARTIST"] = arrayOf(musicMetadata.artist.joinToString(","))
            this["ALBUM"] = arrayOf(musicMetadata.album)
            this["ALBUMARTIST"] = arrayOf(musicMetadata.artist.joinToString(","))
            if (decodeLyric?.lyric != null)
                this["LYRICS"] = arrayOf(decodeLyric.lyric)
        }

        FileUtils.getImageMIMEType(artworkByte)?.apply {
            val artwork = Picture(
                data = artworkByte,
                description = "Front Cover",
                pictureType = "Front Cover",
                mimeType = this,
            )
            TagLib.savePictures(pfd.dup().detachFd(), arrayOf(artwork))
        }
        TagLib.savePropertyMap(pfd.dup().detachFd(), newMetadata)
    }
}