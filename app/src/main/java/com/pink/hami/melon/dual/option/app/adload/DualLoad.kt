package com.pink.hami.melon.dual.option.app.adload

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.pink.hami.melon.dual.option.bean.EveryADBean
import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bean.AdvertiseEntity
import com.pink.hami.melon.dual.option.utils.SmileKey
import com.pink.hami.melon.dual.option.utils.UserConter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pink.hami.melon.dual.option.R
import timber.log.Timber

object DualLoad {
    val TAG = "DualLoad"
    var isLoadOpenFist = false
    var openAdData = EveryADBean()

    var connectAdData = EveryADBean()

    var backAdData = EveryADBean()
    var int3AdData = EveryADBean()
    var reWardeAdData = EveryADBean()

    fun init(context: Context) {
        GoogleAds.init(context) {
            preloadAds()
        }
    }

    fun loadOf(where: String) {
        Load.of(where)?.load()
    }

    fun resultOf(where: String): Any? {
        return Load.of(where)?.res
    }

    fun showFullScreenOf(
        where: String,
        context: AppCompatActivity,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: () -> Unit
    ) {

        Show.of(where)
            .showFullScreen(
                context = context,
                res = res,
                callback = {
                    Load.of(where)?.let { load ->
                        load.clearCache()
                        if (preload) {
                            load.load()
                        }
                    }
                    onShowCompleted()
                }
            )
    }

    fun showNativeOf(
        where: String,
        nativeRoot: View,
        res: Any,
        preload: Boolean = false,
        onShowCompleted: (() -> Unit)? = null
    ) {
        Show.of(where)
            .showNativeOf(
                nativeRoot = nativeRoot,
                res = res,
                callback = {
                    Load.of(where)?.let { load ->
                        load.clearCache()
                        if (preload) {
                            load.load()
                        }
                    }
                    onShowCompleted?.invoke()
                }
            )
    }

    private fun preloadAds() {
        runCatching {
            Load.of(SmileKey.POS_OPEN)?.load()
            Load.of(SmileKey.POS_CONNECT)?.load()
            Load.of(SmileKey.POS_HOME)?.load()
            Load.of(SmileKey.POS_RESULT)?.load()
        }
    }

    private class Load private constructor(private val where: String) {
        companion object {
            private val open by lazy { Load(SmileKey.POS_OPEN) }
            private val navHome by lazy { Load(SmileKey.POS_HOME) }
            private val navEnd by lazy { Load(SmileKey.POS_RESULT) }
            private val connect by lazy { Load(SmileKey.POS_CONNECT) }
            private val back by lazy { Load(SmileKey.POS_BACK) }

            fun of(where: String): Load? {
                return when (where) {
                    SmileKey.POS_OPEN -> open
                    SmileKey.POS_CONNECT -> connect
                    SmileKey.POS_BACK -> back
                    SmileKey.POS_HOME -> navHome
                    SmileKey.POS_RESULT -> navEnd
                    else -> null
                }
            }

        }


        private var createdTime = 0L
        var res: Any? = null
            set
        var isLoading = false
            set

        private fun printLog(content: String) {
            Log.d(TAG, "${where} ---${content}: ")
        }

        fun load(
            context: Context = App.getAppContext(),
            requestCount: Int = 1,
            inst: AdvertiseEntity = SmileKey.getAdJson(),
            isLoadType: Boolean = false
        ) {
            SmileKey.isAppGreenSameDayGreen()
            if (isLoading) {
                printLog("is requesting")
                return
            }

            val cache = res
            val cacheTime = createdTime
            if (cache != null) {
                if (cacheTime > 0L
                    && ((System.currentTimeMillis() - cacheTime) > (1000L * 60L * 60L))
                ) {
                    printLog("cache is expired")
                    Log.e(TAG, "load: clearCache")
                    clearCache()
                } else {
                    printLog("Existing cache")
                    return
                }
            }
            if ((cache == null || cache == "") && SmileKey.isThresholdReached()) {
                printLog("The ad reaches the go-live")
//                SmileNetHelp.postPotNet(context, "oom15", "oo", SmileKey.overrunType())
                res = ""
                return
            }

            if ((where == SmileKey.POS_BACK || where == SmileKey.POS_CONNECT) && !UserConter.showAdBlacklist()) {
                res = ""
                return
            }
            isLoading = true
            val listData = when (where) {
                SmileKey.POS_OPEN -> inst.start
                SmileKey.POS_HOME -> inst.home
                SmileKey.POS_RESULT -> inst.result
                SmileKey.POS_CONNECT -> inst.connect
                SmileKey.POS_BACK -> inst.back

                else -> emptyList()
            }
            val redListData = listData
            printLog("load started-data=${redListData}")
            doRequest(
                context, redListData!!
            ) {
                val isSuccessful = it != null
                printLog("load complete, result=$isSuccessful")
                if (isSuccessful) {
                    res = it
                    createdTime = System.currentTimeMillis()
                }
                isLoading = false
                if (!isSuccessful && where == SmileKey.POS_OPEN && requestCount < 2) {
                    load(context, requestCount + 1, inst)
                }
            }
        }

        fun sortArrayByWeight(items: MutableList<EveryADBean>): MutableList<EveryADBean> {
            items.sortBy { it.adWeightDual }
            return items
        }

        private fun doRequest(
            context: Context,
            units: List<EveryADBean>,
            startIndex: Int = 0,
            callback: ((result: Any?) -> Unit)
        ) {
            val unit = units.getOrNull(startIndex)
            if (unit == null) {
                callback(null)
                return
            }
            printLog("${where},on request: $unit")
            GoogleAds(where).load(context, unit) {
                if (it == null)
                    doRequest(context, units, startIndex + 1, callback)
                else
                    callback(it)
            }
        }

        fun clearCache() {
            res = null
            createdTime = 0L
        }
    }

    private class Show private constructor(private val where: String) {
        companion object {
            private var isShowingFullScreen = false

            fun of(where: String): Show {
                return Show(where)
            }

        }

        fun showFullScreen(
            context: AppCompatActivity,
            res: Any,
            callback: () -> Unit
        ) {
            if (isShowingFullScreen || context.lifecycle.currentState != Lifecycle.State.RESUMED) {
                callback()
                return
            }
            isShowingFullScreen = true
            Log.e(TAG, "showFullScreen: ")
            GoogleAds(where)
                .showFullScreen(
                    context = context,
                    res = res,
                    callback = {
                        isShowingFullScreen = false
                        callback()
                    }
                )
        }

        fun showNativeOf(
            nativeRoot: View,
            res: Any,
            callback: () -> Unit
        ) {
            GoogleAds(where)
                .showNativeOf(
                    nativeRoot = nativeRoot,
                    res = res,
                    callback = callback
                )
        }
    }

    private class GoogleAds(private val where: String) {
        private class GoogleFullScreenCallback(
            private val where: String,
            private val callback: () -> Unit
        ) : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "${where} ---dismissed")
                onAdComplete()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                Log.d(TAG, "${where} ---fail to show, message=${p0.message}")
                onAdComplete()
            }

            private fun onAdComplete() {
                callback()
            }

            override fun onAdShowedFullScreenContent() {
                SmileKey.recordNumberOfAdDisplaysGreen()
                Log.d(TAG, "${where}--showed")

            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "${where}插屏广告点击")
                SmileKey.recordNumberOfAdClickGreen()
            }
        }

        companion object {
            fun init(context: Context, onInitialized: () -> Unit) {
                MobileAds.initialize(context) {
                    onInitialized()
                }
            }

        }

        fun load(
            context: Context,
            unit: EveryADBean,
            callback: ((result: Any?) -> Unit)
        ) {
//            SmileNetHelp.postPotNet(
//                context,
//                "oom13",
//                "oo",
//                "${unit.name}+${unit.id}+${App.top_activity_name}",
//                "oo1",
//                App.vpnLink.toString()
//            )
            val requestContext = context.applicationContext
            when (unit.whereDual) {
                SmileKey.POS_OPEN -> {
//                    openAdData.id = unit.id
//                    openAdData.type = unit.type
//                    openAdData.name = unit.name
//                    openAdData = PutDataUtils.beforeLoadLink(openAdData)
                    AppOpenAd.load(
                        requestContext,
                        unit.adIdDual,
                        AdRequest.Builder().build(),
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                        object :
                            AppOpenAd.AppOpenAdLoadCallback() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                                Log.d(TAG, "${where} ---request success")

                                callback(appOpenAd)
//                                SmileNetHelp.postPotNet(
//                                    context,
//                                    "oom14",
//                                    "oo",
//                                    "${where}+${unit.id}"
//                                )
//                                appOpenAd.setOnPaidEventListener { adValue ->
//                                    adValue.let {
//                                        SmileNetHelp.postAdData(
//                                            App.getAppContext(),
//                                            adValue,
//                                            appOpenAd.responseInfo,
//                                            openAdData
//                                        )
//                                    }
//                                    val adRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                                    adRevenue.setRevenue(
//                                        adValue.valueMicros / 1000000.0,
//                                        adValue.currencyCode
//                                    )
//                                    adRevenue.setAdRevenueNetwork(appOpenAd.responseInfo.mediationAdapterClassName)
//                                    Adjust.trackAdRevenue(adRevenue)
//                                }
                            }
                        })
                }

                SmileKey.POS_CONNECT, SmileKey.POS_BACK -> {
//                    if (unit.name == SmileKey.POS_CONNECT) {
//                        connectAdData.id = unit.id
//                        connectAdData.type = unit.type
//                        connectAdData.name = unit.name
//                        connectAdData = PutDataUtils.beforeLoadLink(connectAdData)
//                    }
//                    if (unit.name == SmileKey.POS_BACK) {
//                        backAdData.id = unit.id
//                        backAdData.type = unit.type
//                        backAdData.name = unit.name
//                        backAdData = PutDataUtils.beforeLoadLink(backAdData)
//                    }
//                    if (unit.name == SmileKey.POS_INT3) {
//                        int3AdData.id = unit.id
//                        int3AdData.type = unit.type
//                        int3AdData.name = unit.name
//                        int3AdData = PutDataUtils.beforeLoadLink(int3AdData)
//                    }
                    InterstitialAd.load(
                        requestContext,
                        unit.adIdDual,
                        AdRequest.Builder().build(),
                        object : InterstitialAdLoadCallback() {
                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                Log.d(TAG, "${where} ---request fail: ${loadAdError.message}")
                                callback(null)
                            }

                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                                SmileNetHelp.postPotNet(
//                                    context,
//                                    "oom14",
//                                    "oo",
//                                    "${where}+${unit.id}"
//                                )
                                callback(interstitialAd)
//                                interstitialAd.setOnPaidEventListener { adValue ->
//                                    val bean = when (unit.name) {
//                                        SmileKey.POS_CONNECT -> {
//                                            connectAdData
//                                        }
//
//                                        SmileKey.POS_BACK -> {
//                                            backAdData
//                                        }
//
//                                        SmileKey.POS_INT3 -> {
//                                            int3AdData
//                                        }
//
//                                        else -> {
//                                            null
//                                        }
//                                    }
//                                    adValue.let {
//                                        bean?.let { it1 ->
//                                            SmileNetHelp.postAdData(
//                                                App.getAppContext(),
//                                                adValue,
//                                                interstitialAd.responseInfo,
//                                                it1
//                                            )
//                                        }
//                                        val adRevenue =
//                                            AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
//                                        adRevenue.setRevenue(
//                                            adValue.valueMicros / 1000000.0,
//                                            adValue.currencyCode
//                                        )
//                                        adRevenue.setAdRevenueNetwork(interstitialAd.responseInfo.mediationAdapterClassName)
//                                        Adjust.trackAdRevenue(adRevenue)
//                                    }
//                                }
                            }
                        }
                    )
                }

                else -> {
                    callback(null)
                }
            }
        }

        fun showFullScreen(
            context: AppCompatActivity,
            res: Any,
            callback: () -> Unit
        ) {
            when (res) {
                is AppOpenAd -> {
                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res.show(context)
//                    openAdData = PutDataUtils.afterLoadLink(openAdData)
                }

                is InterstitialAd -> {
                    if (where != SmileKey.POS_CONNECT && !UserConter.showAdCenter()) {
                        callback.invoke()
                        return
                    }
                    if (!UserConter.showAdBlacklist()) {
                        callback.invoke()
                        return
                    }
                    context.lifecycleScope.launch(Dispatchers.Main) {
                        res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                        res.show(context)
                    }

                    when (where) {
                        SmileKey.POS_CONNECT -> {
//                            connectAdData = PutDataUtils.afterLoadLink(connectAdData)
                        }

                        SmileKey.POS_BACK -> {
//                            backAdData = PutDataUtils.afterLoadLink(backAdData)
                        }
                    }
                }

                is RewardedAd -> {
                    res.fullScreenContentCallback = GoogleFullScreenCallback(where, callback)
                    res?.let { ad ->
                        ad.show(context) { rewardItem ->
                            // Handle the reward.
                            val rewardAmount = rewardItem.amount
                            val rewardType = rewardItem.type
                        }
//                        reWardeAdData = PutDataUtils.afterLoadLink(reWardeAdData)
                    }

                }

                else -> callback()
            }
        }

        fun showNativeOf(
            nativeRoot: View,
            res: Any,
            callback: () -> Unit
        ) {
            val nativeAd = res as? NativeAd ?: return
            if (where == SmileKey.POS_HOME) {
                if (!UserConter.showAdBlacklist()) {
                    Log.e(TAG, "根据黑名单屏蔽home广告。。。")
                    return
                }
            }
            nativeRoot.findViewById<View>(R.id.img_ad_type)?.visibility = View.GONE
            val nativeAdView =
                nativeRoot.findViewById<NativeAdView>(R.id.native_ad_view) ?: return
            nativeAdView.visibility = View.VISIBLE
            nativeRoot.findViewById<MediaView>(R.id.ad_media)?.let { mediaView ->
                nativeAdView.mediaView = mediaView
                nativeAd.mediaContent?.let { mediaContent ->
                    mediaView.setMediaContent(mediaContent)
                    mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                }
            }

            nativeRoot.findViewById<TextView>(R.id.ad_headline)?.let { titleView ->
                nativeAdView.headlineView = titleView
                titleView.text = nativeAd.headline
            }


            nativeRoot.findViewById<TextView>(R.id.ad_call_to_action)?.let { actionView ->
                nativeAdView.callToActionView = actionView
                actionView.text = nativeAd.callToAction
            }

            nativeRoot.findViewById<ImageView>(R.id.ad_app_icon)?.let { iconView ->
                nativeAdView.iconView = iconView
                iconView.setImageDrawable(nativeAd.icon?.drawable)
            }
            Log.e(TAG, "原生广告开始展示")
//            if (where == "si_h") {
//                myUnit_H = afterLoadLinkSettingsBa(myUnit_H)
//            } else {
//                myUnit_R = afterLoadLinkSettingsBa(myUnit_R)
//            }
            nativeAdView.setNativeAd(nativeAd)
            SmileKey.recordNumberOfAdDisplaysGreen()
            Timber.tag(TAG).e("showNativeOf: %s", where)
            callback()
        }
    }
}