package cc.vastsea.zrll.ncmdumpercompose.utils

class RC4Utils {

    private val sBox = IntArray(256)

    fun initializeSBox(key: ByteArray) {
        sBox.indices.forEach { i -> sBox[i] = i }
        var j = 0
        for (i in 0 until 256) {
            j = (j + sBox[i] + key[i % key.size]) and 0xFF
            val swap = sBox[i]
            sBox[i] = sBox[j]
            sBox[j] = swap
        }
    }

    fun processData(data: ByteArray, length: Int) {
        for (i in 0 until length) {
            val j = (i + 1) and 0xFF
            data[i] =
                (data[i].toInt() xor sBox[(sBox[j] + sBox[(sBox[j] + j) and 0xFF]) and 0xFF]).toByte()
        }
    }
}