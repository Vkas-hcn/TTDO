package com.pink.hami.melon.dual.option.model

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.github.shadowsocks.AppData
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityMainBinding
import com.pink.hami.melon.dual.option.ui.finish.FinishActivity
import com.pink.hami.melon.dual.option.ui.list.ListActivity
import com.pink.hami.melon.dual.option.ui.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils.getSmileImage
import com.pink.hami.melon.dual.option.utils.TimeUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.utils.FileStorageManager
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.system.exitProcess

class MainViewModel : ViewModel() {
    lateinit var timeUtils: TimeUtils
    val connection = ShadowsocksConnection(true)
    var whetherRefreshServer = false
    var jobStartSmile: Job? = null

    var nowClickState: Int = 1

    var mService: IOpenVPNAPIService? = null
    lateinit var requestPermissionForResultVPN: ActivityResultLauncher<Intent?>

    companion object {
        var stateListener: ((BaseService.State) -> Unit)? = null
    }


    fun initData(
        activity: MainActivity,
        call: ShadowsocksConnection.Callback
    ) {
        val binding = activity.binding
        changeState(BaseService.State.Idle, activity)
        connection.connect(activity, call)
        DataStore.publicStore.registerChangeListener(activity)
        if (DualContext.localStorage.check_service.isEmpty()) {
            initServerData()
        } else {
            val serviceData = DualContext.localStorage.check_service
            val currentServerData: VpnServiceBean =
                Gson().fromJson(serviceData, object : TypeToken<VpnServiceBean?>() {}.type)
            setFastInformation(currentServerData, binding)
        }
        getSpeedData(activity)
    }

    private fun getSpeedData(activity: MainActivity) {
        val fff = FileStorageManager(activity)
        activity.lifecycleScope.launch {
            while (isActive) {
                val bean = Gson().fromJson(fff.loadData(), AppData::class.java)
                activity.binding.tvDow.text = bean.dual_sp_dow
                activity.binding.tvUp.text = bean.dual_sp_up
                delay(500)
            }
        }
    }

    fun changeState(
        state: BaseService.State = BaseService.State.Idle,
        activity: MainActivity,
        vpnLink: Boolean = false,
    ) {
        connectionStatusJudgment(state.canStop, activity)
        stateListener?.invoke(state)
    }

    fun jumpToServerList(activity: MainActivity) {
        activity.lifecycleScope.launch {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            activity.binding.pbLoading.visibility = View.VISIBLE
            if (!DualContext.isHaveServeData(activity)) {
                delay(2000)
            }
            activity.binding.pbLoading.visibility = View.INVISIBLE
            activity.launchActivityForResult<ListActivity>(null, 567)
        }
    }

    fun setFastInformation(meteorVpnBean: VpnServiceBean, binding: ActivityMainBinding) {
        if (meteorVpnBean.best_dualLoad) {
            binding.imgFlag.setImageResource("Fast Server".getSmileImage())
        } else {
            binding.imgFlag.setImageResource(meteorVpnBean.country_name.getSmileImage())
        }
    }

    fun startTheJudgment(activity: AppCompatActivity) {
        startVpn(activity)
    }

    private fun startVpn(activity: AppCompatActivity) {
        jobStartSmile = activity.lifecycleScope.launch {
            nowClickState = if (App.vpnLink) {
                2
            } else {
                0
            }
            changeOfVpnStatus(activity as MainActivity, "1")
            connectVpn(activity)
        }
    }

    private suspend fun connectVpn(activity: MainActivity) {
        if (!App.vpnLink) {
            if (activity.binding.agreement == "1") {
                mService?.let {
                    setOpenData(activity, it)
                }
                Core.stopService()
            } else {
                delay(2000)
                mService?.disconnect()
                Core.startService()
            }
        } else {
            delay(2000)
            connectOrDisconnectSmile(activity, activity.binding.agreement == "1")
        }
    }


    fun disconnectVpn() {
        if (App.vpnLink) {
            Core.stopService()
        }
    }

    fun connectOrDisconnectSmile(activity: MainActivity, isOpenJump: Boolean = false) {
        if (nowClickState == 2) {
            mService?.disconnect()
            disconnectVpn()
            changeOfVpnStatus(activity, "0")
            if (!App.isBackDataSmile) {
                jumpToFinishPage(activity, false)
            }
        }
        if (nowClickState == 0) {
            if (!App.vpnLink) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(activity, "The connection failed", Toast.LENGTH_SHORT).show()
                }
                return
            }
            if (!isOpenJump) {
                if (activity.binding.agreement == "1") {
                    return
                }
            }
            if (!App.isBackDataSmile) {
                jumpToFinishPage(activity, true)
            }
            changeOfVpnStatus(activity, "2")
        }

    }

    private fun jumpToFinishPage(activity: MainActivity, isConnect: Boolean) {
        activity.lifecycleScope.launch {
            delay(300L)
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            val bundle = Bundle()
            bundle.putString(DualContext.cuSmileConnected, DualContext.localStorage.check_service)
            bundle.putBoolean(DualContext.isSmileConnected, isConnect)
            val intent = Intent(activity, FinishActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, 0x567)
        }

    }

    private fun connectionStatusJudgment(
        vpnLink: Boolean,
        activity: MainActivity
    ) {
        if (vpnLink) {
            changeOfVpnStatus(activity, "2")
            connectOrDisconnectSmile(activity)
        } else {
            changeOfVpnStatus(activity, "0")

        }

    }

    fun changeOfVpnStatus(
        activity: MainActivity,
        stateInt: String
    ) {
        val binding = activity.binding
        binding.serviceState = stateInt
        Log.e(TAG, "changeOfVpnStatus: ${stateInt}")
        when (stateInt) {
            "0" -> {
                timeUtils.endTiming()
                binding.llConnect.background =
                    activity.resources.getDrawable(R.drawable.bg_connect_op)
                binding.imgConnect.setImageResource(R.drawable.ic_connect_1)
                binding.lavConnect.visibility = View.INVISIBLE
                binding.imgConnect.visibility = View.VISIBLE
                binding.imgHeart1.visibility = View.VISIBLE
                binding.imgHeart2.visibility = View.INVISIBLE
                binding.imgLoading.visibility = View.INVISIBLE
            }

            "1" -> {
                binding.llConnect.background =
                    activity.resources.getDrawable(R.drawable.bg_connect_op_2)
                binding.lavConnect.visibility = View.VISIBLE
                binding.imgConnect.visibility = View.INVISIBLE
                binding.imgHeart1.visibility = View.INVISIBLE
                binding.imgHeart2.visibility = View.INVISIBLE
                binding.imgLoading.visibility = View.VISIBLE
            }

            "2" -> {
                timeUtils.startTiming()
                binding.llConnect.background =
                    activity.resources.getDrawable(R.drawable.bg_connect_op_2)
                binding.imgConnect.setImageResource(R.drawable.ic_connect_2)
                binding.lavConnect.visibility = View.INVISIBLE
                binding.imgConnect.visibility = View.VISIBLE
                binding.imgHeart1.visibility = View.INVISIBLE
                binding.imgHeart2.visibility = View.VISIBLE
                binding.imgLoading.visibility = View.INVISIBLE
            }

            else -> {
                timeUtils.endTiming()
                binding.imgConnect.setImageResource(R.drawable.ic_connect_1)
                binding.lavConnect.visibility = View.INVISIBLE
                binding.imgConnect.visibility = View.VISIBLE
                binding.imgHeart1.visibility = View.VISIBLE
                binding.imgHeart2.visibility = View.INVISIBLE
                binding.imgLoading.visibility = View.INVISIBLE
            }
        }
    }


    val liveInitializeServerData: MutableLiveData<VpnServiceBean> by lazy {
        MutableLiveData<VpnServiceBean>()
    }

    val liveNoUpdateServerData: MutableLiveData<VpnServiceBean> by lazy {
        MutableLiveData<VpnServiceBean>()
    }

    val liveUpdateServerData: MutableLiveData<VpnServiceBean?> by lazy {
        MutableLiveData<VpnServiceBean?>()
    }

    var currentServerData: VpnServiceBean = VpnServiceBean()

    var afterDisconnectionServerData: VpnServiceBean = VpnServiceBean()

    fun initServerData() {
        val bestData = DualContext.getFastVpn() ?: return
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                ProfileManager.updateProfile(DulaShowDataUtils.setSkServerData(it, bestData))
            } else {
                val profile = Profile()
                ProfileManager.createProfile(DulaShowDataUtils.setSkServerData(profile, bestData))
            }
        }
        bestData.best_dualLoad = true
        DataStore.profileId = 1L
        currentServerData = bestData
        val serviceData = Gson().toJson(currentServerData)
        DualContext.localStorage.check_service = serviceData
        liveInitializeServerData.postValue(bestData)
    }

    fun updateSkServer(isConnect: Boolean) {
        val skVpnServiceBean = Gson().fromJson<VpnServiceBean>(
            DualContext.localStorage.check_service,
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                DulaShowDataUtils.setSkServerData(it, skVpnServiceBean)
                ProfileManager.updateProfile(it)
            } else {
                ProfileManager.createProfile(Profile())
            }
        }
        DataStore.profileId = 1L
        if (isConnect) {
            afterDisconnectionServerData = skVpnServiceBean
            liveUpdateServerData.postValue(skVpnServiceBean)
        } else {
            currentServerData = skVpnServiceBean
            val serviceData = Gson().toJson(currentServerData)
            DualContext.localStorage.check_service = serviceData
            liveNoUpdateServerData.postValue(skVpnServiceBean)
        }
    }

    fun setOpenData(activity: MainActivity, server: IOpenVPNAPIService): Job {
        return MainScope().launch(Dispatchers.IO) {
            val data = DualContext.localStorage.check_service.isEmpty().let {
                if (it) {
                    DualContext.getAllVpnListData()?.firstOrNull()
                } else {
                    Gson().fromJson<VpnServiceBean>(
                        DualContext.localStorage.check_service,
                        object : TypeToken<VpnServiceBean?>() {}.type
                    )
                }
            }
            DualContext.localStorage.vpn_city = data?.city ?: ""
            DualContext.localStorage.vpn_ip_dualLoad = data?.ip ?: ""
            runCatching {
                val config = StringBuilder()
                activity.assets.open("fast_ippooltest.ovpn").use { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        reader.forEachLine { line ->
                            config.append(
                                when {
                                    line.contains(
                                        "remote 102",
                                        true
                                    ) -> "remote ${data?.ip} 443"

                                    else -> line
                                }
                            ).append("\n")
                        }
                    }
                }
                Log.e(TAG, "step2: =$config")
                server.startVPN(config.toString())
            }.onFailure { exception ->
                Log.e(TAG, "Error in step2: ${exception.message}")
            }
        }
    }

    fun startOpenVpn(activity: MainActivity) {
        val state = checkVPNPermission(activity)
        if (state) {
            startTheJudgment(activity)
        } else {
            VpnService.prepare(activity).let {
                requestPermissionForResultVPN.launch(it)
            }
        }
    }

    fun checkVPNPermission(activity: MainActivity): Boolean {
        VpnService.prepare(activity).let {
            return it == null
        }
    }

    fun isConnectGuo(activity: MainActivity): Boolean {
        return !(nowClickState == 0 && activity.binding.serviceState == "1")
    }

    fun clickDisConnect(activity: MainActivity, nextFun: () -> Unit) {
        if (nowClickState == 2 && activity.binding.serviceState == "1") {
            stopOperate(activity)
        } else {
            nextFun()
        }
    }

    fun clickChange(activity: MainActivity, nextFun: () -> Unit) {
        if (isConnectGuo(activity)) {
            nextFun()
        } else {
            Toast.makeText(
                activity,
                "VPN is connecting. Please try again later.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun stopOperate(activity: MainActivity) {
        connection.bandwidthTimeout = 0
        jobStartSmile?.cancel() // 取消执行方法的协程
        jobStartSmile = null
        if (App.vpnLink) {
            changeOfVpnStatus(activity, "2")
        } else {
            changeOfVpnStatus(activity, "0")
        }
    }

    fun isCanUser(activity: MainActivity): Int {
//        if (isIllegalIp()) {
//            displayCannotUsePopUpBoxes(activity)
//            return 0
//        }
        return 1
    }

    private fun isIllegalIp(): Boolean {
        val ipData = DualContext.localStorage.ip_gsd
        if (ipData.isEmpty()) {
            return isIllegalIp2()
        }

        return ipData == "IR" || ipData == "CN" ||
                ipData == "HK" || ipData == "MO"
    }

    private fun isIllegalIp2(): Boolean {
        val ipData = DualContext.localStorage.ip_gsd_oth
        val locale = Locale.getDefault()
        val language = locale.language
        if (ipData.isEmpty()) {
            return language == "zh" || language == "fa"
        }
        return ipData == "IR" || ipData == "CN" ||
                ipData == "HK" || ipData == "MO"
    }


    private fun displayCannotUsePopUpBoxes(context: MainActivity) {
        val dialogVpn: AlertDialog = AlertDialog.Builder(context)
            .setTitle("Tip")
            .setMessage("Due to the policy reason , this service is not available in your country")
            .setCancelable(false)
            .setPositiveButton("confirm") { dialog, _ ->
                dialog.dismiss()
                exitProcess(0)
            }.create()
        dialogVpn.setCancelable(false)
        dialogVpn.show()
        dialogVpn.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialogVpn.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

}