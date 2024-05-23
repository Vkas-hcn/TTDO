package com.pink.hami.melon.dual.option.ui.main

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.ui.wwwwgidasd.aaagggg.AgreementActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.google.gson.Gson
import de.blinkt.openvpn.api.ExternalOpenVPNService
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.databinding.ActivityMainBinding
import com.pink.hami.melon.dual.option.funutils.MainFunHelp
import com.pink.hami.melon.dual.option.utils.TimerObservers
import com.pink.hami.melon.dual.option.utils.net.DialogHandler
import com.pink.hami.melon.dual.option.utils.net.IpChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main),
    ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {
    companion object {
        var stateListener: ((BaseService.State) -> Unit)? = null
    }

    enum class AgreementStatus {
        Auto,
        SS,
        Open
    }

    private lateinit var ipChecker: IpChecker
    private lateinit var dialogHandler: DialogHandler
    lateinit var mainFun: MainFunHelp
    override fun initViewComponents() {
        mainFun = MainFunHelp()
        ipChecker = IpChecker(this)
        dialogHandler = DialogHandler(this)
        clickListener()
        initVpnSetting()
        setServiceData()
        onBackPressedDispatcher.addCallback(this) {
            if (binding.showGuide == true || binding.dlMain.isOpen) {
                binding.showGuide = false
                binding.dlMain.close()
            } else {
                mainFun.clickChange(this@MainActivity, nextFun = {
                    finish()
                })
            }
        }
        TimerObservers.addObserver { timeString ->
            runOnUiThread {
                binding.tvTime.text = timeString
            }
        }
    }

    override fun initializeData() {
        if (ipChecker.checkIp()) {
            dialogHandler.showCannotUseDialog()
            return
        }
        mainFun.initData(this, this)
    }


    private fun clickListener() {
        binding.agreement = DualContext.localStorage.connection_mode.ifEmpty { "0" }
        if (App.isAppRunning) {
            binding.showGuide = false
        } else {
            binding.showGuide = !App.vpnLink
            App.isAppRunning = true
        }
        binding.llSetting.setOnClickListener { }
        binding.imgConnect.setOnClickListener {
            toClickConnect()
        }
        binding.llConnect.setOnClickListener {
            mainFun.clickChange(this, nextFun = {
                toClickConnect()
            })
        }
        binding.lavDual.setOnClickListener {
            mainFun.clickChange(this, nextFun = {
                toClickConnect()
            })
        }
        binding.viewGuideDual.setOnClickListener {
        }

        binding.imgSetting.setOnClickListener {
            mainFun.clickDisConnect(this, nextFun = {
                mainFun.clickChange(this, nextFun = {
                    binding.dlMain.open()
                })
            })
        }

        binding.tvPp.setOnClickListener {
            launchActivity<AgreementActivity>()
        }

        binding.tvAuto.setOnClickListener {
            mainFun.clickDisConnect(this, nextFun = {
                mainFun.clickChange(this, nextFun = {
                    checkAgreement(AgreementStatus.Auto)
                })
            })
        }
        binding.tvOpen.setOnClickListener {
            mainFun.clickDisConnect(this, nextFun = {
                mainFun.clickChange(this, nextFun = {
                    checkAgreement(AgreementStatus.SS)
                })
            })
        }
        binding.tvSs.setOnClickListener {
            mainFun.clickDisConnect(this, nextFun = {
                mainFun.clickChange(this, nextFun = {
                    checkAgreement(AgreementStatus.Open)
                })
            })
        }

        binding.linearLayout.setOnClickListener {
            mainFun.clickChange(this, nextFun = {
                mainFun.jumpToServerList(this)
            })
        }
    }

    private fun setServiceData() {
        mainFun.liveInitializeServerData.observe(this) {
            it?.let {
                mainFun.setFastInformation(it, binding)
            }
        }

        mainFun.liveUpdateServerData.observe(this) {
            it?.let {
                mainFun.whetherRefreshServer = true
                toConnectVpn()
            }
        }
        mainFun.liveNoUpdateServerData.observe(this) {
            it?.let {
                mainFun.whetherRefreshServer = false
                mainFun.setFastInformation(it, binding)
                toConnectVpn()
            }
        }
    }


    private fun checkAgreement(state: AgreementStatus) {
        if (state == AgreementStatus.Auto) {
            binding.tvAuto.background =
                ContextCompat.getDrawable(this, R.drawable.bg_auto_text_op)
            binding.tvSs.background = null
            binding.tvOpen.background = null
        }

        if (state == AgreementStatus.SS) {
            binding.tvAuto.background = null
            binding.tvSs.background = null
            binding.tvOpen.background =
                ContextCompat.getDrawable(this, R.drawable.bg_auto_text_op)
        }

        if (state == AgreementStatus.Open) {
            binding.tvAuto.background = null
            binding.tvSs.background =
                ContextCompat.getDrawable(this, R.drawable.bg_auto_text_op)
            binding.tvOpen.background = null
        }
        val type = when (state) {
            AgreementStatus.Auto -> "0"
            AgreementStatus.SS -> "1"
            AgreementStatus.Open -> "2"
        }
        if (App.vpnLink) {
            if (state == AgreementStatus.SS && binding.agreement != "1") {
                showSwitching(state)
                return
            }
            if (state != AgreementStatus.SS && binding.agreement == "1") {
                showSwitching(state)
                return
            }
            binding.agreement = type

        } else {
            binding.agreement = type
        }
    }

    private fun showSwitching(state: AgreementStatus) {
        val type = when (state) {
            AgreementStatus.Auto -> "0"
            AgreementStatus.SS -> "1"
            AgreementStatus.Open -> "2"
        }
        val dialogVpn: androidx.appcompat.app.AlertDialog =
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Tips")
                .setMessage("switching the connection mode will disconnect the current connection whether to continue")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    toConnectVpn()
                    binding.agreement = type
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.create()
        dialogVpn.setCancelable(false)
        dialogVpn.show()
    }

    private fun toClickConnect() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                DualONlineFun.getLoadIp()
                DualONlineFun.getLoadOthIp()
            }
            if (binding.serviceState == "1") {
                return@launch
            }
        }
        mainFun.updateSkServer(App.vpnLink)
    }

    private fun toConnectVpn() {
        lifecycleScope.launch {
            binding.showGuide = false
            if (!DualContext.isHaveServeData(this@MainActivity)) {
                binding.pbLoading.visibility = View.VISIBLE
                delay(2000)
                binding.pbLoading.visibility = View.GONE
                mainFun.initServerData()
                return@launch
            }
            if (DulaShowDataUtils.isAppOnline(this@MainActivity)) {
                if (!App.vpnLink) {
                    DualContext.localStorage.connection_mode = binding?.agreement!!
                }
                if (ipChecker.checkIp()) {
                    dialogHandler.showCannotUseDialog()
                    return@launch
                }
                if (binding.agreement == "1") {
                    mainFun.startOpenVpn(this@MainActivity)
                } else {
                    connect.launch(null)
                }
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Please check your network connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun initVpnSetting() {
        bindService(
            Intent(this, ExternalOpenVPNService::class.java),
            mConnection,
            BIND_AUTO_CREATE
        )
        mainFun.requestPermissionForResultVPN =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                requestPermissionForResult(it)
            }
    }


    private val connect = registerForActivityResult(StartService()) {
        if (it) {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        } else {
            mainFun.startTheJudgment(this)
        }
    }


    private fun requestPermissionForResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            mainFun.startTheJudgment(this)
        } else {
            Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show()
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName?,
            service: IBinder?,
        ) {
            mainFun.mService = IOpenVPNAPIService.Stub.asInterface(service)
            try {
                mainFun.mService?.registerStatusCallback(mCallback)
                Log.e("open vpn mService", "mService onServiceConnected")
            } catch (e: Exception) {
                Log.e("open vpn error", e.message.toString())
            }
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            Log.e("open vpn mService", "mService onServiceDisconnected")
            mainFun.mService = null
        }
    }
    private val mCallback = object : IOpenVPNStatusCallback.Stub() {
        override fun newStatus(uuid: String?, state: String?, message: String?, level: String?) {
            // NOPROCESS 未连接 // CONNECTED 已连接
            // RECONNECTING 尝试重新链接 // EXITING 连接中主动掉用断开
            Log.e(
                TAG,
                "newStatus: state=$state;message=$message;agreement=${DualContext.localStorage.connection_mode}"
            )
            if (DualContext.localStorage.connection_mode != "1") {
                return
            }
            when (state) {
                "CONNECTED" -> {
                    App.vpnLink = true
                    mainFun.connectOrDisconnectDual(this@MainActivity, true)
                    mainFun.changeState(
                        state = BaseService.State.Idle,
                        this@MainActivity
                    )
//                    binding.serviceState = "2"
                    handleDualTimerLock()
                }

                "RECONNECTING", "EXITING", "CONNECTRETRY" -> {
                    mainFun.mService?.disconnect()
                }

                "NOPROCESS" -> {
                    mainFun.mService?.disconnect()
                    App.vpnLink = false
                    mainFun.connectOrDisconnectDual(this@MainActivity, true)
                    mainFun.changeState(state = BaseService.State.Idle, this@MainActivity)
                    handleDualTimerLock()
                }


                else -> {}
            }

        }

    }


    override fun onStart() {
        super.onStart()
        mainFun.connection.bandwidthTimeout = 500
    }

    override fun onResume() {
        super.onResume()
        handleWarmBoot()
    }

    private fun handleWarmBoot() {
    }

    private fun handleDualTimerLock() {
        if (App.vpnLink) {
            binding.showGuide = false
            mainFun.changeOfVpnStatus(this, "2")
            if (binding.tvTime.text.toString() == "00:00:00") {
//                DualSjHelp.startTiming()
            }
        } else {
            mainFun.changeOfVpnStatus(this, "0")
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mainFun.connection.bandwidthTimeout = 0
        mainFun.stopOperate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        DataStore.publicStore.unregisterChangeListener(this)
        mainFun.connection.disconnect(this)
        App.isBoot = false
        TimerObservers.removeObserver { timeString ->
            runOnUiThread {
                binding.tvTime.text = timeString
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x567) {
            App.isBoot = false
        }
        if (requestCode == 0x567 && mainFun.whetherRefreshServer) {
            mainFun.setFastInformation(mainFun.afterDisconnectionServerData, binding)
            val serviceData = Gson().toJson(mainFun.afterDisconnectionServerData)
            DualContext.localStorage.check_service = serviceData
            mainFun.currentServerData = mainFun.afterDisconnectionServerData
        }
        if (requestCode == 567) {
            when (App.serviceState) {
                "disconnect" -> {
                    mainFun.updateSkServer(false)
                }

                "connect" -> {
                    mainFun.updateSkServer(true)
                }
            }
            App.serviceState = "mo"
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.showGuide == true || binding.dlMain.isOpen) {
                binding.showGuide = false
                binding.dlMain.close()
            } else {
                mainFun.clickChange(this, nextFun = {
                    finish()
                })
            }
        }
        return true
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        App.vpnLink = state.canStop
        mainFun.changeState(state, this)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        setSsVpnState(state.canStop)
    }

    private fun setSsVpnState(canStop: Boolean) {
        if (DualContext.localStorage.connection_mode != "1") {
            App.vpnLink = canStop
            handleDualTimerLock()
        }
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                mainFun.connection.disconnect(this)
                mainFun.connection.connect(this, this)
            }
        }
    }

}