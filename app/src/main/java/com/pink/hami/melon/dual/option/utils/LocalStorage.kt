package com.pink.hami.melon.dual.option.utils

import android.content.Context
import androidx.annotation.Keep
import java.io.File
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.FileReader
import java.io.FileWriter

@Keep
data class AppData(
    var vpn_online_data_dualLoad: String = "",
    var uuid_dualLoadile: String = "",
    var check_service: String = "",

    var vpn_ip_dualLoad: String = "",
    var vpn_city: String = "",
    var connection_mode: String = "",
    var local_clock: String = "",
    var ip_lo_dualLoad: String = "",
    var ip_gsd: String = "",
    var ip_gsd_oth: String = "",
    var gidData: String = "",

    var dual_sp_dow: String = "",
    var dual_sp_up: String = "",

    var net1:Int = 0,
    var net2:Int = 0,
    var ad_date_app:Long = 0,
    var online_ad_key: String = "",
    var online_ref_key: String = "",
    var online_control_key: String = "",
    var online_control_key_core: Boolean = false,
    var ref_data: String = "",
    )

class LocalStorage(private val context: Context) {
    private val fileStorageManager = FileStorageManager(context)
    private val gson: Gson = GsonBuilder().create()
    private var appData: AppData


    init {
        val dataString = fileStorageManager.loadData()
        appData = if (dataString != null) {
            gson.fromJson(dataString, AppData::class.java)
        } else {
            AppData()
        }
    }

    var vpn_online_data_dualLoad: String
        get() = appData.vpn_online_data_dualLoad
        set(value) {
            appData.vpn_online_data_dualLoad = value
            writeToFile(appData)
        }

    var uuid_dualLoadile: String
        get() = appData.uuid_dualLoadile
        set(value) {
            appData.uuid_dualLoadile = value
            writeToFile(appData)
        }

    var check_service: String
        get() = appData.check_service
        set(value) {
            appData.check_service = value
            writeToFile(appData)
        }


    var vpn_ip_dualLoad: String
        get() = appData.vpn_ip_dualLoad
        set(value) {
            appData.vpn_ip_dualLoad = value
            writeToFile(appData)
        }
    var vpn_city: String
        get() = appData.vpn_city
        set(value) {
            appData.vpn_city = value
            writeToFile(appData)
        }
    var connection_mode: String
        get() = appData.connection_mode
        set(value) {
            appData.connection_mode = value
            writeToFile(appData)
        }
    var local_clock: String
        get() = appData.local_clock
        set(value) {
            appData.local_clock = value
            writeToFile(appData)
        }
    var ip_lo_dualLoad: String
        get() = appData.ip_lo_dualLoad
        set(value) {
            appData.ip_lo_dualLoad = value
            writeToFile(appData)
        }
    var ip_gsd: String
        get() = appData.ip_gsd
        set(value) {
            appData.ip_gsd = value
            writeToFile(appData)
        }
    var ip_gsd_oth: String
        get() = appData.ip_gsd_oth
        set(value) {
            appData.ip_gsd_oth = value
            writeToFile(appData)
        }

    var gidData: String
        get() = appData.gidData
        set(value) {
            appData.gidData = value
            writeToFile(appData)
        }

    var dual_sp_dow: String
        get() = appData.dual_sp_dow
        set(value) {
            appData.dual_sp_dow = value
            writeToFile(appData)
        }

    var dual_sp_up: String
        get() = appData.dual_sp_up
        set(value) {
            appData.dual_sp_up = value
            writeToFile(appData)
        }


    var onlineAdBean : String
        get() = appData.online_ad_key
        set(value) {
            appData.online_ad_key = value
            writeToFile(appData)
        }

    var onlineRefBean: String
        get() = appData.online_ref_key
        set(value) {
            appData.online_ref_key = value
            writeToFile(appData)
        }

    var online_control_bean: String
        get() = appData.online_control_key
        set(value) {
            appData.online_control_key = value
            writeToFile(appData)
        }
    var online_control_bean_core: Boolean
        get() = appData.online_control_key_core
        set(value) {
            appData.online_control_key_core = value
            writeToFile(appData)
        }

    var ref_data: String
        get() = appData.ref_data
        set(value) {
            appData.ref_data = value
            writeToFile(appData)
        }

    var net1: Int
        get() = appData.net1
        set(value) {
            appData.net1 = value
            writeToFile(appData)
        }
    var net2: Int
        get() = appData.net2
        set(value) {
            appData.net2 = value
            writeToFile(appData)
        }
    var ad_date_app: Long
        get() = appData.ad_date_app
        set(value) {
            appData.ad_date_app = value
            writeToFile(appData)
        }
    private fun writeToFile(data: AppData) {
        val dataString = gson.toJson(data)
        fileStorageManager.saveData(dataString)
    }
}


