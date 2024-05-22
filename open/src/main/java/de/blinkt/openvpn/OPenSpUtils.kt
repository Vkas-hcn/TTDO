package de.blinkt.openvpn

import android.util.Log
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import de.blinkt.openvpn.core.ICSOpenVPNApplication
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object OPenSpUtils {
    private val mmkv by lazy {
        MMKV.mmkvWithID("DualLoad", MMKV.MULTI_PROCESS_MODE)
    }
    fun saveData(data: String) {
        try {
            val file = File(ICSOpenVPNApplication.context.filesDir, "shared_data.json")
            FileWriter(file).use { it.write(data) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun loadData(): String? {
        return try {
            Log.e("TAG", "loadData-open: ${ICSOpenVPNApplication.context.filesDir}")
            val file = File(ICSOpenVPNApplication.context.filesDir, "shared_data.json")
            if (file.exists()) {
                FileReader(file).use { it.readText() }
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    fun getSpeedData(upData: String, downData: String, statistics: String) {
        mmkv.encode("speed_dow", downData)
        mmkv.encode("speed_up", upData)
        loadData()
        val jsonData = loadData()
        val bean = Gson().fromJson(jsonData, AppData::class.java)
        bean.dual_sp_dow = downData
        bean.dual_sp_up = upData
        Log.e("TAG", "getSpeedData: $bean", )
        saveData(Gson().toJson(bean))
    }
}