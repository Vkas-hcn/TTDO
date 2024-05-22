package com.pink.hami.melon.dual.option.model

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FirstViewModel:ViewModel() {
    val toMainLive = MutableLiveData<String>()
    val showOpen = MutableLiveData<Any>()
}