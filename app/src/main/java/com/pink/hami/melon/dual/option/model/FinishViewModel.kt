package com.pink.hami.melon.dual.option.model

import androidx.lifecycle.ViewModel
import com.pink.hami.melon.dual.option.ui.finish.FinishActivity

class FinishViewModel : ViewModel() {

    fun returnToHomePage(activity: FinishActivity) {
        activity.finish()
    }

}