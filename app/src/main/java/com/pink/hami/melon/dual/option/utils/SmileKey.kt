package com.pink.hami.melon.dual.option.utils

import android.content.Context
import com.pink.hami.melon.dual.option.BuildConfig
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.OnlineBean
import com.pink.hami.melon.dual.option.bean.SmileFlowBean
import com.pink.hami.melon.dual.option.bean.SmileRefBean
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.utils.SmileNetHelp.getOnlineSmData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.bean.AdvertiseEntity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

object SmileKey {
    const val SHARE_URL = "https://play.google.com/store/apps/details?id="
    const val web_smile_url = "https://www.baidu.com/"
    var put_data_url = if (BuildConfig.DEBUG) {
        "https://test-solstice.bloomingunlimited.com/squalid/clown/grumman"
    } else {
        "https://solstice.bloomingunlimited.com/newborn/council/goblet"
    }
    var put_sm_service_data_url = if (BuildConfig.DEBUG) {
        "https://test.bloomingunlimited.com/LAfmqEZ/wQDmPyeovD/wXGpV/"
    } else {
        "https://api.bloomingunlimited.com/LAfmqEZ/wQDmPyeovD/wXGpV/"
    }
    const val isSmileConnected = "isSmileConnected"
    const val cuSmileConnected = "cuSmileConnected"

    const val POS_OPEN = "si_o"
    const val POS_HOME = "si_h"
    const val POS_CONNECT = "si_c"
    const val POS_BACK = "si_b"
    const val POS_RESULT = "si_r"

    const val vpn_data_type = "blo"
    const val fast_data_type = "blm"
    const val ad_data_type = "blu"
    const val user_data_type = "blg"
    const val lj_data_type = "bly"

    var gidData = ""
    private val sharedPreferences by lazy {
        App.getAppContext().getSharedPreferences(
            "smile_key",
            Context.MODE_PRIVATE
        )
    }

    var vpn_online_data = "vpn_online_data"
        set(value) {
            sharedPreferences.edit().run {
                putString("vpn_online_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("vpn_online_data", "").toString()
    var vpn_ip = "vpn_ip_sm"
        set(value) {
            sharedPreferences.edit().run {
                putString("vpn_ip_sm", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("vpn_ip_sm", "").toString()

    var vpn_city = "vpn_city_sm"
        set(value) {
            sharedPreferences.edit().run {
                putString("vpn_city_sm", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("vpn_city_sm", "").toString()
    var isInstall = "isInstall"
        set(value) {
            sharedPreferences.edit().run {
                putString("isInstall", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("isInstall", "").toString()

    var local_ad = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ad", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ad", "").toString()


    var local_ref_center = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ref_center", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ref_center", "").toString()
    var local_control = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_control", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_control", "").toString()
    var uuid_smile = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("uuid_smile", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("uuid_smile", "").toString()
    var check_service = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("check_service", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("check_service", "").toString()
    var connection_mode = "0"
        set(value) {
            sharedPreferences.edit().run {
                putString("connection_mode", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("connection_mode", "0").toString() ?: "0"
    var local_ref = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_ref", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_ref", "").toString()

    var local_clock = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("local_clock", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("local_clock", "").toString()
    var ip_lo_sm = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_lo_sm", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_lo_sm", "").toString()
    var ip_gsd = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_gsd", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_gsd", "").toString()
    var ip_gsd_oth = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("ip_gsd_oth", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("ip_gsd_oth", "").toString()

    var smile_arrow: Boolean = false
        set(value) {
            App.mmkvSmile.encode("smile_arrow", value)
            field = value
        }
        get() = App.mmkvSmile.decodeBool("smile_arrow", false)
    var adjust_smile = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("adjust_smile", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("adjust_smile", "").toString()

    var now_date = ""
        set(value) {
            sharedPreferences.edit().run {
                putString("now_date", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getString("now_date", "").toString()

    var ss_num_data = 0
        set(value) {
            sharedPreferences.edit().run {
                putInt("show_num_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("show_num_data", 0)

    var cc_num_data = 0
        set(value) {
            sharedPreferences.edit().run {
                putInt("cc_num_data", value)
                commit()
            }
            field = value
        }
        get() = sharedPreferences.getInt("show_num_data", 0)

    private fun decodeBase64(str: String): String {
        return String(android.util.Base64.decode(str, android.util.Base64.DEFAULT))
    }

    fun isHaveServeData(context: Context): Boolean {
        val allVpnListData = getAllVpnListData()
        if (allVpnListData == null) {
            getOnlineSmData(context)
            return false
        }
        return true
    }

    fun getAllVpnListData(): MutableList<VpnServiceBean>? {
        val list = mutableListOf<VpnServiceBean>()
        val listFastData = getFastVpn()
        val listVpnData = getVpnList()
        if (listFastData == null || listVpnData == null) {
            return null
        }
        list.add(listFastData)
        list.addAll(listVpnData)
        return list
    }

    private fun getVpnList(): MutableList<VpnServiceBean>? {
        val listType = object : TypeToken<OnlineBean>() {}.type
        val bean = runCatching {
            Gson().fromJson<OnlineBean>(
                vpn_online_data,
                listType
            )
        }.getOrNull()
        return bean?.data?.server_list
    }

    fun getFastVpn(): VpnServiceBean? {
        val bean = runCatching {
            Gson().fromJson(
                vpn_online_data,
                OnlineBean::class.java
            )
        }.getOrNull()
        val smartBean = bean?.data?.smart_list?.random()
        smartBean?.best_smart = true
        smartBean?.country_name = "Fast Server"
        return smartBean
    }

    fun getAdJson(): AdvertiseEntity {
        val dataJson = local_ad.let {
            if (it.isEmpty()) {
                SmileData.local_smile_ad_data
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<AdvertiseEntity>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_ad_data,
                object : TypeToken<AdvertiseEntity>() {}.type
            )
        }
    }

    fun getRefJson(): SmileRefBean {
        val dataJson = local_ref_center.let {
            if (it.isEmpty()) {
                SmileData.local_smile_data
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<SmileRefBean>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_data,
                object : TypeToken<SmileRefBean>() {}.type
            )
        }
    }

    fun getFlowJson(): SmileFlowBean {
        val dataJson = local_control.let {
            if (it.isEmpty()) {
                SmileData.local_smile_logic
            } else {
                decodeBase64(it)
            }
        }
        return try {
            Gson().fromJson(dataJson, object : TypeToken<SmileFlowBean>() {}.type)
        } catch (e: Exception) {
            e.printStackTrace()
            Gson().fromJson(
                SmileData.local_smile_logic,
                object : TypeToken<SmileFlowBean>() {}.type
            )
        }
    }

    fun recordNumberOfAdDisplaysGreen() {
        var showCount = ss_num_data
        showCount++
        ss_num_data = showCount
    }

    fun recordNumberOfAdClickGreen() {
        var clicksCount = cc_num_data
        clicksCount++
        cc_num_data = clicksCount
    }

    fun isAppGreenSameDayGreen() {
        if (now_date == "") {
            now_date = formatDateNow()
        } else {
            if (dateAfterDate(now_date, formatDateNow())) {
                now_date = formatDateNow()
                cc_num_data = 0
                ss_num_data = 0
            }
        }
    }
    fun isThresholdReached(): Boolean {
        return cc_num_data >= getAdJson().clickMax || ss_num_data >= getAdJson().showMax
    }
    private fun formatDateNow(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        return simpleDateFormat.format(date)
    }

    fun dateAfterDate(startTime: String?, endTime: String?): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd")
        try {
            val startDate: Date = format.parse(startTime)
            val endDate: Date = format.parse(endTime)
            val start: Long = startDate.getTime()
            val end: Long = endDate.getTime()
            if (end > start) {
                return true
            }
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }
        return false
    }
}