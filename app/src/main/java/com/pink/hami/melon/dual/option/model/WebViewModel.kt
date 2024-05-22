package com.pink.hami.melon.dual.option.model

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModel
import com.pink.hami.melon.dual.option.databinding.ActivityWebNetBinding
import com.pink.hami.melon.dual.option.utils.DualContext

class WebViewModel:ViewModel() {
    fun initWeb(binding : ActivityWebNetBinding){
        binding.webViewSmile.loadUrl(DualContext.web_dualLoadile_url)
        binding.webViewSmile.settings.javaScriptEnabled = true
        binding.webViewSmile.webChromeClient = object : WebChromeClient() {

        }
        binding.webViewSmile.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.startsWith("http") == true) {
                    view?.loadUrl(url)
                }
                return true
            }
        }
    }
}