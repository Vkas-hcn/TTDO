package com.pink.hami.melon.dual.option.bjfklieaf.fast.show.first

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.activity.addCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
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
        getFileBaseData()
        startNetworkTasks()
        observeViewModel()
        startCountdown()
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
            DualONlineFun.landingRemoteData(this@FirstActivity)
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
            try {
                withTimeout(10000L) {
                    while (isActive) {
                        if (App.adManagerOpen.canShowAd() ==0) {
                            onCountdownFinished()
                            break
                        }
                        if (App.adManagerOpen.canShowAd()==1) {
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
            if (!BuildConfig.DEBUG) {
                val auth = Firebase.remoteConfig
                auth.fetchAndActivate().addOnSuccessListener {
                    DualContext.localStorage.onlineAdBean = auth.getString(GetAdData.ad_key)
                    DualContext.localStorage.onlineRefBean = auth.getString(GetAdData.ref_key)
                    DualContext.localStorage.online_control_bean =
                        auth.getString(GetAdData.control_key)
                    isCa = true
                }
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
        showOpenAd()
        DualContext.localStorage.online_control_bean_core =  GetAdData.raoliu()
    }


}
