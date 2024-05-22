package com.pink.hami.melon.dual.option.ui.wwwwgidasd.aaagggg

import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.databinding.ActivityWebNetBinding
import com.pink.hami.melon.dual.option.model.WebViewModel
import com.pink.hami.melon.dual.option.R

class AgreementActivity : BaseActivity<ActivityWebNetBinding, WebViewModel>(
    R.layout.activity_web_net, WebViewModel::class.java
) {


    override fun initViewComponents() {
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun initializeData() {
        viewModel.initWeb(binding)
    }
}