package com.pink.hami.melon.dual.option.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

object PutDataUtils {
    private fun firstJsonData(context: Context): JSONObject {
        val jsonData = JSONObject()
        val citron = JSONObject().apply {
            // operator
            put("theist", getNetworkInfo(context))
        }

        val caruso = JSONObject().apply {
            // os_version
            put("act", Build.VERSION.RELEASE)
            // system_language
            put("drought", "${Locale.getDefault().language}_${Locale.getDefault().country}")
            // bundle_id
            put("strata", context.packageName)
        }

        val fusion = JSONObject().apply {
            // android_id
            put("twenty", Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID))
            // device_model
            put("hannah", Build.MODEL)
            // client_ts
            put("teammate", System.currentTimeMillis())
            // gaid
            put("receive", DualContext.gidData)
            // manufacturer
            put("strafe", Build.MODEL)
        }

        val dangle = JSONObject().apply {
            // log_id
            put("balloon", UUID.randomUUID().toString())
            // app_version
            put("megaword", getAppVersion(context))
            // os
            put("slivery", "ryder")
            // distinct_id
            put("director", DualContext.localStorage.uuid_dualLoadile)
        }

        jsonData.put("citron", citron)
        jsonData.put("caruso", caruso)
        jsonData.put("fusion", fusion)
        jsonData.put("dangle", dangle)

        return jsonData
    }



    fun getTbaDataJson(context: Context, name: String): String {
        return firstJsonData(context).apply {
            put("chestnut", name)
        }.toString()
    }

    fun getTbaTimeDataJson(
        context: Context,
        name: String,
        parameterName: String,
        time: Any,

    ): String {
        val data = JSONObject()
        data.put(parameterName, time)
        return firstJsonData(context).apply {
            put("chestnut", name)
            put("gloria", JSONObject().apply {
                put(parameterName, time)
            })
        }.toString()
    }

    private fun getAppVersion(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return "Version information not available"
    }


    private fun getNetworkInfo(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrierName = telephonyManager.networkOperatorName
        val networkOperator = telephonyManager.networkOperator
        val mcc = if (networkOperator.length >= 3) networkOperator.substring(0, 3) else ""
        val mnc = if (networkOperator.length >= 5) networkOperator.substring(3) else ""

        return """
        Carrier Name: $carrierName
        MCC: $mcc
        MNC: $mnc
    """.trimIndent()
    }

}