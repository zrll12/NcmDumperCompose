package cc.vastsea.zrll.ncmdumpercompose.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class HttpUtils {

    object NetworkClient {
        val client: OkHttpClient by lazy { OkHttpClient() }
    }

    suspend fun lyrics(musicId: String): String? = withContext(Dispatchers.IO) {
        val url = "https://music.163.com/api/song/media?id=$musicId"
        val request = Request.Builder().url(url).build()

        return@withContext kotlin.runCatching {
            NetworkClient.client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        }.getOrNull()
    }


}
