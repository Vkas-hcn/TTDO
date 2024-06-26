package com.pink.hami.melon.dual.option.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.webkit.WebView
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.first.FirstActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import com.github.shadowsocks.Core
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.pink.hami.melon.dual.option.app.adload.AdManager
import com.pink.hami.melon.dual.option.app.adload.GetAdData
import de.blinkt.openvpn.OPenSpUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class App : Application(), Application.ActivityLifecycleCallbacks {
    private var isInBackground = false
    private var lastBackgroundTime: Long = 0
    var adActivity: Activity? = null

    companion object {
        var TAG = "DualLoad"
        private lateinit var instance: App
        fun getAppContext() = instance
        var isBackDataDual = false
        var vpnLink = false
        var isBoot = false
        var serviceState: String = "-1"
        var isAppRunning = false
        lateinit var adManagerOpen: AdManager
        lateinit var adManagerHome: AdManager
        lateinit var adManagerEnd: AdManager
        lateinit var adManagerConnect: AdManager
        lateinit var adManagerBack: AdManager
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(this, MainActivity::class)
        OPenSpUtils.initOpenContext(this)
        registerActivityLifecycleCallbacks(this)
        this.registerActivityLifecycleCallbacks(this)
        iniApp()
        val myPid = Process.myPid()
        val activityManager =
            this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val processInfoList = activityManager.runningAppProcesses
        val packageName = this.packageName
        for (info in processInfoList) {
            if (info!!.pid == myPid && packageName == info.processName) {
                MobileAds.initialize(this) {}
                Firebase.initialize(this)
                FirebaseApp.initializeApp(this)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (this.packageName != getProcessName()) {
                        WebView.setDataDirectorySuffix(getProcessName())
                    }
                }
                haveRefDataChanging(this)
                adManagerOpen = AdManager.getInstance(this, GetAdData.AdWhere.GUIDE)
                adManagerHome = AdManager.getInstance(this, GetAdData.AdWhere.HOME)
                adManagerEnd = AdManager.getInstance(this, GetAdData.AdWhere.END)
                adManagerConnect = AdManager.getInstance(this, GetAdData.AdWhere.CONNECT)
                adManagerBack = AdManager.getInstance(this, GetAdData.AdWhere.BACK)
            }
        }
    }


    private fun isMainProcess(context: Context): Boolean {
        val pid = Process.myPid()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName
        for (appProcess in runningApps) {
            if (appProcess.pid == pid && packageName == appProcess.processName) {
                return true
            }
        }
        return false
    }


    private fun iniApp() {
        if (isMainProcess(this)) {
            instance = this
            val data = DualContext.localStorage.uuid_dualLoadile
            if (data.isEmpty()) {
                DualContext.localStorage.uuid_dualLoadile = UUID.randomUUID().toString()
            }
        }
    }


    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is AdActivity) {
            adActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (isInBackground) {
            isInBackground = false
            val currentTime = System.currentTimeMillis()
            val backgroundDuration = currentTime - lastBackgroundTime
            if (backgroundDuration > 3000) {
                restartApp(activity)
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {
        if (isAppInBackground(activity)) {
            isInBackground = true
            lastBackgroundTime = System.currentTimeMillis()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }


    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun isAppInBackground(activity: Activity): Boolean {
        val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (d in processInfo.pkgList) {
                    if (d == activity.packageName) {
                        return false
                    }
                }
            }
        }
        return true
    }

    private fun restartApp(activity: Activity) {
        if (adActivity != null) {
            adActivity?.finish()
        }
        val intent = Intent(activity, FirstActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity.finish()
    }

    private fun haveRefDataChanging(context: Context) {
        GlobalScope.launch {
            while (isActive) {
                if (DualContext.localStorage.ref_data.isEmpty()) {
                    haveRefDataChangingBean(context)
                } else {
                    cancel()
                }
                delay(6000)
            }
        }
    }

    private fun haveRefDataChangingBean(context: Context) {
        if (DualContext.localStorage.ref_data.isNotBlank()) {
            return
        }
//        DualContext.localStorage.ref_data = "gclid"
//        DualContext.localStorage.ref_data = "fb4a"
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    when (p0) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val installReferrer =
                                referrerClient.installReferrer.installReferrer ?: ""

                            DualContext.localStorage.ref_data= installReferrer

                            Log.e(TAG, "onInstallReferrerSetupFinished: ${installReferrer}", )
                        }
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }.onFailure { e ->
        }
    }
}