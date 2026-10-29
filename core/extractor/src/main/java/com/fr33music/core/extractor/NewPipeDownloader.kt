package com.fr33music.core.extractor

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class NewPipeDownloader(
    val okHttpClient: OkHttpClient = defaultClient()
) : Downloader() {

    private val cookieStore = ConcurrentHashMap<String, String>()

    fun setCookie(key: String, value: String) {
        cookieStore[key] = value
    }

    fun getCookie(key: String): String? = cookieStore[key]

    fun getAllCookies(): Map<String, String> = cookieStore

    override fun execute(request: Request): Response {
        val url = request.url()
        val method = request.httpMethod()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        var body: RequestBody? = null
        if (dataToSend != null) {
            val contentType = headers["Content-Type"]?.firstOrNull()
            body = dataToSend.toRequestBody(contentType?.toMediaTypeOrNull())
        } else if (method.equals("POST", ignoreCase = true) || method.equals("PUT", ignoreCase = true)) {
            body = ByteArray(0).toRequestBody(null)
        }

        val requestBuilder = okhttp3.Request.Builder()
            .url(url)
            .method(method, body)

        for ((name, values) in headers) {
            for (value in values) {
                requestBuilder.addHeader(name, value)
            }
        }

        if (!requestBuilder.build().headers.names().contains("User-Agent")) {
            requestBuilder.addHeader("User-Agent", USER_AGENT)
        }

        if (cookieStore.isNotEmpty() && !requestBuilder.build().headers.names().contains("Cookie")) {
            val cookieHeader = cookieStore.entries.joinToString("; ") { "${it.key}=${it.value}" }
            requestBuilder.addHeader("Cookie", cookieHeader)
        }

        val call = okHttpClient.newCall(requestBuilder.build())
        val okResponse = call.execute()

        val responseBodyBytes = okResponse.body?.bytes() ?: ByteArray(0)
        val responseBodyString = responseBodyBytes.decodeToString()
        val responseHeaders = okResponse.headers.toMultimap()

        // Capture any set-cookie headers
        val setCookieHeaders = okResponse.headers("Set-Cookie")
        for (cookie in setCookieHeaders) {
            val parts = cookie.split(";")[0].split("=")
            if (parts.size >= 2) {
                setCookie(parts[0].trim(), parts[1].trim())
            }
        }

        return Response(
            okResponse.code,
            okResponse.message,
            responseHeaders,
            responseBodyString,
            okResponse.request.url.toString()
        )
    }

    companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        const val REFERER_YOUTUBE = "https://www.youtube.com/"

        fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }
}
