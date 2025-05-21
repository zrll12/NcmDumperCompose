package cc.vastsea.zrll.ncmdumpercompose.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AppUtils {

    private fun ByteArray.toLongLE(): Long {
        var result = 0L
        for (i in indices) {
            result = result or ((this[i].toLong() and 0xFF) shl (i * 8))
        }
        return result
    }

    fun ByteArray.toIntLE(): Int {
        return this.toLongLE().toInt()
    }

    @SuppressLint("GetInstance")
    fun aesDecrypt(encryptedData: ByteArray, key: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(encryptedData)
    }

    fun Long.sizeIn(): String {
        return when {
            this < 1000 -> "%d B".format(this)
            this < 1000 * 1000 -> "%d KB".format(this / 1024)
            this < 1000 * 1000 * 1000 -> "%d MB".format(this / (1024 * 1024))
            else -> "%.2f GB".format(this / (1024.0 * 1024 * 1024))
        }
    }

    fun <T> getItemShape(
        prevItem: T?,
        nextItem: T?,
        corner: Dp,
        subCorner: Dp
    ): Shape {
        return when {
            prevItem != null && nextItem != null -> RoundedCornerShape(subCorner)
            prevItem == null && nextItem == null -> RoundedCornerShape(corner)
            prevItem == null -> RoundedCornerShape(
                topStart = corner, topEnd = corner,
                bottomStart = subCorner, bottomEnd = subCorner
            )

            else -> RoundedCornerShape(
                topStart = subCorner, topEnd = subCorner,
                bottomStart = corner, bottomEnd = corner
            )
        }
    }

}