package de.blinkt.openvpn

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object OPenSpUtils {
    private fun saveData(data: String) {
        try {
            val file = File(openContext.filesDir, "shared_data.json")
            FileWriter(file).use { it.write(data) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    lateinit var openContext:Context
    fun initOpenContext(context:Context){
        openContext = context
    }
    private fun loadData(): String? {
        return try {
            val file = File(openContext.filesDir, "shared_data.json")
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
        loadData()
        val jsonData = loadData()
        val bean = Gson().fromJson(jsonData, AppData::class.java)
        bean.dual_sp_dow = downData
        bean.dual_sp_up = upData
        saveData(Gson().toJson(bean))
    }
}