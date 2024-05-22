package com.pink.hami.melon.dual.option.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object DualONlineFun {
    val smileNetManager = DualOnlineFac(App.getAppContext())

    suspend fun getOnlyIp() = withContext(Dispatchers.IO) {
        fetchIpFromUrl("https://ifconfig.me/ip") { content ->
            DualContext.localStorage.ip_lo_dualLoad = content
        }
    }

    suspend fun getLoadIp() = withContext(Dispatchers.IO) {
        fetchIpFromUrl("https://api.infoip.io/") { content ->
            val countryCode = extractCountryCode(content)
            DualContext.localStorage.ip_gsd = countryCode
        }
    }

    suspend fun getLoadOthIp() = withContext(Dispatchers.IO) {
        fetchIpFromUrl("https://ipinfo.io/json") { content ->
            val countryCode = extractCountryCodeFromJson(content)
            DualContext.localStorage.ip_gsd_oth = countryCode
        }
    }

    suspend fun getBlackData(context: Context) = withContext(Dispatchers.IO) {
        val localClock = DualContext.localStorage.local_clock
        if (localClock.isEmpty()) {
            val params = blackData(context)
            try {
                smileNetManager.getMapRequest(
                    "https://withheld.writeonlinepennetproxy.com/donate/explain",
                    params,
                    object : DualOnlineFac.Callback {
                        override fun onSuccess(response: String) {
                            Log.d(TAG, "BlackData-Success: $response", )
                            DualContext.localStorage.local_clock = response
                        }

                        override fun onFailure(error: String) {
                            Log.d(TAG, "BlackData-onFailure: $error", )

                            scheduleRetry(context)
                        }
                    })
            } catch (e: Exception) {
                scheduleRetry(context)
            }
        }
    }

    private fun scheduleRetry(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            getBlackData(context)
        }
    }

    @SuppressLint("HardwareIds")
    fun blackData(context: Context): Map<String, Any> {
        return mapOf(
            "strata" to "com.writeonline.pennetproxy",
            "slivery" to "ryder",
            "megaword" to getAppVersion(context).orEmpty(),
            "director" to DualContext.localStorage.uuid_dualLoadile,
            "teammate" to System.currentTimeMillis(),
            "hannah" to Build.MODEL,
            "act" to Build.VERSION.RELEASE,
            "receive" to DualContext.gidData,
            "twenty" to Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
    }

    private fun getAppVersion(context: Context): String? {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    private fun emitPointData(context: Context, name: String, key: String? = null, time: Int = 0) {
        val data = if (key != null) {
            PutDataUtils.getTbaTimeDataJson(context, name, key, time)
        } else {
            PutDataUtils.getTbaDataJson(context, name)
        }
        try {
            smileNetManager.postPutData(DualContext.put_data_url, data, object : DualOnlineFac.Callback {
                override fun onSuccess(response: String) {
                    Log.e(TAG, "emitPointData--$name: onSuccess=$response")
                }

                override fun onFailure(error: String) {
                    Log.e(TAG, "emitPointData--$name: onFailure=$error")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "emitPointData--$name: Exception=$e")
        }
    }

    fun landingRemoteData(context: Context) {
        val timeStart = System.currentTimeMillis()
        emitPointData(context, "blom1")
        smileNetManager.getServiceData(DualContext.put_dualLoad_service_data_url, {
            val data = DulaShowDataUtils.dropReversed(it)
            DualContext.localStorage.vpn_online_data_dualLoad = data
            val timeEnd = ((System.currentTimeMillis() - timeStart) / 1000).toInt()
            emitPointData(context, "blom2t", "time", timeEnd)
            emitPointData(context, "blom2")
        }, {
            Log.e(TAG, "landingRemoteData---error=: $it")
        })
    }

    private suspend fun fetchIpFromUrl(urlString: String, onContentFetched: (String) -> Unit) {
        try {
            val url = URL(urlString)
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            onContentFetched(content)
        } catch (e: Exception) {
            Log.e(TAG, "fetchIpFromUrl error: ${e.message}")
        }
    }

    private fun extractCountryCode(content: String): String {
        return content.split("\"country_short\":\"")[1].split("\"")[0]
    }

    private fun extractCountryCodeFromJson(content: String): String {
        return content.split("\"country\":\"")[1].split("\"")[0]
    }
}
