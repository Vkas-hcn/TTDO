package com.pink.hami.melon.dual.option.ui.wwwwgidasd.aaagggg

import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.databinding.ActivityWebNetBinding
import com.pink.hami.melon.dual.option.funutils.WebFunHelp
import com.pink.hami.melon.dual.option.R

class AgreementActivity : BaseActivity<ActivityWebNetBinding>(
    R.layout.activity_web_net
) {


    override fun initViewComponents() {
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun initializeData() {
        WebFunHelp.initWeb(binding)
    }
}