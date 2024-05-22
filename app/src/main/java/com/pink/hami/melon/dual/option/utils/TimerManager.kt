package com.pink.hami.melon.dual.option.utils

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

object TimerManager {
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var isRunning: Boolean = false
    private var job: Job? = null

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    private val scope = CoroutineScope(coroutineContext)

    fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            job = scope.launch {
                while (isRunning) {
                    delay(1000)
                    elapsedTime = System.currentTimeMillis() - startTime
                    onTimeUpdate(elapsedTime)
                }
            }
        }
    }

    fun stopTimer() {
        if (isRunning) {
            isRunning = false
            job?.cancel()
        }
    }

    fun resetTimer() {
        stopTimer()
        elapsedTime = 0L
        onTimeUpdate(elapsedTime)
    }

    private fun onTimeUpdate(elapsedTime: Long) {
        val hours = (elapsedTime / (1000 * 60 * 60)).toInt()
        val minutes = ((elapsedTime / (1000 * 60)) % 60).toInt()
        val seconds = ((elapsedTime / 1000) % 60).toInt()
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        // Notify observers with the updated time string
        TimerObservers.notifyObservers(timeString)
    }
}
