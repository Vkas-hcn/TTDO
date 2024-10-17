package com.pink.hami.melon.dual.option.funutils

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.list.ListActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.list.ListServiceAdapter
import com.pink.hami.melon.dual.option.utils.DualContext
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.list.VerticalSpaceItemDecoration
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object ListFunHelp {
    lateinit var allVpnListData: MutableList<VpnServiceBean>
    var listServiceAdapter: ListServiceAdapter? = null
    var ecVpnServiceBeanList: MutableList<VpnServiceBean> = ArrayList()
    lateinit var checkSkVpnServiceBean: VpnServiceBean
    lateinit var checkSkVpnServiceBeanClick: VpnServiceBean

    fun selectServer(activity: AppCompatActivity, positionBean: VpnServiceBean) {
        if (isSameServerSelected(positionBean)) {
            handleSameServerSelected(activity)
            return
        }

        updateServerSelection(positionBean)
        listServiceAdapter?.notifyDataSetChanged()
        showDisconnectDialog(activity)
    }

    private fun isSameServerSelected(positionBean: VpnServiceBean): Boolean {
        return positionBean.ip == checkSkVpnServiceBeanClick.ip &&
                positionBean.best_dualLoad == checkSkVpnServiceBeanClick.best_dualLoad
    }

    private fun handleSameServerSelected(activity: AppCompatActivity) {
        if (!App.vpnLink) {
            App.serviceState = "disconnect"
            activity.finish()
            DualContext.localStorage.check_service = Gson().toJson(checkSkVpnServiceBean)
        }
    }

    private fun updateServerSelection(positionBean: VpnServiceBean) {
        ecVpnServiceBeanList.forEachIndexed { index, item ->
            ecVpnServiceBeanList[index].check_dualLoad = positionBean == item
            if (ecVpnServiceBeanList[index].check_dualLoad) {
                checkSkVpnServiceBean = ecVpnServiceBeanList[index]
            }
        }
    }

    fun initAllAdapter(
        activity: ListActivity,
        onClick: (activity: ListActivity, positionBean: VpnServiceBean) -> Unit
    ) {
        getAllServer()
        activity.binding.rvList.adapter = listServiceAdapter
        val verticalSpaceHeight =
            activity.resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)
        activity.binding.rvList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)
        activity.binding.rvList.addItemDecoration(VerticalSpaceItemDecoration(verticalSpaceHeight))

        listServiceAdapter?.setOnItemClickListener(object : ListServiceAdapter.OnItemClickListener {
            override fun onItemClick(positionBean: VpnServiceBean) {
                onClick(activity, positionBean)
            }
        })
    }

    private fun getAllServer() {
        initializeData()
        updateSelection()
        initializeAdapter()
    }

    private fun initializeData() {
        allVpnListData = DualContext.getAllVpnListData()!!
        ecVpnServiceBeanList = allVpnListData
    }

    private fun updateSelection() {
        ecVpnServiceBeanList.forEachIndexed { index, vpnServiceBean ->
            vpnServiceBean.check_dualLoad = if (checkSkVpnServiceBeanClick.best_dualLoad) {
                ecVpnServiceBeanList[0].check_dualLoad = true
                index == 0
            } else {
                ecVpnServiceBeanList[0].check_dualLoad = false
                vpnServiceBean.ip == checkSkVpnServiceBeanClick.ip
            }
        }
    }

    private fun initializeAdapter() {
        listServiceAdapter = ListServiceAdapter(ecVpnServiceBeanList)
    }


    fun returnToHomePage(activity: ListActivity) {
        if (App.adManagerBackService.canShowAd() == 0) {
            activity.finish()
            return
        }
        activity.binding.dataLoading = true
        App.adManagerBackService.loadAd()
        DualONlineFun.emitPointData("v20proxy")
        activity.lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            var elapsedTime: Long
            try {
                while (isActive) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime >= 4000L) {
                        Log.e("TAG", "连接超时")
                        activity.finish()
                        activity.binding.dataLoading = false
                        break
                    }

                    if (App.adManagerBackService.canShowAd() == 1) {
                        App.adManagerBackService.showAd(activity) {
                            activity.finish()
                            activity.binding.dataLoading = false
                        }
                        break
                    }
                    delay(500L)
                }
            } catch (e: Exception) {
                activity.finish()
                activity.binding.dataLoading = false
            }
        }
    }

    private fun showDisconnectDialog(activity: AppCompatActivity) {
        if (handleVpnDisconnectedState(activity)) {
            return
        }

        createDisconnectDialog(activity).apply {
            configureDialogSize(this)
            setCancelable(false)
            show()
            configureButtonColors(this)
        }
    }

    private fun handleVpnDisconnectedState(activity: AppCompatActivity): Boolean {
        if (!App.vpnLink) {
            activity.finish()
            App.serviceState = "disconnect"
            DualContext.localStorage.check_service = Gson().toJson(checkSkVpnServiceBean)
            return true
        }
        return false
    }

    private fun createDisconnectDialog(activity: AppCompatActivity): AlertDialog {
        return AlertDialog.Builder(activity)
            .setTitle("Are you sure to disconnect current server")
            .setNegativeButton("CANCEL") { dialog, _ ->
                handleCancelClick(dialog)
            }
            .setPositiveButton("DISCONNECT") { dialog, _ ->
                handleDisconnectClick(dialog, activity)
            }.create()
    }

    private fun handleCancelClick(dialog: DialogInterface) {
        dialog.dismiss()
        updateEcVpnServiceBeanListSelection()
        listServiceAdapter?.notifyDataSetChanged()
    }

    private fun handleDisconnectClick(dialog: DialogInterface, activity: AppCompatActivity) {
        dialog.dismiss()
        activity.finish()
        App.serviceState = "connect"
        DualContext.localStorage.check_service = Gson().toJson(checkSkVpnServiceBean)
    }

    private fun updateEcVpnServiceBeanListSelection() {
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            ecVpnServiceBeanList[index].check_dualLoad =
                (ecVpnServiceBeanList[index].ip == checkSkVpnServiceBeanClick.ip && ecVpnServiceBeanList[index].best_dualLoad == checkSkVpnServiceBeanClick.best_dualLoad)
        }
    }

    private fun configureDialogSize(dialog: AlertDialog) {
        dialog.window?.let { window ->
            val params = window.attributes
            params.width = 200
            params.height = 200
            window.attributes = params
        }
    }

    private fun configureButtonColors(dialog: AlertDialog) {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

}