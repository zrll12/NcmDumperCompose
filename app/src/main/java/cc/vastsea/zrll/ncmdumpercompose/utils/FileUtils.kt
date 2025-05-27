package cc.vastsea.zrll.ncmdumpercompose.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.InputStream
import androidx.core.net.toUri
import cc.vastsea.zrll.ncmdumpercompose.model.NcmFile
import cc.vastsea.zrll.ncmdumpercompose.model.TaskState
import kotlinx.coroutines.flow.first
import java.io.FileInputStream

object FileUtils {
    fun fetchNcmFileUri(context: Context, inputDirUri: Uri, outputDir: Uri): Flow<List<NcmFile>> = flow {
        val resultList = mutableListOf<NcmFile>()
        val ncmSuffix = ".ncm"
        val contentResolver = context.contentResolver
        val folderDocumentId = DocumentsContract.getTreeDocumentId(inputDirUri)
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(inputDirUri, folderDocumentId)

        // 获取输出目录中的MP3文件列表
        val mp3Files = fetchMp3FileNames(context, outputDir)
            .first()
            .toSet()

        contentResolver.query(
            childrenUri, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED
            ), null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val documentId = cursor.getString(0)
                val mimeType = cursor.getString(1)
                val size = cursor.getLong(2)
                val fileName = cursor.getString(3) ?: "unknown.ncm"
                val lastModified = cursor.getLong(4)
                val documentUri =
                    DocumentsContract.buildDocumentUriUsingTree(inputDirUri, documentId)
                if (!mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR, ignoreCase = true) &&
                    fileName.endsWith(ncmSuffix, ignoreCase = true)
                ) {
                    val nameWithoutExt = fileName.removeSuffix(ncmSuffix)
                    val taskState = if (mp3Files.contains(nameWithoutExt)) TaskState.Dumped else TaskState.Wait
                    resultList.add(
                        NcmFile(
                            documentUri,
                            size,
                            nameWithoutExt,
                            lastModified,
                            taskState = taskState
                        )
                    )
                }
            }
        }
        emit(resultList.sortedByDescending { it.lastModified })
    }

    fun fetchMp3FileNames(context: Context, folder: Uri): Flow<List<String>> = flow {
        val resultList = mutableListOf<String>()
        val ncmSuffix = ".mp3"
        val contentResolver = context.contentResolver
        val folderDocumentId = DocumentsContract.getTreeDocumentId(folder)
        val childrenUri =
            DocumentsContract.buildChildDocumentsUriUsingTree(folder, folderDocumentId)

        contentResolver.query(
            childrenUri, arrayOf(
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME
            ), null, null, null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val mimeType = cursor.getString(0)
                val fileName = cursor.getString(1) ?: "unknown.mp3"
                if (!mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR, ignoreCase = true) &&
                    fileName.endsWith(ncmSuffix, ignoreCase = true)
                ) {
                    val nameWithoutExt = fileName.removeSuffix(ncmSuffix)
                    resultList.add(nameWithoutExt)
                }
            }
        }

        emit(resultList)
    }

    fun isValidNcmOrMp3File(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.isFile &&
                (file.name.endsWith(".ncm") || file.name.endsWith(".mp3"))
    }

    fun getFileInputStream(context: Context, uri: Uri): InputStream? {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        return pfd?.fileDescriptor?.let { FileInputStream(it) }
    }

    fun getImageMIMEType(byteArray: ByteArray): String? {
        return when {
            byteArray.size >= 4 -> {
                when {
                    byteArray[0] == 0xFF.toByte() && byteArray[1] == 0xD8.toByte() && byteArray[2] == 0xFF.toByte() -> "image/jpeg"
                    byteArray[0] == 0x89.toByte() && byteArray[1] == 0x50.toByte() &&
                            byteArray[2] == 0x4E.toByte() && byteArray[3] == 0x47.toByte() -> "image/png"

                    else -> null
                }
            }

            else -> null
        }
    }
}