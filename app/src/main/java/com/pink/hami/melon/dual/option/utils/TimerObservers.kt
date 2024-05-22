package com.pink.hami.melon.dual.option.utils

object TimerObservers {
    private val observers = mutableListOf<(String) -> Unit>()

    fun addObserver(observer: (String) -> Unit) {
        observers.add(observer)
    }

    fun removeObserver(observer: (String) -> Unit) {
        observers.remove(observer)
    }

    fun notifyObservers(timeString: String) {
        observers.forEach { it(timeString) }
    }
}
