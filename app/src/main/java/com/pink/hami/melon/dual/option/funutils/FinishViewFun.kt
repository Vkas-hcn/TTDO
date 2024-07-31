package com.pink.hami.melon.dual.option.funutils

import android.util.Log
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish.FinishActivity
import com.pink.hami.melon.dual.option.utils.DualONlineFun

object FinishViewFun {
    fun returnToHomePage(activity: FinishActivity) {
        DualONlineFun.emitPointData("v21proxy")
        if (App.adManagerBackResult.hasAdDataForPosition()) {
            App.adManagerBackResult.showAd(activity) {
                activity.finish()
            }
        } else {
            activity.finish()
        }
    }
}