package com.pink.hami.melon.dual.option.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.android.installreferrer.api.ReferrerDetails
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.pink.hami.melon.dual.option.BuildConfig
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.adload.AdBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Currency

object DualONlineFun {
    val smileNetManager = DualOnlineFac(App.getAppContext())

    suspend fun getOnlyIp() = withContext(Dispatchers.IO) {
        if(App.vpnLink){return@withContext}
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
                    "https://egypt.writeonlinepennetproxy.com/tao/veto/beta",
                    params,
                    object : DualOnlineFac.Callback {
                        override fun onSuccess(response: String) {
                            Log.d("TAG", "BlackData-Success: $response")
                            DualContext.localStorage.local_clock = response
                        }

                        override fun onFailure(error: String) {
                            Log.d("TAG", "BlackData-onFailure: $error")

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
            "panic" to "com.tunix.vpn.proxymaster.fastvpn.bestvpn.freevpn",
            "brain" to "perigee",
            "academic" to getAppVersion(context).orEmpty(),
            "terse" to System.currentTimeMillis(),
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

    fun emitPointData(
        name: String,
        key: String? = null,
        keyValue: Any? = null,
        key2: String? = null,
        keyValue2: Any? = null
    ) {
        val data = if (key != null) {
            PutDataUtils.getTbaTimeDataJson(name, key, keyValue, key2, keyValue2)
        } else {
            PutDataUtils.getTbaDataJson(name)
        }
        try {
            smileNetManager.postPutData(
                DualContext.put_data_url,
                data,
                object : DualOnlineFac.Callback {
                    override fun onSuccess(response: String) {
                        Log.e("TAG", "emitPointData--$name: onSuccess=$response")
                    }

                    override fun onFailure(error: String) {
                        Log.e("TAG", "emitPointData--$name: onFailure=$error")
                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "emitPointData--$name: Exception=$e")
        }
    }

    fun landingRemoteData() {
        val timeStart = System.currentTimeMillis()
        emitPointData("blom1")
        smileNetManager.getServiceData(DualContext.put_dualLoad_service_data_url, {
            val data = DulaShowDataUtils.dropReversed(it)
            DualContext.localStorage.vpn_online_data_dualLoad = data
            Log.e(
                "TAG",
                "landingRemoteData: success->$${DualContext.localStorage.vpn_online_data_dualLoad}"
            )
            val timeEnd = ((System.currentTimeMillis() - timeStart) / 1000).toInt()
            emitPointData("blom2t", "time", timeEnd)
            emitPointData("blom2")
        }, {
            Log.e("TAG", "landingRemoteData---error=: $it")
        })
    }

    private suspend fun fetchIpFromUrl(urlString: String, onContentFetched: (String) -> Unit) {
        try {
            val url = URL(urlString)
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            onContentFetched(content)
        } catch (e: Exception) {
            Log.e("TAG", "fetchIpFromUrl error: ${e.message}")
        }
    }

    private fun extractCountryCode(content: String): String {
        return content.split("\"country_short\":\"")[1].split("\"")[0]
    }

    private fun extractCountryCodeFromJson(content: String): String {
        return content.split("\"country\":\"")[1].split("\"")[0]
    }


    fun emitInstallData(context: Context, referrerDetails: ReferrerDetails) {
        if (DualContext.localStorage.up_install_thing) {
            return
        }
        val json = PutDataUtils.install(context, referrerDetails)
        Log.e("TBA", "json-install--->${json}")
        try {
            smileNetManager.postPutData(
                DualContext.put_data_url,
                json,
                object : DualOnlineFac.Callback {
                    override fun onSuccess(response: String) {
                        Log.e("TAG", "install事件上报-成功->")
                        DualContext.localStorage.up_install_thing = true
                    }

                    override fun onFailure(error: String) {
                        Log.e("TAG", "install事件上报-失败=$error")
                        DualContext.localStorage.up_install_thing = false

                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "install事件上报-失败=$e")

        }
    }

    fun emitSessionData() {
        val json = PutDataUtils.getSessionJson()
        Log.e("TBA", "json-getSessionJson--->${json}")
        try {
            smileNetManager.postPutData(
                DualContext.put_data_url,
                json,
                object : DualOnlineFac.Callback {
                    override fun onSuccess(response: String) {
                        Log.e("TAG", "Session事件上报-成功->")
                    }

                    override fun onFailure(error: String) {
                        Log.e("TAG", "Session事件上报-失败=$error")

                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "Session事件上报-失败=$e")

        }
    }

    fun emitAdData(
        adValue: AdValue,
        responseInfo: ResponseInfo,
        adBean: AdBean?,
        adType: String,
        adKey: String,
    ) {
        val json = PutDataUtils.getAdJson(adValue, responseInfo, adBean, adType, adKey)
        Log.e("TBA", "json-${adKey}-getADJson--->${json}")
        try {
            smileNetManager.postPutData(
                DualContext.put_data_url,
                json,
                object : DualOnlineFac.Callback {
                    override fun onSuccess(response: String) {
                        Log.e("TAG", "${adKey}-广告事件上报-成功->${response}")
                    }

                    override fun onFailure(error: String) {
                        Log.e("TAG", "${adKey}-广告事件上报-失败=$error")

                    }
                })
        } catch (e: Exception) {
            Log.e("TAG", "S${adKey}-广告事件上报-失败=$e")
        }
    }


    fun beforeLoadLinkSettingsTTD(ufDetailBean: AdBean?): AdBean? {
        var data = false
        if (App.vpnLink) {
            ufDetailBean?.ttd_load_ip = DualContext.localStorage.vpn_ip_dualLoad
            ufDetailBean?.ttd_load_city = DualContext.localStorage.vpn_city
        } else {
            data = true
        }
        if (data) {
            ufDetailBean?.ttd_load_ip = DualContext.localStorage.ip_lo_dualLoad
            ufDetailBean?.ttd_load_city = "null"
        }
        return ufDetailBean
    }


    fun afterLoadLinkSettingsTTD(ufDetailBean: AdBean?): AdBean? {
        var data = false
        if (App.vpnLink) {
            ufDetailBean?.ttd_show_ip = DualContext.localStorage.vpn_ip_dualLoad
            ufDetailBean?.ttd_show_city = DualContext.localStorage.vpn_city
        } else {
            data = true
        }
        if (data) {
            ufDetailBean?.ttd_show_ip = DualContext.localStorage.ip_lo_dualLoad
            ufDetailBean?.ttd_show_city = "null"
        }
        return ufDetailBean
    }

    fun toBuriedPointAdValueTTD(
        adValue: AdValue,
        responseInfo: ResponseInfo,
    ) {
        val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
        adRevenue.setRevenue(
            adValue.valueMicros / 1000000.0,
            adValue.currencyCode
        )
        adRevenue.setAdRevenueNetwork(responseInfo.mediationAdapterClassName)
        Adjust.trackAdRevenue(adRevenue)
        if (!BuildConfig.DEBUG) {
            AppEventsLogger.newLogger(App.getAppContext()).logPurchase(
                (adValue.valueMicros / 1000000.0).toBigDecimal(), Currency.getInstance("USD")
            )
        } else {
            Log.d("TBA", "purchase打点--value=${adValue.valueMicros}")
        }
    }
}
