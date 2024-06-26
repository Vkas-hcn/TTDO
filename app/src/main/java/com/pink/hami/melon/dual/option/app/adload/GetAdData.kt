package com.pink.hami.melon.dual.option.app.adload

import android.content.Context
import android.graphics.Outline
import android.util.Base64
import android.view.View
import android.view.ViewOutlineProvider
import com.google.gson.Gson
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.utils.DualContext
import com.pink.hami.melon.dual.option.utils.LocalStorage
import java.io.IOException

object GetAdData {
    enum class AdWhere {
        GUIDE,
        HOME,
        END,
        CONNECT,
        BACK
    }

    const val ad_key = "oee"
    const val ref_key = "mid"
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


    fun getRefData(): AdRefBean {
        val onlineRefBean = DualContext.localStorage.onlineRefBean
        val localRefBean = getJsonDataFromAsset(App.getAppContext(), "ref.json")
        runCatching {
            if (onlineRefBean.isNotEmpty()) {
                return Gson().fromJson(onlineRefBean, AdRefBean::class.java)
            } else {
                return Gson().fromJson(localRefBean, AdRefBean::class.java)
            }
        }.getOrNull() ?: return Gson().fromJson(localRefBean, AdRefBean::class.java)
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


    private fun isFacebookUser(): Boolean {
        val pattern = "fb4a|facebook".toRegex(RegexOption.IGNORE_CASE)
        return (pattern.containsMatchIn(DualContext.localStorage.ref_data) && getRefData().opposite == "1")
    }

    fun isItABuyingUser(): Boolean {
        return isFacebookUser()
                || (getRefData().gadzooks == "1" && DualContext.localStorage.ref_data.contains(
            "gclid",
            true
        ))
                || (getRefData().scarcely == "1" && DualContext.localStorage.ref_data.contains(
            "not%20set",
            true
        ))
                || (getRefData().bludgeon == "1" && DualContext.localStorage.ref_data.contains(
            "youtubeads",
            true
        ))
                || (getRefData().standard == "1" && DualContext.localStorage.ref_data.contains(
            "%7B%22",
            true
        ))
                || (getRefData().chaplain == "1" && DualContext.localStorage.ref_data.contains(
            "adjust",
            true
        ))
                || (getRefData().inasmuch == "1" && DualContext.localStorage.ref_data.contains(
            "bytedance",
            true
        ))
    }

    fun raoliu(): Boolean {
        when (getControlData().tear) {
            "1" -> {
                return true
            }

            "2" -> {
                return false
            }

            "3" -> {
                return !isItABuyingUser()
            }

            else -> {
                return false
            }
        }
    }
    fun refAdUsers(): Boolean {
        when (getControlData().geez) {
            "1" -> {
                return true
            }

            "2" -> {
                return isItABuyingUser()
            }

            "3" -> {
                return false
            }

            else -> {
                return true
            }
        }
    }
    fun getAdBlackData(): Boolean {
        return when (getControlData().deer) {
            "1" -> {
                DualContext.localStorage.local_clock == "psychic"
            }

            "2" -> {
                false
            }

            else -> {
                false
            }
        }
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