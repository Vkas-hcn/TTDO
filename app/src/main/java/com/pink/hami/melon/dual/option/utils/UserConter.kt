package com.pink.hami.melon.dual.option.utils

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.pink.hami.melon.dual.option.app.App
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object UserConter {
    fun getReferInformation(context: Context) {
        GlobalScope.launch {
            while (isActive) {
                if (SmileKey.local_ref.isEmpty()) {
                    getReferrerData(context)
                } else {
                    cancel()
                }
                delay(6000)
            }
        }
    }

    private fun getReferrerData(context: Context) {
        var installReferrer = ""
        val referrer = SmileKey.local_ref
        if (referrer.isNotBlank()) {
            return
        }
//        installReferrer = "gclid"
//        installReferrer = "utm_source=(not%20set)&utm_medium=(not%20set)"
//        SmileKey.local_ref = installReferrer
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(context).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(p0: Int) {
                    when (p0) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val installReferrer =
                                referrerClient.installReferrer.installReferrer ?: ""
                            SmileKey.local_ref = installReferrer
                            referrerClient.installReferrer?.run {
                                SmileNetHelp.postInstallData(App.getAppContext(), this)
                            }
                        }
                    }
                    referrerClient.endConnection()
                }

                override fun onInstallReferrerServiceDisconnected() {
                }
            })
        }.onFailure { e ->
            // 处理异常
        }
    }

    private fun isFacebookUser(): Boolean {
        val data = SmileKey.getRefJson()
        val referrer = SmileKey.local_ref
        val pattern = "fb4a|facebook".toRegex(RegexOption.IGNORE_CASE)
        return (pattern.containsMatchIn(referrer) && data.fb4a == "1")
    }

    private fun isItABuyingUser(): Boolean {
        val data = SmileKey.getRefJson()
        val referrer = SmileKey.local_ref
        return isFacebookUser()
                || (data.gclid == "1" && referrer.contains("gclid", true))
                || (data.not == "1" && referrer.contains("not%20set", true))
                || (data.youtubeads == "1" && referrer.contains(
            "youtubeads",
            true
        ))
                || (data.qiB22 == "1" && referrer.contains("%7B%22", true))
                || (data.adjust == "1" && referrer.contains("adjust", true))
                || (data.bytedance == "1" && referrer.contains("bytedance", true))
                || (SmileKey.adjust_smile.isNotEmpty())
    }

    //屏蔽广告用户
    fun showAdCenter(): Boolean {
        return when (SmileKey.getFlowJson().shield) {
            "1" -> {
                true
            }

            "2" -> {
                isItABuyingUser()
            }

            "3" -> {
                false
            }

            else -> {
                true
            }
        }
    }

    //黑名单
    fun showAdBlacklist(): Boolean {
        val blackData = SmileKey.local_clock != "chintz"
        return when (SmileKey.getFlowJson().cloak) {
            "1" -> {
                !blackData
            }

            "2" -> {
                true
            }

            else -> {
                true
            }
        }
    }

    //是否扰流
    fun spoilerOrNot(): Boolean {
        when (SmileKey.getFlowJson().flowAround) {
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

}