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
        initializeViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupBinding()
        setupLifecycleOwner()
        initViewComponents()
        initializeData()
    }

    private fun initializeViewModel(): VM {
        return ViewModelProvider(this).get(viewModelClass)
    }

    private fun setupBinding() {
        binding = DataBindingUtil.setContentView(this, layoutId)
    }

    private fun setupLifecycleOwner() {
        binding.lifecycleOwner = this
    }

    abstract fun initViewComponents()
    abstract fun initializeData()

    inline fun <reified T : AppCompatActivity> launchActivity() {
        val intent = createIntent<T>()
        startActivity(intent)
    }

    inline fun <reified T : AppCompatActivity> launchActivityWithExtras(params: Bundle) {
        val intent = createIntent<T>()
        addExtrasToIntent(intent, params)
        startActivity(intent)
    }

    inline fun <reified T : AppCompatActivity> launchActivityForResult(params: Bundle? = null, requestCode: Int) {
        val intent = createIntent<T>()
        params?.let {
            addExtrasToIntent(intent, it)
        }
        startActivityForResult(intent, requestCode)
    }

    // Changed visibility from private to internal to avoid visibility issues
     inline fun <reified T : AppCompatActivity> createIntent(): Intent {
        return Intent(this, T::class.java)
    }

    fun addExtrasToIntent(intent: Intent, params: Bundle) {
        intent.putExtras(params)
    }
}
