package com.pink.hami.melon.dual.option.utils

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.pink.hami.melon.dual.option.app.App
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Base64
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.toolbox.HttpHeaderParser

class DualOnlineFac(private val context: Context) {
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    interface Callback {
        fun onSuccess(response: String)
        fun onFailure(error: String)
    }

    fun getMapRequest(url: String, map: Map<String, Any>, callback: Callback) {
        val urlBuilder = StringBuilder(url)
        if (map.isNotEmpty()) {
            urlBuilder.append("?")
            map.forEach { entry ->
                urlBuilder.append(
                    "${URLEncoder.encode(entry.key, StandardCharsets.UTF_8.toString())}=" +
                            "${
                                URLEncoder.encode(
                                    entry.value.toString(),
                                    StandardCharsets.UTF_8.toString()
                                )
                            }&"
                )
            }
            urlBuilder.setLength(urlBuilder.length - 1)  // Remove the last '&'
        }
        val request = StringRequest(
            Request.Method.GET,
            urlBuilder.toString(),
            { response -> callback.onSuccess(response) },
            { error -> callback.onFailure(error.toString()) }
        )

        requestQueue.add(request)
    }

    fun postPutData(url: String, body: Any, callback: Callback) {
        val jsonBody = JSONObject(body.toString())
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response -> callback.onSuccess(response.toString()) },
            { error ->
                if (error is TimeoutError) {
                    // Handle timeout error specifically
                    callback.onFailure("Request timed out. Please try again later.")
                } else {
                    callback.onFailure(error.toString())
                }
            }

        )

        // Set a custom retry policy (5 seconds timeout, 2 retries, exponential backoff multiplier 2.0)
        request.retryPolicy = DefaultRetryPolicy(
            5000, // timeout in milliseconds
            2, // number of retries
            2.0f // backoff multiplier
        )

        requestQueue.add(request)
    }

    fun postAdminData(context: Context, url: String, body: Any, callback: Callback) {
        val jsonBodyString = JSONObject(body.toString()).toString()
        val timestamp = System.currentTimeMillis().toString()
        val xorEncryptedString = xorWithTimestamp(jsonBodyString, timestamp)
        val base64EncodedString = Base64.encodeToString(
            xorEncryptedString.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )
         var responseHeaders: Map<String, String> = emptyMap()
        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    val timestampResponse = responseHeaders["timestamp"] ?: throw IllegalArgumentException("Timestamp missing in headers")
                    val decodedBytes = Base64.decode(response.toString(), Base64.DEFAULT)
                    val decodedString = String(decodedBytes, Charsets.UTF_8)
                    val finalData = xorWithTimestamp(decodedString, timestampResponse)
                    val jsonResponse = JSONObject(finalData)
                    callback.onSuccess(jsonResponse.toString())
                } catch (e: Exception) {
                    callback.onFailure("Decryption failed: ${e.message}")
                }
            },
            { error ->
                if (error is TimeoutError) {
                    callback.onFailure("Request timed out. Please try again later.")
                } else {
                    callback.onFailure(error.toString())
                }
            }
        ) {

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                responseHeaders = response.headers!! // 保存 headers
                val parsed = String(response.data, Charsets.UTF_8) // 将响应数据解析为字符串
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["timestamp"] = timestamp  // 将时间戳放入 header
                return headers
            }

            override fun getBody(): ByteArray {
                return base64EncodedString.toByteArray(StandardCharsets.UTF_8)
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            5000, // timeout in milliseconds
            2, // number of retries
            2.0f // backoff multiplier
        )
        Volley.newRequestQueue(context).add(request)
    }
    private fun xorWithTimestamp(text: String, timestamp: String): String {
        val cycleKey = timestamp.toCharArray()
        val keyLength = cycleKey.size
        return text.mapIndexed { index, char ->
            char.toInt().xor(cycleKey[index % keyLength].toInt()).toChar()
        }.joinToString("")
    }


    fun getServiceData(
        url: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = object : StringRequest(
            Method.GET,
            url,
            Response.Listener { response -> onSuccess(response) },
            Response.ErrorListener { error -> onError("Network error: ${error.message}") }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["ZTM"] = "ZZ"
                headers["SER"] = App.getAppContext().packageName
                return headers
            }
        }
        request.retryPolicy = DefaultRetryPolicy(
            5000, // timeout in milliseconds
            2, // number of retries
            2.0f // backoff multiplier
        )
        requestQueue.add(request)
    }
}
