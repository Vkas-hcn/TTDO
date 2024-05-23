package com.pink.hami.melon.dual.option.funutils

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import com.pink.hami.melon.dual.option.databinding.ActivityWebNetBinding
import com.pink.hami.melon.dual.option.utils.DualContext

object WebFunHelp {
    fun initWeb(binding : ActivityWebNetBinding){
        binding.webViewDual.loadUrl(DualContext.web_dualLoadile_url)
        binding.webViewDual.settings.javaScriptEnabled = true
        binding.webViewDual.webChromeClient = object : WebChromeClient() {

        }
        binding.webViewDual.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.startsWith("http") == true) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
    }
}