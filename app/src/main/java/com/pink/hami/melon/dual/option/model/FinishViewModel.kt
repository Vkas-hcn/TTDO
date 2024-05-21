package com.pink.hami.melon.dual.option.model

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import com.pink.hami.melon.dual.option.app.adload.DualLoad
import com.pink.hami.melon.dual.option.ui.finish.FinishActivity
import com.pink.hami.melon.dual.option.utils.SmileKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FinishViewModel : ViewModel() {

    fun returnToHomePage(activity: FinishActivity) {
        val res = DualLoad.resultOf(SmileKey.POS_BACK)
        if (res == null) {
            activity.finish()
        } else {
            showBackFun(res, activity)
        }
    }

    fun showEndAd(activity: FinishActivity) {
        activity.lifecycleScope.launch {
            DualLoad.loadOf(SmileKey.POS_RESULT)
            delay(300)
            if (activity.lifecycle.currentState != Lifecycle.State.RESUMED) {
                return@launch
            }
            activity.binding.nativeAdView.visibility = android.view.View.GONE
            activity.binding.imgAdType.visibility = android.view.View.VISIBLE
            while (isActive) {
                val res = DualLoad.resultOf(SmileKey.POS_RESULT)
                if (res != null) {
                    Log.e(TAG, "showEndAd: 1")
                    activity.binding.nativeAdView.visibility = android.view.View.VISIBLE
                    showResultNativeAd(res, activity)
                    cancel()
                    break
                } else {
                    Log.e(TAG, "showEndAd: 2")
                }
                delay(500)
            }
        }
    }

    private fun showResultNativeAd(res: Any, activity: FinishActivity) {
        DualLoad.showNativeOf(
            where = SmileKey.POS_RESULT,
            nativeRoot = activity.binding.nativeAdView,
            res = res,
            preload = true,
            onShowCompleted = {
            }
        )
    }

    private fun showBackFun(it: Any, activity: FinishActivity) {
        DualLoad.showFullScreenOf(
            where = SmileKey.POS_BACK,
            context = activity,
            res = it,
            onShowCompleted = {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.finish()
                }
            }
        )
    }


}