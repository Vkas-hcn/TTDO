package com.pink.hami.melon.dual.option.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

class FileStorageManager(private val context: Context) {
    private val fileName = "shared_data.json"

    fun saveData(data: String) {
        try {
            val file = File(context.filesDir, fileName)
            FileWriter(file).use { it.write(data) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadData(): String? {
        return try {
            val file = File(context.filesDir, fileName)
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
}
