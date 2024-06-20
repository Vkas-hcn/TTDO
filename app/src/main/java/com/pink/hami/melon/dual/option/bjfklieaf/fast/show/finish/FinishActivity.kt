package com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.utils.DualContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.databinding.ActivityFinishBinding
import com.pink.hami.melon.dual.option.funutils.FinishViewFun

class FinishActivity : BaseActivity<ActivityFinishBinding>(
    R.layout.activity_finish
) {
    private lateinit var vpnServiceBean: VpnServiceBean
    private var isConnect: Boolean = false
    private lateinit var connectionStatus: ConnectionStatus

    override fun initViewComponents() {
        extractBundleData()
        setupBackButton()
        addBackPressedCallback()
        val data = Intent().apply {
            // Add any data you want to return
            putExtra("key", "value")
        }
        setResult(Activity.RESULT_OK, data)
    }

    private fun extractBundleData() {
        val bundle = intent.extras
        vpnServiceBean = parseVpnServiceBean(bundle)
        isConnect = getConnectionStatus(bundle)
    }

    private fun parseVpnServiceBean(bundle: Bundle?): VpnServiceBean {
        val vpnServiceJson = bundle?.getString(DualContext.cuDualConnected)
        val type = object : TypeToken<VpnServiceBean?>() {}.type
        return Gson().fromJson(vpnServiceJson, type)
    }

    private fun getConnectionStatus(bundle: Bundle?): Boolean {
        return bundle?.getBoolean(DualContext.isDualConnected) ?: false
    }

    private fun setupBackButton() {
        binding.imgBack.setOnClickListener {
            FinishViewFun.returnToHomePage(this)
        }
    }

    private fun addBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this) {
            FinishViewFun.returnToHomePage(this@FinishActivity)
        }
    }

    override fun initializeData() {
        connectionStatus = if (isConnect) {
            ConnectionStatus.CONNECTED
        } else {
            ConnectionStatus.DISCONNECTED
        }
        updateUI(connectionStatus)
    }

    private fun updateUI(status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.CONNECTED -> {
                binding.imgVpnEnd.setImageResource(R.drawable.ic_end_connect)
                binding.tvEndState1.text = "Connected succeed"
                binding.tvEndState2.text = "You are very safe right now!"
            }

            ConnectionStatus.DISCONNECTED -> {
                binding.imgVpnEnd.setImageResource(R.drawable.ic_end_dis)
                binding.tvEndState1.text = "Disconnection succeed"
                binding.tvEndState2.text = "You have exposed in danger!"
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    enum class ConnectionStatus {
        CONNECTED,
        DISCONNECTED
    }
}
