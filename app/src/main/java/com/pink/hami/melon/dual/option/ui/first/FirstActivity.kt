package com.pink.hami.melon.dual.option.ui.first

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.activity.addCallback
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.funutils.FirstFunHelp
import com.pink.hami.melon.dual.option.ui.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.databinding.ActivityFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstActivity : BaseActivity<ActivityFirstBinding>(R.layout.activity_first) {
    val toMainLive = MutableLiveData<String>()

    override fun initViewComponents() {
        setupBackPressedCallback()
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this) {
            // Custom back button behavior can be added here if needed
        }
    }

    override fun initializeData() {
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
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            updateProgressBar(animation.animatedValue as Int)
        }
        return animator
    }

    private fun setupAnimatorListeners(animator: ValueAnimator) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onCountdownFinished()
            }
        })
    }

    private fun updateProgressBar(progress: Int) {
        binding.progressBarStart.progress = progress
    }

    private fun onCountdownFinished() {
       toMainLive.postValue("main")
    }
}
