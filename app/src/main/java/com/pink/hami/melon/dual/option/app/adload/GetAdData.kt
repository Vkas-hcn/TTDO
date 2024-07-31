package com.pink.hami.melon.dual.option.app.adload

import android.content.Context
import android.graphics.Outline
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.utils.DualContext
import com.pink.hami.melon.dual.option.utils.DualONlineFun
import com.pink.hami.melon.dual.option.utils.LocalStorage
import java.io.IOException

object GetAdData {
    enum class AdWhere {
        GUIDE,
        HOME,
        END,
        CONNECT,
        BACK_SERVICE,
        BACK_RESULT
    }

    const val ad_key = "oee"
    const val control_key = "pew"
    fun getAdData(): AdListBean {
        val onlineAdBean = DualContext.localStorage.onlineAdBean
        val localAdBean = getJsonDataFromAsset(App.getAppContext(), "ad.json")
        runCatching {
            if (onlineAdBean.isNotEmpty()) {
                return Gson().fromJson(base64Decode(onlineAdBean), AdListBean::class.java)
            } else {
                return Gson().fromJson(base64Decode(localAdBean!!), AdListBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(base64Decode(localAdBean!!), AdListBean::class.java)
    }

    //base64解密
    fun base64Decode(base64Str: String): String {
        return String(Base64.decode(base64Str, Base64.DEFAULT))
    }


    fun getControlData(): AdConBean {
        val onlineControlBean = DualContext.localStorage.online_control_bean
        val localControlBean = getJsonDataFromAsset(App.getAppContext(), "control.json")
        runCatching {
            if (onlineControlBean.isNotEmpty()) {
                return Gson().fromJson(onlineControlBean, AdConBean::class.java)
            } else {
                return Gson().fromJson(localControlBean, AdConBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localControlBean, AdConBean::class.java)
    }


    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }


    fun raoliu(): Boolean {
        when (getControlData().tear) {
            "1" -> {
                return true
            }

            "2" -> {
                return false
            }

            else -> {
                return false
            }
        }
    }

    fun getAdBlackData(): Boolean {
        val type = when (getControlData().deer) {
            "1" -> {
                DualContext.localStorage.local_clock != "stringy"
            }

            "2" -> {
                false
            }

            else -> {
                true
            }
        }
        if (type && !DualContext.localStorage.locak_up) {
            DualONlineFun.emitPointData("v1proxy")
            DualContext.localStorage.locak_up = true
        }
        return type
    }

    class getAdC : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            val sView = view ?: return
            val sOutline = outline ?: return
            sOutline.setRoundRect(
                0,
                0,
                sView.width,
                sView.height,
                8f
            )
        }
    }
}