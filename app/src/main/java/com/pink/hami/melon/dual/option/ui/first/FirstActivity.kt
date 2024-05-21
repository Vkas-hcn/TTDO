package com.pink.hami.melon.dual.option.ui.first

import android.view.KeyEvent
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.base.BaseActivity
import com.pink.hami.melon.dual.option.model.FirstViewModel
import com.pink.hami.melon.dual.option.ui.main.MainActivity
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.pink.hami.melon.dual.option.utils.SmileNetHelp
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.adload.DualLoad
import com.pink.hami.melon.dual.option.databinding.ActivityFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class FirstActivity : BaseActivity<ActivityFirstBinding, FirstViewModel>(
    R.layout.activity_first, FirstViewModel::class.java
) {
    override fun intiView() {

    }

    private var jobOpenSmile: Job? = null
    var count = 0

    override fun initData() {
        showOpenFun()
        countDown()

        viewModel.toMainLive.observe(this) {
            if (it == "main") {
                startActivityFirst<MainActivity>()
                finish()
            }
        }
        lifecycleScope.launch {
            SmileNetHelp.getOnlineSmData(this@FirstActivity)
            SmileNetHelp.getLoadIp()
            SmileNetHelp.getLoadOthIp()
            SmileNetHelp.getBlackData(this@FirstActivity)
            SmileNetHelp.getOnlyIp()
        }
        viewModel.getFileBaseData(this) {
            DualLoad.isLoadOpenFist = false
            DualLoad.init(this)
            loadOpenAd()
        }

    }

    private fun countDown() {
        lifecycleScope.launch {
            while (true) {
                count += 1
                binding.progressBarStart.progress = count
                if (count >= 100) {
                    cancel()
                    break
                }
                delay(120)
            }
        }
    }

    private fun loadOpenAd() {
        jobOpenSmile?.cancel()
        jobOpenSmile = null
        jobOpenSmile = lifecycleScope.launch {
            try {
                withTimeout(12000L) {
                    while (isActive) {
                        DualLoad.resultOf(SmileKey.POS_OPEN)?.let { res ->
                            viewModel.showOpen.value = res
                            cancel()
                            jobOpenSmile = null
                            count = 100
                            binding.progressBarStart.progress = count
                        }
                        delay(500L)
                    }
                }
            } catch (e: TimeoutCancellationException) {
                cancel()
                jobOpenSmile = null
                count = 100
                binding.progressBarStart.progress = count
                viewModel.toMainLive.postValue("main")
            }
        }
    }

    private fun showOpenFun() {
        viewModel.showOpen.observe(this) {
            DualLoad.showFullScreenOf(
                where = SmileKey.POS_OPEN,
                context = this,
                res = it,
                onShowCompleted = {
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.toMainLive.postValue("main")

                    }
                }
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return true
    }
}