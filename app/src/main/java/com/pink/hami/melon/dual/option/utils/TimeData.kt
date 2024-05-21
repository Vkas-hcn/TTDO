package com.pink.hami.melon.dual.option.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.DecimalFormat

object TimeData {
    var skTime = 0
    var isStopThread = true

    fun sendTimerInformation() {
        GlobalScope.launch {
            while (isActive) {
                skTime++
                delay(1000)
            }
        }
    }


    fun startTiming() {
        if (isStopThread) {
            skTime = 0
        }
        isStopThread = false
    }
    fun endTiming() {
        skTime = 0
        isStopThread = true
    }

    private fun formatTime(timerData: Int): String {
        val hh: String = DecimalFormat("00").format(timerData / 3600)
        val mm: String = DecimalFormat("00").format(timerData % 3600 / 60)
        val ss: String = DecimalFormat("00").format(timerData % 60)
        return "$hh:$mm:$ss"
    }
    fun getTiming():String{
        return if(!isStopThread){
            formatTime(skTime)
        }else{
            "00:00:00"
        }
    }
}