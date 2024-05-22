package com.pink.hami.melon.dual.option.funutils

import androidx.lifecycle.ViewModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.ui.list.ListActivity
import com.pink.hami.melon.dual.option.ui.list.ListServiceAdapter
import com.pink.hami.melon.dual.option.utils.DualContext
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.ui.list.VerticalSpaceItemDecoration

class ListFunHelp : ViewModel() {
    lateinit var allVpnListData: MutableList<VpnServiceBean>
    lateinit var listServiceAdapter: ListServiceAdapter
    var ecVpnServiceBeanList: MutableList<VpnServiceBean> = ArrayList()
    lateinit var checkSkVpnServiceBean: VpnServiceBean
    lateinit var checkSkVpnServiceBeanClick: VpnServiceBean

    fun selectServer(activity: AppCompatActivity, position: Int) {
        if (isSameServerSelected(position)) {
            handleSameServerSelected(activity)
            return
        }

        updateServerSelection(position)
        listServiceAdapter.notifyDataSetChanged()
        showDisconnectDialog(activity)
    }

    private fun isSameServerSelected(position: Int): Boolean {
        return ecVpnServiceBeanList[position].ip == checkSkVpnServiceBeanClick.ip &&
                ecVpnServiceBeanList[position].best_dualLoad == checkSkVpnServiceBeanClick.best_dualLoad
    }

    private fun handleSameServerSelected(activity: AppCompatActivity) {
        if (!App.vpnLink) {
            App.serviceState = "disconnect"
            activity.finish()
            DualContext.localStorage.check_service = Gson().toJson(checkSkVpnServiceBean)
        }
    }

    private fun updateServerSelection(position: Int) {
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            ecVpnServiceBeanList[index].check_dualLoad = position == index
            if (ecVpnServiceBeanList[index].check_dualLoad) {
                checkSkVpnServiceBean = ecVpnServiceBeanList[index]
            }
        }
    }

    fun initAllAdapter(
        activity: ListActivity,
        onClick: (activity: ListActivity, position: Int) -> Unit
    ) {
        getAllServer()
        activity.binding.rvList.adapter = listServiceAdapter
        val verticalSpaceHeight = activity.resources.getDimensionPixelSize(R.dimen.recycler_view_item_spacing)
        activity.binding.rvList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(activity)
        activity.binding.rvList.addItemDecoration(VerticalSpaceItemDecoration(verticalSpaceHeight))

        listServiceAdapter.setOnItemClickListener(object : ListServiceAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                onClick(activity, position)
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
        activity.finish()
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
        listServiceAdapter.notifyDataSetChanged()
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