package com.pink.hami.melon.dual.option.ui.list

import android.view.KeyEvent
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityListBinding
import com.pink.hami.melon.dual.option.model.ListViewModel
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.google.gson.Gson

class ListActivity : BaseActivity<ActivityListBinding, ListViewModel>(
    R.layout.activity_list, ListViewModel::class.java
) {
    override fun intiView() {
        binding.imgBack.setOnClickListener {
            viewModel.returnToHomePage(this)
        }
    }

    override fun initData() {
        if (SmileKey.isHaveServeData(this)) {
            binding.showList = true
            viewModel.checkSkVpnServiceBean = VpnServiceBean()
            viewModel.checkSkVpnServiceBean = if (SmileKey.check_service.isBlank()) {
                SmileKey.getFastVpn()!!
            } else {
                Gson().fromJson(
                    SmileKey.check_service,
                    VpnServiceBean::class.java
                )
            }
            viewModel.checkSkVpnServiceBeanClick = viewModel.checkSkVpnServiceBean
            viewModel.initAllAdapter(this) { activity, position ->
                viewModel.selectServer(activity, position)
            }
        } else {
            binding.showList = false
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewModel.returnToHomePage(this)
        }
        return true
    }
}