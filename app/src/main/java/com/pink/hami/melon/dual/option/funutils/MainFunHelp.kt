package com.pink.hami.melon.dual.option.funutils

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.pink.hami.melon.dual.option.databinding.ActivityMainBinding
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish.FinishActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.list.ListActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils
import com.pink.hami.melon.dual.option.utils.DulaShowDataUtils.getDualImage
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pink.hami.melon.dual.option.utils.AppData
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import com.pink.hami.melon.dual.option.utils.FileStorageManager
import com.pink.hami.melon.dual.option.utils.TimerManager
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class MainFunHelp {
    val connection = ShadowsocksConnection(true)
    var whetherRefreshServer = false
    var jobStartDual: Job? = null
    var jobConnectTTD: Job? = null

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
    var nowClickState: String = "1"

    var mService: IOpenVPNAPIService? = null
    lateinit var requestPermissionForResultVPN: ActivityResultLauncher<Intent?>

    private fun getSpeedData(activity: MainActivity) {
        val fff = FileStorageManager(activity)
        activity.lifecycleScope.launch {
            while (isActive) {
                val data = fff.loadData()
                val bean = if (data != null) {
                    Gson().fromJson(data, AppData::class.java)
                } else {
                    null
                }

                if (bean != null) {
                    activity.binding.tvDow.text = bean.dual_sp_dow ?: "0/kb"
                    activity.binding.tvUp.text = bean.dual_sp_up ?: "0/kb"
                } else {
                    activity.binding.tvDow.text = "0/kb"
                    activity.binding.tvUp.text = "0/kb"
                }

                delay(500)
            }
        }
    }



    fun initData(activity: MainActivity, call: ShadowsocksConnection.Callback) {
        initializeActivityState(activity, call)
        DataStore.publicStore.registerChangeListener(activity)

        if (DualContext.localStorage.check_service.isEmpty()) {
            initServerData()
        } else {
            val serviceData = parseServiceData(DualContext.localStorage.check_service)
            val currentServerData = getCurrentServerData(serviceData)
            updateUiWithServerData(currentServerData, activity.binding)
        }
        fetchSpeedData(activity)
        getSpeedData(activity)
    }

    private fun initializeActivityState(
        activity: MainActivity,
        call: ShadowsocksConnection.Callback
    ) {
        connection.connect(activity, call)
    }

    private fun parseServiceData(serviceJson: String): VpnServiceBean {
        return Gson().fromJson(serviceJson, object : TypeToken<VpnServiceBean?>() {}.type)
    }

    private fun getCurrentServerData(serviceData: VpnServiceBean): VpnServiceBean {
        return if (serviceData.best_dualLoad) {
            DualContext.getFastVpn() ?: VpnServiceBean()
        } else {
            serviceData
        }
    }

    private fun updateUiWithServerData(serverData: VpnServiceBean, binding: ActivityMainBinding) {
        if (serverData.best_dualLoad) {
            binding.imgFlag.setImageResource("Fast Server".getDualImage())
            binding.tvCountryName.text = "Fast Server"
            binding.tvCountry.text = "·Fast Server·"
        } else {
            binding.imgFlag.setImageResource(serverData.country_name.getDualImage())
            binding.tvCountryName.text = "${serverData.country_name}-${serverData.city}"
            binding.tvCountry.text = "·${serverData.country_name}·"
        }
    }

    fun initServerData() {
        val bestData = fetchBestData() ?: return
        val profile = fetchOrCreateProfile(DataStore.profileId)
        updateProfileWithBestData(profile, bestData)
        configureCurrentServer(bestData)
        updateLocalStorage(bestData)
        liveInitializeServerData.postValue(bestData)
    }

    private fun fetchSpeedData(activity: MainActivity) {
        val fileStorageManager = FileStorageManager(activity)
        activity.lifecycleScope.launch {
            while (isActive) {
                updateSpeedData(fileStorageManager, activity)
                delay(500)
            }
        }
    }

    private fun updateSpeedData(
        fileStorageManager: FileStorageManager,
        activity: MainActivity
    ) {
        val json = fileStorageManager.loadData()
        if(!json.isNullOrBlank()){
            val appData = fileStorageManager.loadData()?.let { parseAppData(it) }
            activity.binding.tvDow.text = appData?.dual_sp_dow
            activity.binding.tvUp.text = appData?.dual_sp_up
        }
    }

    private fun parseAppData(json: String): AppData? {
        return try {
            Gson().fromJson(json, AppData::class.java)
        }catch (e:NullPointerException){
            null
        }
    }


    fun papgeAtVpnServices(activity: MainActivity) {
        activity.lifecycleScope.launch {
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            activity.binding.pbLoading.visibility = View.VISIBLE
            if (!DualContext.isHaveServeData()) {
                delay(2000)
            }
            activity.binding.pbLoading.visibility = View.INVISIBLE
            activity.serverListLauncher.launch(Intent(activity, ListActivity::class.java))
        }
    }

    fun haveSmService(meteorVpnBean: VpnServiceBean, binding: ActivityMainBinding) {
        if (meteorVpnBean.best_dualLoad) {
            binding.imgFlag.setImageResource("Fast Server".getDualImage())
            binding.tvCountryName.text = "Fast Server"
            binding.tvCountry.text = "·Fast Server·"
        } else {
            binding.imgFlag.setImageResource(meteorVpnBean.country_name.getDualImage())
            binding.tvCountryName.text = meteorVpnBean.country_name + "-" + meteorVpnBean.city
            binding.tvCountry.text = "·" + meteorVpnBean.country_name + "·"
        }
    }

    fun startTheJudgment(activity: AppCompatActivity) {
        startVpn(activity)
    }

    @SuppressLint("LogNotTimber")
    private fun startVpn(activity: AppCompatActivity) {
        jobStartDual?.cancel()
        jobStartDual = null
        jobStartDual = activity.lifecycleScope.launch {
            nowClickState = if (App.vpnLink) {
                "2"
            } else {
                "0"
            }
            setTypeService(activity as MainActivity, 1)
            delay(2000)
            if(!isActive){
                return@launch
            }
            if (App.vpnLink) {
                showConnectAd(activity)
            } else {
                ljVPn(activity)
            }
        }
    }

    private fun showFinishAd(activity: MainActivity) {
        if (nowClickState == "0") {
            connectFinish(activity)
        }
        if (nowClickState == "2") {
            disConnectFinish(activity)
        }
    }

    fun showConnectAd(activity: MainActivity) {
        jobConnectTTD?.cancel()
        jobConnectTTD = null
        jobConnectTTD = activity.lifecycleScope.launch {
            if (App.adManagerConnect.canShowAd() == 0) {
                showFinishAd(activity)
                return@launch
            }
            val startTime = System.currentTimeMillis()
            var elapsedTime: Long
            try {
                while (isActive) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime >= 10000L) {
                        Log.e("TAG", "连接超时")
                        showFinishAd(activity)
                        break
                    }

                    if (App.adManagerConnect.canShowAd() == 1) {
                        App.adManagerConnect.showAd(activity) {
                            showFinishAd(activity)
                        }
                        break
                    }
                    delay(500L)
                }
            } catch (e: Exception) {
                showFinishAd(activity)
            }
        }
        homeLoadAd()
    }

    private fun homeLoadAd() {
        App.adManagerConnect.loadAd()
        App.adManagerBack.loadAd()
        App.adManagerEnd.loadAd()
    }

    private fun ljVPn(activity: MainActivity) {
        DualONlineFun.emitPointData("v9proxy")
        if (activity.binding.agreement == "1") {
            mService?.let {
                setOpenData(activity, it)
            }
            Core.stopService()
        } else {
            mService?.disconnect()
            Core.startService()
        }
    }

    private fun connectFinish(activity: MainActivity) {
        activity.binding.showGuide = false
        setTypeService(activity, 2)
        pageToRePage(activity, true)
    }

    private fun disConnectFinish(activity: MainActivity) {
        Log.e("TAG", "关闭广告断开vpn: ")
        mService?.disconnect()
        Core.stopService()
        setTypeService(activity, 0)
        pageToRePage(activity, false)
    }

    private fun pageToRePage(activity: MainActivity, isConnect: Boolean) {
        activity.lifecycleScope.launch {
            delay(300L)
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            val bundle = Bundle().apply {
                putString(DualContext.cuDualConnected, DualContext.localStorage.check_service)
                putBoolean(DualContext.isDualConnected, isConnect)
            }
            val intent = Intent(activity, FinishActivity::class.java).apply {
                putExtras(bundle)
            }
            activity.finishPageLauncher.launch(intent)
        }
    }

    fun setTypeService(
        activity: MainActivity,
        stateInt: Int
    ) {
        val binding = activity.binding
        binding.serviceState = stateInt
        if (stateInt == 0) {
            TimerManager.resetTimer()
            binding.llConnect.background =
                activity.resources.getDrawable(R.drawable.bg_connect_op)
            binding.imgConnect.setImageResource(R.drawable.ic_connect_1)
            binding.lavConnect.visibility = View.INVISIBLE
            binding.imgConnect.visibility = View.VISIBLE
            binding.imgHeart1.visibility = View.VISIBLE
            binding.imgHeart2.visibility = View.INVISIBLE
            binding.imgLoading.visibility = View.INVISIBLE
            binding.tvState.text = "Disconnected"
        }

        if (stateInt == 1) {
            binding.llConnect.background =
                activity.resources.getDrawable(R.drawable.bg_connect_op_2)
            binding.lavConnect.visibility = View.VISIBLE
            binding.imgConnect.visibility = View.INVISIBLE
            binding.imgHeart1.visibility = View.INVISIBLE
            binding.imgHeart2.visibility = View.INVISIBLE
            binding.imgLoading.visibility = View.VISIBLE
            if (nowClickState == "2") {
                binding.tvState.text = "Disconnecting"
            } else {
                binding.tvState.text = "Connecting"
            }
        }

        if (stateInt == 2) {
            TimerManager.startTimer()
            binding.llConnect.background =
                activity.resources.getDrawable(R.drawable.bg_connect_op_2)
            binding.imgConnect.setImageResource(R.drawable.ic_connect_2)
            binding.lavConnect.visibility = View.INVISIBLE
            binding.imgConnect.visibility = View.VISIBLE
            binding.imgHeart1.visibility = View.INVISIBLE
            binding.imgHeart2.visibility = View.VISIBLE
            binding.imgLoading.visibility = View.INVISIBLE
            binding.tvState.text = "Connected"
        }
        if (stateInt != 0 && stateInt != 1 && stateInt != 2) {
            binding.imgConnect.setImageResource(R.drawable.ic_connect_1)
            binding.lavConnect.visibility = View.INVISIBLE
            binding.imgConnect.visibility = View.VISIBLE
            binding.imgHeart1.visibility = View.VISIBLE
            binding.imgHeart2.visibility = View.INVISIBLE
            binding.imgLoading.visibility = View.INVISIBLE
        }
    }


    fun fetchBestData(): VpnServiceBean? {
        return DualContext.getFastVpn()
    }

    fun fetchOrCreateProfile(profileId: Long): Profile {
        return ProfileManager.getProfile(profileId) ?: Profile().also {
            ProfileManager.createProfile(it)
        }
    }

    fun updateProfileWithBestData(profile: Profile, bestData: VpnServiceBean) {
        ProfileManager.updateProfile(DulaShowDataUtils.setSkServerData(profile, bestData))
        DataStore.profileId = 1L
    }

    fun configureCurrentServer(bestData: VpnServiceBean) {
        bestData.best_dualLoad = true
        currentServerData = bestData
    }

    fun updateLocalStorage(bestData: VpnServiceBean) {
        val serviceData = Gson().toJson(currentServerData)
        DualContext.localStorage.check_service = serviceData
    }

    fun updateSkServer(isConnect: Boolean) {
        val skVpnServiceBean = fetchOrCreateServiceData()
        val profile = fetchOrCreateProfile(DataStore.profileId)
        updateProfileWithBestData(profile, skVpnServiceBean)

        if (isConnect) {
            handleConnection(skVpnServiceBean)
        } else {
            handleDisconnection(skVpnServiceBean)
        }
    }

    fun fetchOrCreateServiceData(): VpnServiceBean {
        val serviceData = Gson().fromJson<VpnServiceBean>(
            DualContext.localStorage.check_service,
            object : TypeToken<VpnServiceBean?>() {}.type
        )
        return if (serviceData?.best_dualLoad == true) {
            DualContext.getFastVpn() ?: VpnServiceBean()
        } else {
            serviceData ?: VpnServiceBean()
        }
    }

    fun handleConnection(skVpnServiceBean: VpnServiceBean) {
        afterDisconnectionServerData = skVpnServiceBean
        liveUpdateServerData.postValue(skVpnServiceBean)
    }

    fun handleDisconnection(skVpnServiceBean: VpnServiceBean) {
        currentServerData = skVpnServiceBean
        updateLocalStorage(skVpnServiceBean)
        liveNoUpdateServerData.postValue(skVpnServiceBean)
    }

    private fun setOpenData(activity: MainActivity, server: IOpenVPNAPIService) {
        val data = fetchVpnServiceData()
        updateLocalStorageMy(data)
        configureAndStartVPN(activity, server, data)
    }

    private fun fetchVpnServiceData(): VpnServiceBean? {
        return if (DualContext.localStorage.check_service.isEmpty()) {
            DualContext.getAllVpnListData()?.firstOrNull()
        } else {
            Gson().fromJson(
                DualContext.localStorage.check_service,
                object : TypeToken<VpnServiceBean?>() {}.type
            )
        }
    }

    private fun updateLocalStorageMy(data: VpnServiceBean?) {
        DualContext.localStorage.vpn_city = data?.city ?: ""
        DualContext.localStorage.vpn_ip_dualLoad = data?.ip ?: ""
    }

    private fun configureAndStartVPN(
        activity: MainActivity,
        server: IOpenVPNAPIService,
        data: VpnServiceBean?
    ) {
        runCatching {
            val config = buildVpnConfig(activity, data?.ip)
            server.startVPN(config)
        }.onFailure { exception ->
        }
    }

    private fun buildVpnConfig(activity: MainActivity, ip: String?): String {
        val config = StringBuilder()
        var inputStream: InputStream? = null
        var reader: BufferedReader? = null

        try {
            inputStream = activity.assets.open("fast_ippooltest.ovpn")
            reader = BufferedReader(InputStreamReader(inputStream))

            reader.forEachLine { line ->
                config.append(
                    if (line.contains("remote 103", true)) {
                        "remote $ip 443"
                    } else {
                        line
                    }
                ).append("\n")
            }
        } catch (e: IOException) {
            Log.e("TAG", "Error reading config file: ${e.message}")
        } finally {
            try {
                reader?.close()
            } catch (e: IOException) {
                Log.e("TAG", "Error closing reader: ${e.message}")
            }
            try {
                inputStream?.close()
            } catch (e: IOException) {
                Log.e("TAG", "Error closing inputStream: ${e.message}")
            }
        }

        return config.toString()
    }


    fun isConnectGuo(activity: MainActivity): Boolean {
        return !(nowClickState == "0" && activity.binding.serviceState == 1)
    }

    fun clickDisConnect(activity: MainActivity, nextFun: () -> Unit) {
        if (nowClickState == "2" && activity.binding.serviceState == 1) {
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
        jobStartDual?.cancel()
        jobStartDual = null
        jobConnectTTD?.cancel()
        jobConnectTTD = null
        if (App.vpnLink) {
            setTypeService(activity, 2)
        } else {
            mService?.disconnect()
            Core.stopService()
            setTypeService(activity, 0)
        }
    }

}