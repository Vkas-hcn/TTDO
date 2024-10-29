package com.pink.hami.melon.dual.option.bjfklieaf.fast.show.first

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.activity.addCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.pink.hami.melon.dual.option.BuildConfig
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.app.adload.GetAdData
import com.pink.hami.melon.dual.option.databinding.ActivityFirstBinding
import com.pink.hami.melon.dual.option.utils.DualContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class FirstActivity : BaseActivity<ActivityFirstBinding>(R.layout.activity_first) {
    val toMainLive = MutableLiveData<String>()


    private var jobOpenTdo: Job? = null
    private var fileBaseJob: Job? = null
    override fun initViewComponents() {
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this) {
        }
    }

    override fun initializeData() {
        haveRefDataChangingBean(this)
        updateUserOpinions()
        getFileBaseData()
        startNetworkTasks()
        observeViewModel()
        startCountdown()
        DualONlineFun.emitSessionData()
        DualONlineFun.emitPointData("v2proxy")
    }

    private fun observeViewModel() {
        toMainLive.observe(this) { navigateTo ->
            if (navigateTo == "main") {
                navigateToMainActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
        launchActivity<MainActivity>()
        finish()
    }

    private fun startNetworkTasks() {
        lifecycleScope.launch {
            performNetworkTasks()
        }
    }

    private suspend fun performNetworkTasks() {
        withContext(Dispatchers.IO) {
            DualONlineFun.getAdminData(this@FirstActivity)
            DualONlineFun.landingRemoteData()
            DualONlineFun.getLoadIp()
            DualONlineFun.getLoadOthIp()
            DualONlineFun.getBlackData(this@FirstActivity)
            DualONlineFun.getOnlyIp()
        }
    }

    private fun startCountdown() {
        val animator = createCountdownAnimator()
        setupAnimatorListeners(animator)
        animator.start()
    }

    private fun createCountdownAnimator(): ValueAnimator {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 14000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            updateProgressBar(animation.animatedValue as Int)
        }
        return animator
    }

    private fun setupAnimatorListeners(animator: ValueAnimator) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
            }
        })
    }

    private fun updateProgressBar(progress: Int) {
        binding.progressBarStart.progress = progress
    }

    private fun onCountdownFinished() {
        jobOpenTdo?.cancel()
        jobOpenTdo = null
        binding.progressBarStart.progress = 100
        toMainLive.postValue("main")
    }

    private fun showOpenAd() {
        jobOpenTdo?.cancel()
        jobOpenTdo = null
        jobOpenTdo = lifecycleScope.launch {
            delay(2000)
            try {
                withTimeout(10000L) {
                    while (isActive) {
                        if (App.adManagerOpen.canShowAd() == 0) {
                            onCountdownFinished()
                            break
                        }
                        if (App.adManagerOpen.canShowAd() == 1) {
                            App.adManagerOpen.showAd(this@FirstActivity) {
                                onCountdownFinished()
                            }
                            break
                        }
                        delay(500L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                onCountdownFinished()
            }
        }
    }

    private fun getFileBaseData() {
        fileBaseJob = lifecycleScope.launch {
            var isCa = false
            val auth = Firebase.remoteConfig
            auth.fetchAndActivate().addOnSuccessListener {
                DualContext.localStorage.onlineAdBean = auth.getString(GetAdData.ad_key)
                DualContext.localStorage.online_control_bean =
                    auth.getString(GetAdData.control_key)
                Log.e("TAG", "getFileBaseData: ${DualContext.localStorage.online_control_bean}", )
                initFaceBook()
                isCa = true
            }
            try {
                withTimeout(4000L) {
                    while (true) {
                        if (!isActive) {
                            break
                        }
                        if (isCa) {
                            getAdLoad()
                            cancel()
                            fileBaseJob = null
                        }
                        delay(500)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                cancel()
                fileBaseJob = null
                getAdLoad()
            }
        }
    }

    private fun getAdLoad() {
        App.adManagerOpen.loadAd()
        App.adManagerHome.loadAd()
        App.adManagerConnect.loadAd()
        wODFun()
        DualContext.localStorage.online_control_bean_core = GetAdData.raoliu()
    }

    private fun initFaceBook() {
        val data = GetAdData.getControlData().aaxxz?:""
        if (data.isBlank()) {
            return
        }
        Log.e("TAG", "initFaceBook: ${data}")
        FacebookSdk.setApplicationId(data)
        FacebookSdk.sdkInitialize(App.getAppContext())
        AppEventsLogger.activateApp(App.getAppContext())
    }

    private fun wODFun() {
        GlobalScope.launch {
            while (isActive) {
                if (DualContext.localStorage.cmpType) {
                    showOpenAd()
                    cancel()
                }
                delay(500)
            }
        }
    }

    private fun updateUserOpinions() {
        if (DualContext.localStorage.cmpType) {
            return
        }
        val debugSettings =
            ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("BCD99A19DFC84C1B71AF2A884D73059C")
                .build()
        val params = ConsentRequestParameters
            .Builder()
            .setConsentDebugSettings(debugSettings)
            .build()
        val consentInformation: ConsentInformation =
            UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) {
                    if (consentInformation.canRequestAds()) {
                        DualContext.localStorage.cmpType = true
                    }
                }
            },
            {
                DualContext.localStorage.cmpType = true
            }
        )
    }

    private fun haveRefDataChangingBean(context: Context) {
        runCatching {
            val timeStart = System.currentTimeMillis()
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    when (p0) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val installReferrer =
                                referrerClient.installReferrer.installReferrer ?: ""
                            DualContext.localStorage.ref_data = installReferrer
                            val timeEnd = ((System.currentTimeMillis() - timeStart) / 1000).toInt()
                            DualONlineFun.emitPointData("v25proxy", "time", timeEnd)
                            runCatching {
                                referrerClient?.installReferrer?.run {
                                    DualONlineFun.emitInstallData(context, this)
                                }
                            }.exceptionOrNull()
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
