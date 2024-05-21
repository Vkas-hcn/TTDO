package com.pink.hami.melon.dual.option.model

import androidx.lifecycle.ViewModel
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.ui.list.ListActivity
import com.pink.hami.melon.dual.option.ui.list.ListServiceAdapter
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.app.adload.DualLoad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListViewModel:ViewModel() {
    lateinit var skVpnServiceBean: VpnServiceBean
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
                ecVpnServiceBeanList[position].best_smart == checkSkVpnServiceBeanClick.best_smart
    }

    private fun handleSameServerSelected(activity: AppCompatActivity) {
        if (!App.vpnLink) {
            App.serviceState = "disconnect"
            activity.finish()
            SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
        }
    }

    private fun updateServerSelection(position: Int) {
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            ecVpnServiceBeanList[index].check_smart = position == index
            if (ecVpnServiceBeanList[index].check_smart) {
                checkSkVpnServiceBean = ecVpnServiceBeanList[index]
            }
        }
    }

    fun initAllAdapter(activity: ListActivity, onClick:(activity: ListActivity, position:Int)->Unit) {
        getAllServer()
       activity.binding.rvList.adapter = listServiceAdapter
       activity.binding.rvList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
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
        allVpnListData = SmileKey.getAllVpnListData()!!
        ecVpnServiceBeanList = allVpnListData
    }

    private fun updateSelection() {
        ecVpnServiceBeanList.forEachIndexed { index, vpnServiceBean ->
            vpnServiceBean.check_smart = if (checkSkVpnServiceBeanClick.best_smart) {
                ecVpnServiceBeanList[0].check_smart = true

                index == 0
            } else {
                ecVpnServiceBeanList[0].check_smart = false

                vpnServiceBean.ip == checkSkVpnServiceBeanClick.ip
            }
        }
    }

    private fun initializeAdapter() {
        listServiceAdapter = ListServiceAdapter(ecVpnServiceBeanList)
    }




    fun returnToHomePage(activity: ListActivity) {
        val res = DualLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            activity.finish()
        } else {
            showBackFun(res,activity)
        }
    }
     private fun showBackFun(it: Any, activity: ListActivity) {
        DualLoad.showFullScreenOf(
            where = SmileKey.POS_BACK,
            context = activity,
            res = it,
            onShowCompleted = {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.finish()
                }
            }
        )
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
            SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
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
        SmileKey.check_service = Gson().toJson(checkSkVpnServiceBean)
    }

    private fun updateEcVpnServiceBeanListSelection() {
        ecVpnServiceBeanList.forEachIndexed { index, _ ->
            ecVpnServiceBeanList[index].check_smart =
                (ecVpnServiceBeanList[index].ip == checkSkVpnServiceBeanClick.ip && ecVpnServiceBeanList[index].best_smart == checkSkVpnServiceBeanClick.best_smart)
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