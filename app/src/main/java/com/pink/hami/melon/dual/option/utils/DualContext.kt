package com.pink.hami.melon.dual.option.utils

import android.annotation.SuppressLint
import android.content.Context
import com.pink.hami.melon.dual.option.BuildConfig
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.DualFFFFFFBean
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.utils.DualONlineFun.landingRemoteData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DualContext {
    const val web_dualLoadile_url = "https://www.baidu.com/"
    val put_data_url: String
    val put_dualLoad_service_data_url: String

    const val isDualConnected = "isDualConnected"
    const val cuDualConnected = "cuDualConnected"

    var gidData = ""
    @SuppressLint("StaticFieldLeak")
    val localStorage = LocalStorage(App.getAppContext())

    init {
        put_data_url = initializePutDataUrl()
        put_dualLoad_service_data_url = initializePutDualLoadServiceDataUrl()
    }

    private fun initializePutDataUrl(): String {
        return if (BuildConfig.DEBUG) {
            "https://test-hydrant.writeonlinepennetproxy.com/crupper/bravado/cagey"
        } else {
            "https://hydrant.writeonlinepennetproxy.com/tuff/senile"
        }
    }

    private fun initializePutDualLoadServiceDataUrl(): String {
        return if (BuildConfig.DEBUG) {
            "https://test.writeonlinepennetproxy.com/yPVH/xkGjmHPRM/mUBXhRlAY/"
        } else {
            "https://api.writeonlinepennetproxy.com/yPVH/xkGjmHPRM/mUBXhRlAY/"
        }
    }

   suspend fun isHaveServeData(context: Context): Boolean {
        return if (getAllVpnListData() == null) {
            fetchOnlineDataIfNecessary(context)
            false
        } else {
            val serviceData = Gson().fromJson<VpnServiceBean>(
                localStorage.check_service,
                object : TypeToken<VpnServiceBean?>() {}.type
            )
            val skVpnServiceBean: VpnServiceBean = if (serviceData.best_dualLoad) {
                getFastVpn() ?: VpnServiceBean()
            } else {
                serviceData
            }
            localStorage.check_service = Gson().toJson(skVpnServiceBean)
            true
        }
    }

    private fun fetchOnlineDataIfNecessary(context: Context) {
        landingRemoteData(context)
    }

    fun getAllVpnListData(): MutableList<VpnServiceBean>? {
        val vpnList = mutableListOf<VpnServiceBean>()
        val fastVpn = getFastVpn()
        val vpnListData = getVpnList()
        return if (fastVpn == null || vpnListData == null) {
            null
        } else {
            populateVpnList(vpnList, fastVpn, vpnListData)
            vpnList
        }
    }

    private fun populateVpnList(
        vpnList: MutableList<VpnServiceBean>,
        fastVpn: VpnServiceBean,
        vpnListData: List<VpnServiceBean>
    ) {
        vpnList.add(fastVpn)
        vpnList.addAll(vpnListData)
    }

    private fun getVpnList(): MutableList<VpnServiceBean>? {
        return runCatching {
            val listType = object : TypeToken<DualFFFFFFBean>() {}.type
            Gson().fromJson<DualFFFFFFBean>(localStorage.vpn_online_data_dualLoad, listType)?.data?.server_list
        }.getOrNull()
    }

    fun getFastVpn(): VpnServiceBean? {
        return runCatching {
            Gson().fromJson(localStorage.vpn_online_data_dualLoad, DualFFFFFFBean::class.java)?.data?.smart_list?.random()
        }.onSuccess { smartBean ->
            configureFastVpnBean(smartBean)
        }.getOrNull()
    }

    private fun configureFastVpnBean(smartBean: VpnServiceBean?) {
        smartBean?.apply {
            best_dualLoad = true
            country_name = "Fast Server"
        }
    }
}
