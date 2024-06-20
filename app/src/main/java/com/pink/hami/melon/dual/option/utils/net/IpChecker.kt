package com.pink.hami.melon.dual.option.utils.net

import android.content.Context
import com.pink.hami.melon.dual.option.utils.DualContext
import java.util.Locale

class IpChecker() {

    fun checkIp(): Boolean {
        val ipData = fetchIpData()
        return isIllegalIp(ipData)
    }

    private fun fetchIpData(): String {
        return DualContext.localStorage.ip_gsd.ifEmpty {
            DualContext.localStorage.ip_gsd_oth
        }
    }

    private fun isIllegalIp(ipData: String): Boolean {
        if (ipData.isEmpty()) {
            return checkLocaleForIllegalIp()
        }
        return ipData == "IR" || ipData == "CN" || ipData == "HK" || ipData == "MO"
    }

    private fun checkLocaleForIllegalIp(): Boolean {
        val locale = Locale.getDefault()
        val language = locale.language
        return language == "zh" || language == "fa"
    }
}
