package com.pink.hami.melon.dual.option.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.pink.hami.melon.dual.option.BuildConfig
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.DualFFFFFFBean
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.utils.DualONlineFun.landingRemoteData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import java.io.StringReader
object DualContext {
    const val web_dualLoadile_url = "https://www.baidu.com/"
    val put_data_url: String
    val put_dualLoad_service_data_url: String

    const val isDualConnected = "isDualConnected"
    const val cuDualConnected = "cuDualConnected"

    val localStorage = LocalStorage(App.getAppContext())

    init {
        put_data_url = initializePutDataUrl()
        put_dualLoad_service_data_url = initializePutDualLoadServiceDataUrl()
    }

    private fun initializePutDataUrl(): String {
        return if (BuildConfig.DEBUG) {
            "https://test-dynamo.writeonlinepennetproxy.com/charley/conifer"
        } else {
            "https://dynamo.writeonlinepennetproxy.com/chisel/steamy/hothouse"
        }
    }

    private fun initializePutDualLoadServiceDataUrl(): String {
        return if (BuildConfig.DEBUG) {
            "https://test.writeonlinepennetproxy.com/yPVH/xkGjmHPRM/mUBXhRlAY/"
        } else {
            "https://api.writeonlinepennetproxy.com/yPVH/xkGjmHPRM/mUBXhRlAY/"
        }
    }

    private val gson = Gson()

    @Synchronized
    fun isHaveServeData(): Boolean {
        val allData = getAllVpnListData()
        return if (allData == null) {
            if (!fetchOnlineDataIfNecessary()) {
                // 如果 fetchOnlineDataIfNecessary() 失败，返回 false
                return false
            }
            false
        } else {
            try {
                val serviceData = gson.fromJson<VpnServiceBean>(
                    localStorage.check_service,
                    object : TypeToken<VpnServiceBean?>() {}.type
                )
                val skVpnServiceBean: VpnServiceBean = if (serviceData?.best_dualLoad == true) {
                    getFastVpn() ?: serviceData ?: VpnServiceBean()
                } else {
                    serviceData ?: VpnServiceBean()
                }
                localStorage.check_service = gson.toJson(skVpnServiceBean)
                true
            } catch (e: Exception) {
                // 处理异常，记录日志
                e.printStackTrace()
                false
            }
        }
    }

    private fun fetchOnlineDataIfNecessary(): Boolean {
        landingRemoteData()
        return true
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

    private fun getVpnList2(): MutableList<VpnServiceBean>? {
        return runCatching {
            val listType = object : TypeToken<DualFFFFFFBean>() {}.type
            Gson().fromJson<DualFFFFFFBean>(localStorage.vpn_online_data_dualLoad, listType)?.data?.server_list
        }.getOrNull()
    }

    fun getFastVpn2(): VpnServiceBean? {
        return runCatching {
            Gson().fromJson(localStorage.vpn_online_data_dualLoad, DualFFFFFFBean::class.java)?.data?.smart_list?.random()
        }.onSuccess { smartBean ->
            configureFastVpnBean(smartBean)
        }.getOrNull()
    }


    private fun parseVpnData(): DualFFFFFFBean? {
        val jsonString = localStorage.vpn_online_data_dualLoad
        if (jsonString.isEmpty()) {
            return null
        }
        return try {
            Gson().fromJson(jsonString, DualFFFFFFBean::class.java)
        } catch (e: JsonSyntaxException) {
            // 记录异常信息
            Log.e("VpnService", "Failed to parse JSON: ${e.message}")
            null
        }
    }

    private fun getVpnList(): MutableList<VpnServiceBean>? {
        return runCatching {
            parseVpnData()?.data?.server_list
        }.getOrNull()
    }

    fun getFastVpn(): VpnServiceBean? {
        return runCatching {
            val smartList = parseVpnData()?.data?.smart_list
            if (smartList.isNullOrEmpty()) {
                null
            } else {
                smartList.random()
            }
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
