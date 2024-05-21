package com.pink.hami.melon.dual.option.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.*

class TimeUtils {

    var isStopThread = true
    private var listener: TimeUtilsListener? = null
    private var job: Job? = null

    interface TimeUtilsListener {
        fun onTimeChanged()
    }

    fun setListener(listener: TimeUtilsListener) {
        this.listener = listener
    }

    fun sendTimerInformation() {
        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                if (!isStopThread) {
                    listener?.onTimeChanged()
                }
                delay(1000)
            }
        }
    }

    fun startTiming() {
        if (isStopThread) {
            TimeData.startTiming()
            sendTimerInformation()
        }
        isStopThread = false
    }

    fun endTiming() {
        isStopThread = true
        job?.cancel()
        TimeData.endTiming()
        listener?.onTimeChanged()
    }

}
