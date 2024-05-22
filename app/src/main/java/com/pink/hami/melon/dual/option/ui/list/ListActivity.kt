package com.pink.hami.melon.dual.option.ui.list

import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityListBinding
import com.pink.hami.melon.dual.option.model.ListViewModel
import com.pink.hami.melon.dual.option.utils.DualContext
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils.getSmileImage

class ListActivity : BaseActivity<ActivityListBinding, ListViewModel>(
    R.layout.activity_list, ListViewModel::class.java
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
            viewModel.returnToHomePage(this)
        }
    }

    private fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this) {
            viewModel.returnToHomePage(this@ListActivity)
        }
    }

    override fun initializeData() {
        if (DualContext.isHaveServeData(this)) {
            showServiceList()
            setupVpnServiceBean()
            configureVpnServiceBeanDisplay()
            setupAdapter()
        } else {
            hideServiceList()
        }
    }

    private fun showServiceList() {
        binding.showList = true
    }

    private fun hideServiceList() {
        binding.showList = false
    }

    private fun setupVpnServiceBean() {
        viewModel.checkSkVpnServiceBean = VpnServiceBean()
        viewModel.checkSkVpnServiceBean = if (DualContext.localStorage.check_service.isBlank()) {
            DualContext.getFastVpn() ?: VpnServiceBean()
        } else {
            parseVpnServiceBean(DualContext.localStorage.check_service)
        }
        viewModel.checkSkVpnServiceBeanClick = viewModel.checkSkVpnServiceBean
    }

    private fun parseVpnServiceBean(json: String): VpnServiceBean {
        return Gson().fromJson(json, VpnServiceBean::class.java)
    }

    private fun configureVpnServiceBeanDisplay() {
        val vpnServiceBean = viewModel.checkSkVpnServiceBean
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
        binding.imgFlagList.setImageResource(vpnServiceBean.country_name.getSmileImage())
    }

    private fun setupAdapter() {
        viewModel.initAllAdapter(this) { activity, position ->
            viewModel.selectServer(activity, position)
        }
    }

    private fun listenToTheInputBox() {
        binding.edtSearchService.addTextChangedListener { text ->
            val query = text.toString()
            filterServiceList(query)
        }
    }

    private fun filterServiceList(query: String) {
        viewModel.listServiceAdapter.filter(query)
    }
}
