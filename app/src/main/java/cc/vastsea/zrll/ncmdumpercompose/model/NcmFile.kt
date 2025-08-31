package cc.vastsea.zrll.ncmdumpercompose.model

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import cc.vastsea.zrll.ncmdumpercompose.utils.AppUtils.aesDecrypt
import cc.vastsea.zrll.ncmdumpercompose.utils.AppUtils.toIntLE
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import kotlin.experimental.xor
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


data class NcmFile(
    val uri: Uri,
    val mp3Uri: Uri?,
    val size: Long,
    val name: String,
    val lastModified: Long,
    val checkState: Boolean = false,
    val taskState: TaskState
) {
    companion object {
        fun fromInputStream(inputStream: InputStream): NCMMetadata {
            return parseMetadata(inputStream)
        }

        fun NcmFile.toNcmMetadata(context: Context): NCMMetadata? {
            return context.contentResolver.openInputStream(uri)?.use { inputStream ->
                fromInputStream(inputStream)
            }
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun parseMetadata(inputStream: InputStream): NCMMetadata {
            val magicBytes = ByteArray(8)
            inputStream.read(magicBytes)
            check(String(magicBytes, Charsets.UTF_8) == "CTENFDAM") {
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
            return decodeMetadata(metadata)
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

        private val CORE_KEY = byteArrayOf(
            0x68, 0x7A, 0x48, 0x52, 0x41, 0x6D, 0x73, 0x6F,
            0x35, 0x6B, 0x49, 0x6E, 0x62, 0x61, 0x78, 0x57
        )
        private val MATA_KEY = byteArrayOf(
            0x23, 0x31, 0x34, 0x6C, 0x6A, 0x6B, 0x5F, 0x21,
            0x5C, 0x5D, 0x26, 0x30, 0x55, 0x3C, 0x27, 0x28
        )
    }
}

sealed class TaskState : Comparable<TaskState> {
    data object Wait : TaskState()
    data object Dumped : TaskState()

    override fun compareTo(other: TaskState): Int {
        if (this is Wait && other is Dumped) return -1
        if (this is Dumped && other is Wait) return 1
        return 0
    }

    @Composable
    fun getSelectedBackgroundColor(): Color {
        return when (this) {
            is Wait -> Color(0xFF4D6782) // Blue
            is Dumped -> Color(0xFF146F57) // Green
        }
    }

    @Composable
    fun getBackgroundColor(): Color {
        val isDarkTheme = isSystemInDarkTheme()
        return when (this) {
            is Wait -> if (isDarkTheme) Color(0xFF102136) else Color(0xFFE3F2FD) // Blue
            is Dumped -> if (isDarkTheme) Color(0xFF102E2F) else Color(0xFFC8E6C9) // Green
        }
    }
}