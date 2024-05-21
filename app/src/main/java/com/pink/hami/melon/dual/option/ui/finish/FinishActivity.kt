package com.pink.hami.melon.dual.option.ui.finish

import android.annotation.SuppressLint
import android.view.KeyEvent
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.model.FinishViewModel
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.databinding.ActivityFinishBinding

class FinishActivity : BaseActivity<ActivityFinishBinding, FinishViewModel>(
    R.layout.activity_finish, FinishViewModel::class.java
) {
    private lateinit var vpnServiceBean: VpnServiceBean
    private var isConnect: Boolean = false
    override fun intiView() {
        val bundle = intent.extras
        vpnServiceBean = Gson().fromJson(
            bundle?.getString(SmileKey.cuSmileConnected),
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        isConnect = bundle?.getBoolean(SmileKey.isSmileConnected) ?: false
        binding.imgBack.setOnClickListener {
            viewModel.returnToHomePage(this)
        }
        viewModel.showEndAd(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        if (isConnect) {
            binding.imgVpnEnd.setImageResource(R.drawable.ic_end_connect)
            binding.tvEndState1.text = "Connected succeed"
            binding.tvEndState2.text = "You are very safe right now!"
        } else {
            binding.imgVpnEnd.setImageResource(R.drawable.ic_end_dis)
            binding.tvEndState1.text = "Disconnection succeed"
            binding.tvEndState2.text = "You have exposed in danger!"
        }
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            viewModel.returnToHomePage(this)
        }
        return true
    }


}