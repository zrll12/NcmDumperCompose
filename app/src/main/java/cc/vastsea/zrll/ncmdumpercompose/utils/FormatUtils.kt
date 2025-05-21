package cc.vastsea.zrll.ncmdumpercompose.utils

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object FormatUtils {
    fun formatPath(path: String?): String {
        if (path.isNullOrEmpty()) return ""
        var formattedPath = path
        if (formattedPath.startsWith("content://com.android.externalstorage.documents/tree/primary%3A")) {
            formattedPath = formattedPath.replace("content://com.android.externalstorage.documents/tree/primary%3A", "")
        }
        return try {
            URLDecoder.decode(formattedPath, StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            path
        }
    }

    fun formatSize(size: Long): String {
        val kb = size / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        return when {
            gb > 0 -> String.format("%.2f GB", gb.toDouble())
            mb > 0 -> String.format("%.2f MB", mb.toDouble())
            kb > 0 -> String.format("%.2f KB", kb.toDouble())
            else -> "$size B"
        }
    }

    fun formatNcmName(name: String): Pair<String, String> {
        val parts = name.split(" - ")
        return parts[0] to parts[1]
    }
}