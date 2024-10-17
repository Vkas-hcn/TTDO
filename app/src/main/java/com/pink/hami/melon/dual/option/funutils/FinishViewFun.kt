package com.pink.hami.melon.dual.option.funutils

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish.FinishActivity
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object FinishViewFun {
    fun returnToHomePage(activity: FinishActivity) {
        if (App.adManagerBackResult.canShowAd() == 0) {
            activity.finish()
            return
        }
        activity.binding.dataLoading = true
        App.adManagerBackResult.loadAd()
        DualONlineFun.emitPointData("v21proxy")
         activity.lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            var elapsedTime: Long
            try {
                while (isActive) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime >= 4000L) {
                        Log.e("TAG", "连接超时")
                        activity.finish()
                        activity.binding.dataLoading = false
                        break
                    }

                    if (App.adManagerBackResult.canShowAd() == 1) {
                        App.adManagerBackResult.showAd(activity) {
                            activity.finish()
                            activity.binding.dataLoading = false
                        }
                        break
                    }
                    delay(500L)
                }
            } catch (e: Exception) {
                activity.finish()
                activity.binding.dataLoading = false
            }
        }
    }
}