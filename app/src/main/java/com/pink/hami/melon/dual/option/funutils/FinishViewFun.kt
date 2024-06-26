package com.pink.hami.melon.dual.option.funutils

import android.util.Log
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish.FinishActivity

object FinishViewFun {
    fun returnToHomePage(activity: FinishActivity) {
        if (App.adManagerBack.hasAdDataForPosition()) {
            App.adManagerBack.showAd(activity) {
                activity.finish()
            }
        } else {
            activity.finish()
        }
    }
}