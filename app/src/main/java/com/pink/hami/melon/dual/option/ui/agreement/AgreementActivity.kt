package com.pink.hami.melon.dual.option.ui.agreement

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.databinding.ActivityWebNetBinding
import com.pink.hami.melon.dual.option.model.WebViewModel
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.pink.hami.melon.dual.option.R

class AgreementActivity : BaseActivity<ActivityWebNetBinding, WebViewModel>(
    R.layout.activity_web_net, WebViewModel::class.java
) {
    override fun intiView() {
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun initData() {
        binding.webViewSmile.loadUrl(SmileKey.web_smile_url)
        binding.webViewSmile.settings.javaScriptEnabled = true
        binding.webViewSmile.webChromeClient=object :WebChromeClient(){

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