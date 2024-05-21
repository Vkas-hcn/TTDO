package de.blinkt.openvpn

import android.content.Context
import android.util.Log
import android.net.VpnService
import android.text.format.Formatter
import com.tencent.mmkv.MMKV

object MkUtils {
    private val mmkv by lazy {
        MMKV.mmkvWithID("smile", MMKV.MULTI_PROCESS_MODE)
    }

    private fun getFlowData(): Boolean {
        val data = mmkv.decodeBool("smile_arrow", true)
        Log.e("TAG", "getAroundFlowJsonData-oo: ${data}")
        return data
    }

    fun brand(builder: VpnService.Builder, myPackageName: String) {
        if (getFlowData()) {
            //黑名单绕流
            (listOf(myPackageName) + listGmsPackages())
                .iterator()
                .forEachRemaining {
                    runCatching { builder.addDisallowedApplication(it) }
                }
        }
    }

    private fun listGmsPackages(): List<String> {
        return listOf(
            "com.google.android.gms",
            "com.google.android.ext.services",
            "com.google.process.gservices",
            "com.android.vending",
            "com.google.android.gms.persistent",
            "com.google.android.cellbroadcastservice",
            "com.google.android.packageinstaller",
            "com.google.android.gms.location.history",
            "com.android.chrome",
        )
    }

    fun getSpeedData(upData: String, downData: String, statistics: String) {
        mmkv.encode("speed_dow", downData)
        mmkv.encode("speed_up", upData)
        mmkv.encode("statistics_data", statistics)
    }
}