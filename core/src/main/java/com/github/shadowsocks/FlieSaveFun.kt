package com.github.shadowsocks

import android.content.Context
import android.util.Log
import android.text.format.Formatter
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object FlieSaveFun {
    fun saveData(data: String) {
        try {
            val file = File(Core.app.filesDir, "shared_data.json")
            FileWriter(file).use { it.write(data) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun loadData(): String? {
        return try {
            val file = File(Core.app.filesDir, "shared_data.json")
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
    fun getSpeedData(service: BaseService.Interface, stats: TrafficStats) {
        val context = service as Context
        val formattedData = formatTrafficData(context, stats)
        val extractedData = extractSpeedData(formattedData)
        if (extractedData != null) {
            updateAppData(extractedData)
        }
    }

    private fun formatTrafficData(context: Context, stats: TrafficStats): String {
        val txSpeed = formatSpeed(context, stats.txRate)
        val rxSpeed = formatSpeed(context, stats.rxRate)
        return context.getString(com.github.shadowsocks.core.R.string.traffic, txSpeed, rxSpeed)
    }

    private fun formatSpeed(context: Context, rate: Long): String {
        return context.getString(com.github.shadowsocks.core.R.string.speed, Formatter.formatFileSize(context, rate))
    }

    private fun extractSpeedData(data: String): SpeedData? {
        val pattern = """([\d.]+)\s*([^\s]+)\s*([↑↓])\s*([\d.]+)\s*([^\s]+)\s*([↑↓])""".toRegex()
        val matches = pattern.find(data)
        return if (matches != null) {
            val (value1, unit1, arrow1, value2, unit2, arrow2) = matches.destructured
            SpeedData(value1, unit1, arrow1, value2, unit2, arrow2)
        } else {
            null
        }
    }

    private data class SpeedData(
        val value1: String,
        val unit1: String,
        val arrow1: String,
        val value2: String,
        val unit2: String,
        val arrow2: String
    )

    private fun updateAppData(speedData: SpeedData) {
        val jsonData = loadData()
        val bean = Gson().fromJson(jsonData, AppData::class.java)
        bean.dual_sp_dow = "${speedData.value1} ${speedData.unit1}"
        bean.dual_sp_up = "${speedData.value2} ${speedData.unit2}"
        saveData(Gson().toJson(bean))
    }
}