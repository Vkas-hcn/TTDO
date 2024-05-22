package com.pink.hami.melon.dual.option.utils
import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                            "${URLEncoder.encode(entry.value.toString(), StandardCharsets.UTF_8.toString())}&"
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
            { error -> callback.onFailure(error.toString()) }
        )

        requestQueue.add(request)
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
                headers["SER"] = "com.pink.hami.melon.dual.option"
                return headers
            }
        }

        requestQueue.add(request)
    }
}
