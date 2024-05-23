package com.pink.hami.melon.dual.option.ui.list

import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityListBinding
import com.pink.hami.melon.dual.option.funutils.ListFunHelp
import com.pink.hami.melon.dual.option.utils.DualContext
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils.getDualImage

class ListActivity : BaseActivity<ActivityListBinding>(
    R.layout.activity_list
) {
    override fun initViewComponents() {
        setupListeners()
        configureBackButton()
        handleOnBackPressed()
    }

    private fun setupListeners() {
        listenToTheInputBox()
    }

    private fun configureBackButton() {
        binding.imgBack.setOnClickListener {
            ListFunHelp.returnToHomePage(this)
        }
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            ListFunHelp.returnToHomePage(this@ListActivity)
        }
    }

    override fun initializeData() {
        if (DualContext.isHaveServeData(this)) {
            setupVpnServiceBean()
            configureVpnServiceBeanDisplay()
            setupAdapter()
        }
    }

    private fun setupVpnServiceBean() {
        ListFunHelp.checkSkVpnServiceBean = VpnServiceBean()
        ListFunHelp.checkSkVpnServiceBean = if (DualContext.localStorage.check_service.isBlank()) {
            DualContext.getFastVpn() ?: VpnServiceBean()
        } else {
            parseVpnServiceBean(DualContext.localStorage.check_service)
        }
        ListFunHelp.checkSkVpnServiceBeanClick = ListFunHelp.checkSkVpnServiceBean
    }

    private fun parseVpnServiceBean(json: String): VpnServiceBean {
        return Gson().fromJson(json, VpnServiceBean::class.java)
    }

    private fun configureVpnServiceBeanDisplay() {
        val vpnServiceBean = ListFunHelp.checkSkVpnServiceBean
        if (vpnServiceBean.best_dualLoad) {
            displayFastServer()
        } else {
            displayCountryAndCity(vpnServiceBean)
        }
    }

    private fun displayFastServer() {
        binding.tvCountryList.text = "Faster Server"
        binding.imgFlagList.setImageResource(R.drawable.ic_fast)
    }

    private fun displayCountryAndCity(vpnServiceBean: VpnServiceBean) {
        binding.tvCountryList.text = String.format("${vpnServiceBean.country_name},${vpnServiceBean.city}")
        binding.imgFlagList.setImageResource(vpnServiceBean.country_name.getDualImage())
    }

    private fun setupAdapter() {
        ListFunHelp.initAllAdapter(this) { activity, positionBean ->
            ListFunHelp.selectServer(activity, positionBean)
        }
    }

    private fun listenToTheInputBox() {
        binding.edtSearchService.addTextChangedListener { text ->
            val query = text.toString()
            filterServiceList(query)
        }
    }

    private fun filterServiceList(query: String) {
        if(ListFunHelp.listServiceAdapter == null){return}
        ListFunHelp.listServiceAdapter?.filter(query)
    }
}
