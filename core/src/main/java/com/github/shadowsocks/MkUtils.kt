package com.github.shadowsocks

import android.content.Context
import android.util.Log
import android.net.VpnService
import android.text.format.Formatter
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.tencent.mmkv.MMKV

object MkUtils {
    private val mmkv by lazy {
        MMKV.mmkvWithID("smile", MMKV.MULTI_PROCESS_MODE)
    }

    private fun getFlowData(): Boolean {
        val data = mmkv.decodeBool("smile_arrow", true)
        Log.e("TAG", "getAroundFlowJsonData-ss: ${data}")
        return data
    }

    fun brand(builder: VpnService.Builder, myPackageName: String) {
        if(getFlowData()){
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
        )
    }


    fun getSpeedData(service: BaseService.Interface, stats: TrafficStats){
        val data = (service as Context).getString(
            com.github.shadowsocks.core.R.string.traffic,
            (service as Context).getString(
                com.github.shadowsocks.core.R.string.speed,
                Formatter.formatFileSize((service as Context), stats.txRate)
            ),
            (service as Context).getString(
                com.github.shadowsocks.core.R.string.speed,
                Formatter.formatFileSize((service as Context), stats.rxRate)
            )
        )
        val pattern = """([\d.]+)\s*([^\s]+)\s*([↑↓])\s*([\d.]+)\s*([^\s]+)\s*([↑↓])""".toRegex()
        val matches = pattern.find(data)
        if (matches != null) {
            val (value1, unit1, arrow1, value2, unit2, arrow2) = matches.destructured
            mmkv.encode("speed_dow", "$value1 $unit1")
            mmkv.encode("speed_up", "$value2 $unit2")

        }
    }
}