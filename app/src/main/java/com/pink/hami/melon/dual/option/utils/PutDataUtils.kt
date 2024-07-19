package com.pink.hami.melon.dual.option.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebSettings
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.adload.AdBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.UUID

object PutDataUtils {
    private fun firstJsonData(
        isAd: Boolean = false,
        adBean: AdBean? = null
    ): JSONObject {
        return JSONObject().apply {
            if (isAd) {
                val bubbleLoadCity = adBean?.ttd_load_city ?: "null"
                val bubbleShowCity = adBean?.ttd_show_city ?: "null"
                put("chicken", JSONObject().apply {
                    put("gal", bubbleLoadCity)
                    put("dfr", bubbleShowCity)
                })
            }
            put("musty", JSONObject().apply {
                //client_ts
                put("terse", System.currentTimeMillis())

                //os_version
                put("walter", "1")
                //ip
                put("frisian", DualContext.localStorage.ip_lo_dualLoad)
                //os
                put("brain", "perigee")
                put("cause", "mmc")
            })
            put("care", JSONObject().apply {
                //os_country
                put("rajah", Locale.getDefault().country)
                //log_id
                put("ripple", UUID.randomUUID().toString())
                put("ginger", "xiaomi")
                put("johnson", "1")

            })
            put("theyve", JSONObject().apply {
                //app_version
                put("academic", getAppVersion())
                //android_id
                put("be", "1")
                //system_language
                put("hunk", "${Locale.getDefault().language}_${Locale.getDefault().country}")
            })

            put("befuddle", JSONObject().apply {
                //bundle_id
                put("panic", App.getAppContext().packageName)
                //distinct_id
                put("tuft", DualContext.localStorage.android_id_data)
            })
        }
    }


    fun getTbaDataJson(name: String): String {
        return firstJsonData().apply {
            put("chestnut", name)
        }.toString()
    }

    fun getTbaTimeDataJson(
        name: String,
        parameterName: String,
        parameterValue: Any?,
        parameterName2: String?,
        parameterValue2: Any?,
    ): String {
        val data = JSONObject()
        data.put(parameterName, parameterValue)
        if (parameterName2 != null) {
            data.put(parameterName2, parameterValue2)
        }
        return firstJsonData().apply {
            put("chestnut", name)
            put("gloria", JSONObject().apply {
                put(parameterName, parameterValue)
                if (parameterName2 != null) {
                    put(parameterName2, parameterValue2)
                }
            })
        }.toString()
    }

    fun getSessionJson(): String {
        return firstJsonData().apply {
            put("chestnut", "embolden")
        }.toString()
    }

    fun install(context: Context, referrerDetails: ReferrerDetails): String {
        return firstJsonData().apply {
            put("penguin", JSONObject().apply {
                //build
                put("hereto", "build/${Build.ID}")

                //referrer_url
                put("catholic", referrerDetails.installReferrer)

                //install_version
                put("juneau", referrerDetails.installVersion)

                //user_agent
                put("trout", getWebDefaultUserAgent(context))

                //lat
                put("descant", getLimitTracking(context))

                //referrer_click_timestamp_seconds
                put("scad", referrerDetails.referrerClickTimestampSeconds)

                //install_begin_timestamp_seconds
                put("memory", referrerDetails.installBeginTimestampSeconds)

                //referrer_click_timestamp_server_seconds
                put("deem", referrerDetails.referrerClickTimestampServerSeconds)

                //install_begin_timestamp_server_seconds
                put("slide", referrerDetails.installBeginTimestampServerSeconds)

                //install_first_seconds
                put("maltose", getFirstInstallTime(context))

                //last_update_seconds
                put("elution", getLastUpdateTime(context))

                //google_play_instant
                put("hillman", referrerDetails.googlePlayInstantParam)
            })

        }.toString()
    }

    fun getAdJson(
        adValue: AdValue,
        responseInfo: ResponseInfo,
        adBean: AdBean?,
        adType: String,
        adKey: String
    ): String {

        return firstJsonData(true, adBean).apply {
            put("trawl", JSONObject().apply {
                //ad_pre_ecpm
                put("wild", adValue.valueMicros)
                //currency
                put("keyhole", adValue.currencyCode)
                //ad_network
                put(
                    "mattson",
                    responseInfo.mediationAdapterClassName
                )
                //ad_source
                put("windward", "admob")
                //ad_code_id
                put("dean", adBean?.netw)
                //ad_pos_id
                put("nh", adKey)
                //ad_rit_id
                put("mimosa", null)
                //ad_sense
                put("farewell", null)
                //ad_format
                put("herdsmen", adType)
                //precision_type
                put("fudge", getPrecisionType(adValue.precisionType))
                //ad_load_ip
                put("woven", adBean?.ttd_load_ip ?: "")
                //ad_impression_ip
                put("conform", adBean?.ttd_show_ip ?: "")
                //ad_sdk_ver
                put("custer", responseInfo.responseId)
            })

        }.toString()
    }

    private fun getAppVersion(): String {
        try {
            val packageInfo = App.getAppContext().packageManager.getPackageInfo(
                App.getAppContext().packageName,
                0
            )

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Version information not available"
    }

    private fun getLimitTracking(context: Context): String {
        return try {
            if (AdvertisingIdClient.getAdvertisingIdInfo(context).isLimitAdTrackingEnabled) {
                "columbus"
            } else {
                "mutilate"
            }
        } catch (e: Exception) {
            "mutilate"
        }
    }

    private fun getWebDefaultUserAgent(context: Context): String {
        return try {
            WebSettings.getDefaultUserAgent(context)
        } catch (e: Exception) {
            ""
        }
    }

    private fun getFirstInstallTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.firstInstallTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getLastUpdateTime(context: Context): Long {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.lastUpdateTime / 1000
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    private fun getPrecisionType(precisionType: Int): String {
        return when (precisionType) {
            0 -> {
                "UNKNOWN"
            }

            1 -> {
                "ESTIMATED"
            }

            2 -> {
                "PUBLISHER_PROVIDED"
            }

            3 -> {
                "PRECISE"
            }

            else -> {
                "UNKNOWN"
            }
        }
    }

    fun isNetworkReachable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            process.destroy()

            val result = output.toString()
            val state = result.contains("1 packets transmitted, 1 received")
            Log.e("TAG", "isNetworkReachable: ${state}")
            return state
        } catch (e: Exception) {
            Log.e("TAG", "isNetworkReachable: ----fasle")

            false // 发生异常，连接失败
        }
    }

    fun v10proxy() {
        GlobalScope.launch(Dispatchers.IO) {
            Log.e("TAG", "v10proxy: 开始检测")
            val netState = isNetworkReachable()
            val isHaveData = if (netState) {
                Log.e("TAG", "v10proxy: 开始检测-1")
                "1"
            } else {
                Log.e("TAG", "v10proxy: 开始检测-2")
                "2"
            }
            DualONlineFun.emitPointData(
                "v10proxy",
                "gg",
                App.adManagerConnect.isHaveAdData().toString(),
                "hh",
                isHaveData
            )
        }
    }

    fun v12proxy(isConnect: Boolean) {
        val text = if (isConnect) {
            "cont"
        } else {
            "dis"
        }
        DualONlineFun.emitPointData(
            "v12proxy", "gg",
            App.adManagerConnect.isHaveAdData().toString(),
            "hh",
            text
        )
    }


    fun v14proxy(adBean: AdBean) {
        DualONlineFun.emitPointData(
            "v14proxy",
            "gg",
            "${adBean.netu}+${App.top_activity_name}",
            "hh",
            App.vpnLink.toString()
        )
        if (App.vpnLink && !DualContext.localStorage.online_control_bean_core) {
            DualONlineFun.emitPointData(
                "v22proxy",
                "gg",
                adBean.netu,
            )
        }
        if (App.vpnLink) {
            DualONlineFun.emitPointData(
                "v23proxy",
                "gg",
                adBean.netu,
            )
        }

    }

    fun v15proxy(adBean: AdBean) {
        DualONlineFun.emitPointData(
            "v15proxy",
            "gg",
            "${adBean.netu}+${App.top_activity_name}",
        )
    }

    fun v17proxy(adBean: AdBean, errorString: String) {
        DualONlineFun.emitPointData(
            "v17proxy",
            "gg",
            "${adBean.netu}+${errorString}",
        )
    }
}