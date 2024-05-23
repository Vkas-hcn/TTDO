package com.pink.hami.melon.dual.option.utils.net

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import kotlin.system.exitProcess

class DialogHandler(private val context: Context) {

    fun showCannotUseDialog() {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("Due to policy reasons, this service is not available in your country")
            .setCancelable(false)
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.dismiss()
                exitApp()
            }.create()
        dialog.setCancelable(false)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
    }

    private fun exitApp() {
        if (context is Activity) {
            context.finish()
        }
        exitProcess(0)
    }
}
