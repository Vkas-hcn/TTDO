package com.pink.hami.melon.dual.option.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<B : ViewDataBinding, VM : ViewModel>(
    private val layoutId: Int,
    private val viewModelClass: Class<VM>
) : AppCompatActivity() {

    lateinit var binding: B
        private set

    protected val viewModel: VM by lazy {
        ViewModelProvider(this).get(viewModelClass)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this
        intiView()
        initData()
    }

    abstract fun intiView()
    abstract fun initData()

    inline fun <reified T : AppCompatActivity> startActivityFirst() {
        val intent = Intent(this, T::class.java)
        startActivity(intent)
    }

     inline fun <reified T : AppCompatActivity> startActivityWithParamsFirst(params: Bundle) {
        val intent = Intent(this, T::class.java)
        intent.putExtras(params)
        startActivity(intent)
    }

     inline fun <reified T : AppCompatActivity> startActivityWithReFirst(params: Bundle?=null,code:Int) {
        val intent = Intent(this, T::class.java)
         if (params != null) {
             intent.putExtras(params)
         }
        startActivityForResult(intent,code)
    }
}