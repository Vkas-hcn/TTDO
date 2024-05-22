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
        try {
            val url = URL("https://ifconfig.me/ip")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }

            DualContext.localStorage.ip_lo_dualLoad = content
        } catch (e: Exception) {
        }
    }

    suspend fun getLoadIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.infoip.io/")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country_short\":\"")[1].split("\"")[0]
            DualContext.localStorage.ip_gsd = data
        } catch (e: Exception) {
        }
    }

    suspend fun getLoadOthIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipinfo.io/json")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country\":\"")[1].split("\"")[0]
            DualContext.localStorage.ip_gsd_oth = data
        } catch (e: Exception) {
        }
    }

    suspend fun getBlackData(context: Context) = withContext(Dispatchers.IO) {
        val data = DualContext.localStorage.local_clock
        if (data.isEmpty()) {
            val params = blackData(context)
            try {
                smileNetManager.getMapRequest(
                    "https://withheld.writeonlinepennetproxy.com/donate/explain",
                    params,
                    object : DualOnlineFac.Callback {
                        override fun onSuccess(response: String) {
                            Log.e(TAG, "getBlackData-onSuccess: $response", )
                            DualContext.localStorage.local_clock = response
                        }

                        override fun onFailure(error: String) {
                            Log.e(TAG, "getBlackData-onFailure: $error", )
                            nextBlackFun(context)
                        }
                    })
            } catch (e: Exception) {
                nextBlackFun(context)
            }
        }
    }

    private fun nextBlackFun(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            getBlackData(context)
        }
    }


    @SuppressLint("HardwareIds")
    fun blackData(context: Context): Map<String, Any> {
        return mapOf<String, Any>(
            //bundle_id
            "strata" to ("com.writeonline.pennetproxy"),
            //os
            "slivery" to "ryder",
            //app_version
            "megaword" to (getAppVersion(context) ?: ""),
            //distinct_id
            "director" to DualContext.localStorage.uuid_dualLoadile,
            //client_ts
            "teammate" to (System.currentTimeMillis()),

            //device_model
            "hannah" to Build.MODEL,
            //os_version
            "act" to Build.VERSION.RELEASE,
            //gaid
            "receive" to DualContext.gidData,
            //android_id
            "twenty" to Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ),
        )
    }

    private fun getAppVersion(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun postPotIntData(context: Context, name: String, key: String? = null, time: Int = 0) {
        val data = if (key != null) {
            PutDataUtils.getTbaTimeDataJson(context, name, key, time)
        } else {
            PutDataUtils.getTbaDataJson(context, name)
        }
        Log.e(TAG, "postPotIntData--${name}: data=${data}")
        try {
            smileNetManager.postPutData(
                DualContext.put_data_url,
                data,
                object : DualOnlineFac.Callback {
                    override fun onSuccess(response: String) {
                        Log.e(TAG, "postPotIntData--${name}: onSuccess=${response}")
                    }

                    override fun onFailure(error: String) {
                        Log.e(TAG, "postPotIntData--${name}: onFailure=${error}")

                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "postPotIntData--${name}: Exception=${e}")
        }
    }


    fun getOnlineSmData(context: Context) {
        val timeStart = System.currentTimeMillis()
        postPotIntData(context, "blom1")
        smileNetManager.getServiceData(DualContext.put_dualLoad_service_data_url, {
            val data = DulaShowDataUtils.dropReversed(it)
            DualContext.localStorage.vpn_online_data_dualLoad = data
            Log.e(TAG, "getOnlineSmData: ${DualContext.localStorage.vpn_online_data_dualLoad}")
            val timeEnd = (System.currentTimeMillis() - timeStart) / 1000
            postPotIntData(context, "blom2t", "time", timeEnd.toInt())
            postPotIntData(context, "blom2")

        }, {
            Log.e(TAG, "getOnlineSmData---error=: ${it}")

        })
    }
}