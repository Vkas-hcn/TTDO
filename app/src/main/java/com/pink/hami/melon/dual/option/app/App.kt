package com.pink.hami.melon.dual.option.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import com.pink.hami.melon.dual.option.ui.first.FirstActivity
import com.pink.hami.melon.dual.option.ui.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import com.github.shadowsocks.Core
import com.tencent.mmkv.MMKV
import java.util.UUID

class App : Application(), Application.ActivityLifecycleCallbacks {
    private var isInBackground = false
    private var lastBackgroundTime: Long = 0

    companion object {
        var TAG = "DualLoad"
        private lateinit var instance: App
        fun getAppContext() = instance
        var isBackDataDual = false
        var vpnLink = false
        var isBoot = false
        var serviceState: String = "mo"
        val mmkvDual by lazy {
            MMKV.mmkvWithID("DualLoad", MMKV.MULTI_PROCESS_MODE)
        }
        var isAppRunning = false
    }
    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        Core.init(this, MainActivity::class)
        registerActivityLifecycleCallbacks(this)
        iniApp()
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
        val intent = Intent(activity, FirstActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        activity.finish()
    }
}