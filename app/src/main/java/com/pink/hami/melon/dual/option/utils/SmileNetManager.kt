package com.pink.hami.melon.dual.option.utils
import android.content.Context
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
class SmileNetManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    interface Callback {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    fun getMapRequest(url: String, map: Map<String, Any>, callback: Callback) {
        val urlBuilder = url.toHttpUrl().newBuilder()
        map.forEach { entry ->
            urlBuilder.addEncodedQueryParameter(
                entry.key,
                URLEncoder.encode(entry.value.toString(), StandardCharsets.UTF_8.toString())
            )
        }
        val request = Request.Builder()
            .get()
            .tag(map)
            .url(urlBuilder.build())
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onFailure(responseBody.toString())
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Network error")
            }
        })
    }
    fun postPutData(url: String, body: Any, callback: Callback) {
        val requestBody =
            RequestBody.create("application/json".toMediaTypeOrNull(), body.toString())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .tag(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSuccess(responseBody)
                } else {
                    callback.onFailure(responseBody.toString())
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure("Network error")
            }
        })
    }

    fun getServiceData(context: Context,url: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .header("WZN", "ZZ")
            .header("AWA", "com.fast.open.ss.dual.agreement")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        onError("Error from server: ${response.code}")
                        return
                    }

                    val responseData = response.body?.string() ?: ""
                    onSuccess(responseData)
                } catch (e: Exception) {
                    onError("Error processing response: ${e.message}")
                } finally {
                    response.close()
                }
            }
        })
    }

}