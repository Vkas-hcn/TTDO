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
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityMainBinding
import com.pink.hami.melon.dual.option.ui.finish.FinishActivity
import com.pink.hami.melon.dual.option.ui.list.ListActivity
import com.pink.hami.melon.dual.option.ui.main.MainActivity
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.pink.hami.melon.dual.option.utils.SmileUtils
import com.pink.hami.melon.dual.option.utils.SmileUtils.getSmileImage
import com.pink.hami.melon.dual.option.utils.SmileUtils.isVisible
import com.pink.hami.melon.dual.option.utils.TimeUtils
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.app.adload.DualLoad
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.Locale
import kotlin.system.exitProcess

class MainViewModel : ViewModel() {
    val showConnectLive = MutableLiveData<Any>()
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
        if (SmileKey.check_service.isEmpty()) {
            initServerData()
        } else {
            val serviceData = SmileKey.check_service
            val currentServerData: VpnServiceBean =
                Gson().fromJson(serviceData, object : TypeToken<VpnServiceBean?>() {}.type)
            setFastInformation(currentServerData, binding)
        }
       getSpeedData(activity)

    }
    private fun getSpeedData(activity: MainActivity) {
        activity.lifecycleScope.launch {
            while (isActive) {
                activity.binding.tvDow.text = App.mmkvSmile.decodeString("speed_dow", "0 B")
                activity.binding.tvUp.text = App.mmkvSmile.decodeString("speed_up", "0 B")
                delay(500)
            }
        }
    }

    fun changeState(
        state: BaseService.State = BaseService.State.Idle,
        activity: AppCompatActivity,
        vpnLink: Boolean = false,
    ) {
        connectionStatusJudgment(vpnLink, activity)
        stateListener?.invoke(state)
    }

    fun jumpToServerList(activity: MainActivity) {
        activity.lifecycleScope.launch {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            activity.binding.pbLoading.visibility = View.VISIBLE
            if (!SmileKey.isHaveServeData(activity)) {
                delay(2000)
            }
            activity.binding.pbLoading.visibility = View.INVISIBLE
            DualLoad.loadOf(SmileKey.POS_BACK)
            activity.startActivityWithReFirst<ListActivity>(null, 567)
        }
    }

    fun setFastInformation(meteorVpnBean: VpnServiceBean, binding: ActivityMainBinding) {
        if (meteorVpnBean.best_smart) {
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
            loadSmileAdvertisements(activity)
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
        }
    }


    fun disconnectVpn() {
        if (App.vpnLink) {
            Core.stopService()
        }
    }


    private suspend fun loadSmileAdvertisements(activity: AppCompatActivity) {
        val contData = try {
            (SmileKey.getFlowJson().cont.toLong())*1000
        }catch (e:Exception){
            10000L
        }
        try {
            withTimeout(contData) {
                delay(2000L)
                while (isActive) {
                    if (DualLoad.resultOf(SmileKey.POS_CONNECT) != null) {
                        showConnectLive.value = DualLoad.resultOf(SmileKey.POS_CONNECT)
                        cancel()
                        jobStartSmile?.cancel()
                        jobStartSmile = null
                    }
                    delay(500L)
                }
            }
        } catch (e: TimeoutCancellationException) {
            connectOrDisconnectSmile(activity as MainActivity)
        }
    }

    fun showConnectFun(activity: MainActivity, it: Any) {
        DualLoad.showFullScreenOf(
            where = SmileKey.POS_CONNECT,
            context = activity,
            res = it,
            preload = true,
            onShowCompleted = {
                jobStartSmile?.cancel()
                jobStartSmile = null
                connectOrDisconnectSmile(activity, true)
            }
        )
    }

    fun connectOrDisconnectSmile(activity: MainActivity, isOpenJump: Boolean = false) {
        if (nowClickState == 2) {
            if(App.vpnLink){
                Toast.makeText(activity, "VPN is connecting. Please try again later.", Toast.LENGTH_SHORT).show()
                return
            }
            mService?.disconnect()
            disconnectVpn()
            changeOfVpnStatus(activity, "0")
            if (!App.isBackDataSmile) {
                jumpToFinishPage(activity, false)
            }
        }
        if (nowClickState == 0) {
            if(!App.vpnLink){
                Toast.makeText(activity, "The connection failed", Toast.LENGTH_SHORT).show()
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
            bundle.putString(SmileKey.cuSmileConnected, SmileKey.check_service)
            bundle.putBoolean(SmileKey.isSmileConnected, isConnect)
            val intent = Intent(activity, FinishActivity::class.java)
            intent.putExtras(bundle)
            activity.startActivityForResult(intent, 0x567)
        }

    }

    fun connectionStatusJudgment(
        vpnLink: Boolean,
        activity: AppCompatActivity
    ) {
        val binding = (activity as MainActivity).binding
        when (vpnLink) {
            true -> {
                // 连接成功
                connectionServerSuccessful(binding)
            }

            false -> {
                disconnectServerSuccessful(binding)
            }
        }
    }

    /**
     * 连接服务器成功
     */
    fun connectionServerSuccessful(binding: ActivityMainBinding) {
        binding.serviceState = "2"
    }

    /**
     * 断开服务器
     */
    fun disconnectServerSuccessful(binding: ActivityMainBinding) {


    }

    /**
     * vpn状态变化
     * 是否连接
     */
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
                binding.llConnect.background = activity.resources.getDrawable(R.drawable.bg_connect_op)
                binding.imgConnect.setImageResource(R.drawable.ic_connect_1)
                binding.lavConnect.visibility = View.INVISIBLE
                binding.imgConnect.visibility = View.VISIBLE
                binding.imgHeart1.visibility = View.VISIBLE
                binding.imgHeart2.visibility = View.INVISIBLE
                binding.imgLoading.visibility = View.INVISIBLE
            }

            "1" -> {
                binding.llConnect.background = activity.resources.getDrawable(R.drawable.bg_connect_op_2)
                binding.lavConnect.visibility = View.VISIBLE
                binding.imgConnect.visibility = View.INVISIBLE
                binding.imgHeart1.visibility = View.INVISIBLE
                binding.imgHeart2.visibility = View.INVISIBLE
                binding.imgLoading.visibility = View.VISIBLE
            }

            "2" -> {
                timeUtils.startTiming()
                binding.llConnect.background = activity.resources.getDrawable(R.drawable.bg_connect_op_2)
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

    fun showHomeAd(activity: MainActivity) {
        activity.showHomeJob?.cancel()
        activity.showHomeJob ==null
        activity.showHomeJob= activity.lifecycleScope.launch {
            delay(300)
            DualLoad.loadOf(SmileKey.POS_HOME)
            if (activity.isVisible() && !App.isBoot) {
                activity.binding.nativeAdView.visibility = View.INVISIBLE
                activity.binding.imgAdType.visibility = View.VISIBLE
                App.isBoot = true
                while (isActive) {
                    val adHomeData = DualLoad.resultOf(SmileKey.POS_HOME)
                    if (adHomeData != null) {
                        Log.e(TAG, "showHomeAd: ", )
                        activity.binding.nativeAdView.visibility = View.VISIBLE
                        DualLoad.showNativeOf(
                            where = SmileKey.POS_HOME,
                            nativeRoot = activity.binding.nativeAdView,
                            res = adHomeData,
                            preload = true,
                            onShowCompleted = {
                            }
                        )
                        activity.showHomeJob?.cancel()
                        activity.showHomeJob = null
                    }
                    delay(500)
                }
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
        val bestData = SmileKey.getFastVpn() ?: return
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                ProfileManager.updateProfile(SmileUtils.setSkServerData(it, bestData))
            } else {
                val profile = Profile()
                ProfileManager.createProfile(SmileUtils.setSkServerData(profile, bestData))
            }
        }
        bestData.best_smart = true
        DataStore.profileId = 1L
        currentServerData = bestData
        val serviceData = Gson().toJson(currentServerData)
        SmileKey.check_service = serviceData
        liveInitializeServerData.postValue(bestData)
    }

    fun updateSkServer(isConnect: Boolean) {
        val skVpnServiceBean = Gson().fromJson<VpnServiceBean>(
            SmileKey.check_service,
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        ProfileManager.getProfile(DataStore.profileId).let {
            if (it != null) {
                SmileUtils.setSkServerData(it, skVpnServiceBean)
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
            SmileKey.check_service = serviceData
            liveNoUpdateServerData.postValue(skVpnServiceBean)
        }
    }

    fun setOpenData(activity: MainActivity, server: IOpenVPNAPIService): Job {
        return MainScope().launch(Dispatchers.IO) {
            val data = SmileKey.check_service.isEmpty().let {
                if (it) {
                    SmileKey.getAllVpnListData()?.firstOrNull()
                } else {
                    Gson().fromJson<VpnServiceBean>(
                        SmileKey.check_service,
                        object : TypeToken<VpnServiceBean?>() {}.type
                    )
                }
            }
            SmileKey.vpn_city = data?.city ?: ""
            SmileKey.vpn_ip = data?.ip ?: ""
            runCatching {
                val config = StringBuilder()
                activity.assets.open("fast_bloomingvpn.ovpn").use { inputStream ->
                    inputStream.bufferedReader().use { reader ->
                        reader.forEachLine { line ->
                            config.append(
                                when {
                                    line.contains(
                                        "remote 103",
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
        val ipData = SmileKey.ip_gsd
        if (ipData.isEmpty()) {
            return isIllegalIp2()
        }

        return ipData == "IR" || ipData == "CN" ||
                ipData == "HK" || ipData == "MO"
    }

    private fun isIllegalIp2(): Boolean {
        val ipData = SmileKey.ip_gsd_oth
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