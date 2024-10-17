package com.pink.hami.melon.dual.option.bjfklieaf.fast.show.wwwwgidasd.aaagggg

import android.app.Activity
import android.content.Intent
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
        val data = Intent().apply {
            putExtra("key", "value")
        }
        setResult(Activity.RESULT_OK, data)
    }

    override fun initializeData() {
        WebFunHelp.initWeb(binding)
    }
}