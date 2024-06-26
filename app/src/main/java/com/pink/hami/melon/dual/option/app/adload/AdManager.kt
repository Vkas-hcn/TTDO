package com.pink.hami.melon.dual.option.app.adload

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.App
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.finish.FinishActivity
import com.pink.hami.melon.dual.option.bjfklieaf.fast.show.main.MainActivity
import com.pink.hami.melon.dual.option.utils.DualContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class AdManager private constructor(
    private val application: Application,
    private val adPosition: GetAdData.AdWhere
) : Application.ActivityLifecycleCallbacks {

    private var ad: Any? = null
    private var lastLoadTime: Long = 0
    private var adList: AdListBean? = null
    private var adIndex: Int = 0
    private var onAdClosedCallback: (() -> Unit)? = null
    private var isAdLoading: Boolean = false
    private var isOneOpenLoad = false

    init {
        MobileAds.initialize(application) {}
        application.registerActivityLifecycleCallbacks(this)
        resetCountsIfNeeded()
    }

    companion object {
        private val instances = mutableMapOf<String, AdManager>()

        fun getInstance(application: Application, adPosition: GetAdData.AdWhere): AdManager {
            return instances.getOrPut(adPosition.toString()) {
                AdManager(application, adPosition)
            }
        }
    }

    private fun setAdList(adList: AdListBean) {
        this.adList = adList
    }

    fun loadAd() {
        val data = GetAdData.getAdData()
        setAdList(data)
        if (!canLoadAd()) {
            Log.e("TAG", "${adPosition}-广告超限-无法加载")
            return
        }
        if (isAdAvailable()) return

        val userData = GetAdData.refAdUsers()
        val blackData = GetAdData.getAdBlackData()
        if (!userData && (adPosition == GetAdData.AdWhere.HOME || adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
            Log.e("TAG", "买量屏蔽${adPosition}广告")
            return
        }
        if (blackData && (adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
            Log.e("TAG", "黑名单屏蔽${adPosition}广告")
            return
        }
        adIndex = 0
        isAdLoading = true

        when (adPosition) {
            GetAdData.AdWhere.GUIDE -> loadAppOpenAd()
            GetAdData.AdWhere.CONNECT, GetAdData.AdWhere.BACK -> loadInterstitialAd()
            GetAdData.AdWhere.HOME, GetAdData.AdWhere.END -> loadNativeAd()
        }
    }

    fun showAd(activity: Activity, onAdClosedCallback: (() -> Unit)? = null) {
        this.onAdClosedCallback = onAdClosedCallback
        Log.e("TAG", "${adPosition}-广告位是否是前台-${isAppInForeground}")
        if (!isAppInForeground) return

        Log.e("TAG", "${adPosition}广告-开始展示")
        when (ad) {
            is AppOpenAd -> {
                (ad as AppOpenAd).fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdClosedCallback?.invoke()
                        clearAd()
                    }

                    override fun onAdShowedFullScreenContent() {
                        incrementOpenCount()
                    }

                    override fun onAdClicked() {
                        incrementClickCount()
                    }
                }
                (ad as AppOpenAd).show(activity)
            }

            is InterstitialAd -> {
                (ad as InterstitialAd).fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onAdClosedCallback?.invoke()
                            clearAd()
                            if (adPosition == GetAdData.AdWhere.CONNECT) {
                                loadAd()
                            }
                        }

                        override fun onAdShowedFullScreenContent() {
                            incrementOpenCount()
                        }

                        override fun onAdClicked() {
                            incrementClickCount()
                        }
                    }
                (ad as InterstitialAd).show(activity)
            }

            is NativeAd -> {
                if (adPosition == GetAdData.AdWhere.HOME) {
                    setDisplayHomeNativeAdFlash(activity as MainActivity)
                } else {
                    setDisplayEndNativeAdFlash(activity as FinishActivity)
                }
            }
        }
    }

    fun canShowAd(): Int {
        val userData = GetAdData.refAdUsers()
        val blackData = GetAdData.getAdBlackData()
        if (!userData && (adPosition == GetAdData.AdWhere.HOME || adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
            return 0
        }
        if (blackData && (adPosition == GetAdData.AdWhere.CONNECT || adPosition == GetAdData.AdWhere.BACK)) {
            onAdClosedCallback?.invoke()
            return 0
        }
        if (ad == null && !canLoadAd()) {
            return 0
        }
        if (ad == null && canLoadAd()) {
            return 2
        }
        if (ad != null && canLoadAd()) {
            return 1
        }
        return 1
    }

    fun hasAdDataForPosition(): Boolean {
        return ad != null && !isAdExpired()
    }

    private fun isAdAvailable(): Boolean {
        return ad != null && !isAdExpired()
    }

    private fun isAdExpired(): Boolean {
        val currentTime = Calendar.getInstance().timeInMillis
        return currentTime - lastLoadTime > 24 * 60 * 60 * 1000 // 24 hours
    }

    private fun clearAd() {
        ad = null
        isAdLoading = false
    }

    private fun canLoadAd(): Boolean {
        resetCountsIfNeeded()
        val adOpenNum = adList?.net1 ?: 30
        val adClickNum = adList?.net2 ?: 5
        val currentOpenCount = DualContext.localStorage.net1
        val currentClickCount = DualContext.localStorage.net2
        return currentOpenCount < adOpenNum && currentClickCount < adClickNum
    }

    private fun incrementOpenCount() {
        val currentCount = DualContext.localStorage.net1
        DualContext.localStorage.net1 = currentCount + 1
    }

    private fun incrementClickCount() {
        val currentCount = DualContext.localStorage.net2
        DualContext.localStorage.net2 = currentCount + 1
    }

    private fun resetCountsIfNeeded() {
        val currentDate = Calendar.getInstance().timeInMillis
        if (!isSameDay(DualContext.localStorage.ad_date_app, currentDate)) {
            DualContext.localStorage.ad_date_app = currentDate
            DualContext.localStorage.net1 = 0
            DualContext.localStorage.net2 = 0
        }
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val calendar1 = Calendar.getInstance().apply { timeInMillis = time1 }
        val calendar2 = Calendar.getInstance().apply { timeInMillis = time2 }
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    private fun loadAppOpenAd() {
        val adBeans = adList?.sadly?.filter { it.netu == adPosition.toString() }
            ?.sortedByDescending { it.neta }
        if (adBeans.isNullOrEmpty() || adIndex >= adBeans.size) {
            isAdLoading = false
            if (!isOneOpenLoad) {
                adIndex = 0
                isOneOpenLoad = true
                loadAppOpenAd()
            }
            return
        }
        val adBean = adBeans[adIndex]
        val request = AdRequest.Builder().build()
        Log.e("TAG", "${adPosition}广告-开始加载;neta=${adBean.neta};id=${adBean.netw}")
        AppOpenAd.load(
            application,
            adBean.netw,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(loadedAd: AppOpenAd) {
                    Log.e("TAG", "${adPosition}广告-加载成功")
                    ad = loadedAd
                    lastLoadTime = Calendar.getInstance().timeInMillis
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adPosition}广告-加载失败:${loadAdError}")
                    adIndex++
                    loadAppOpenAd()
                }
            })
    }

    private fun loadInterstitialAd() {
        val adBeans = if (adPosition == GetAdData.AdWhere.CONNECT) {
            adList?.bathe?.filter { it.netu == adPosition.toString() }
                ?.sortedByDescending { it.neta }
        } else {
            adList?.ad_back?.filter { it.netu == adPosition.toString() }
                ?.sortedByDescending { it.neta }
        }
        if (adBeans.isNullOrEmpty() || adIndex >= adBeans.size) {
            isAdLoading = false
            return
        }
        Log.e("TAG", "${adPosition}广告-开始加载")
        val adBean = adBeans[adIndex]
        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            application,
            adBean.netw,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loadedAd: InterstitialAd) {
                    Log.e("TAG", "${adPosition}广告加载成功")
                    ad = loadedAd
                    lastLoadTime = Calendar.getInstance().timeInMillis
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adPosition}广告加载失败:${loadAdError}")
                    adIndex++
                    loadInterstitialAd()
                }
            })
    }

    private fun loadNativeAd() {
        val adBeans = if (adPosition == GetAdData.AdWhere.HOME) {
            adList?.cheap?.filter { it.netu == adPosition.toString() }
                ?.sortedByDescending { it.neta }
        } else {
            adList?.mouth?.filter { it.netu == adPosition.toString() }
                ?.sortedByDescending { it.neta }
        }
        if (adBeans.isNullOrEmpty() || adIndex >= adBeans.size) {
            isAdLoading = false
            return
        }
        Log.e("TAG", "${adPosition}广告-开始加载")
        val adBean = adBeans[adIndex]
        val adLoader = AdLoader.Builder(application, adBean.netw)
            .forNativeAd { unifiedNativeAd ->
                Log.e("TAG", "${adPosition}广告加载成功")
                ad = unifiedNativeAd
                lastLoadTime = Calendar.getInstance().timeInMillis
                isAdLoading = false
                unifiedNativeAd.setOnPaidEventListener {
                    loadAd()
                }
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("TAG", "${adPosition}广告加载失败:${loadAdError}")
                    adIndex++
                    loadNativeAd()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.e("TAG", "点击原生广告")
                    incrementClickCount()
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun setDisplayHomeNativeAdFlash(activity: MainActivity) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (ad as NativeAd).let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED

                if (state) {
                    val userData = GetAdData.refAdUsers()
                    if (!userData) {
                        activity.binding.adLayout.isVisible = false
                        Log.e("TAG", "买量屏蔽Home广告")
                        return@let
                    }
                    activity.binding.imgOcAd.isVisible = true

                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(
                        R.layout.layout_home,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.binding.adLayoutAdmob.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.binding.imgOcAd.isVisible = false
                    activity.binding.adLayoutAdmob.isVisible = true
                    incrementOpenCount()
                    clearAd()
                }
            }
        }
    }

    private fun setDisplayEndNativeAdFlash(activity: FinishActivity) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            (ad as NativeAd).let { adData ->
                val state = activity.lifecycle.currentState == Lifecycle.State.RESUMED

                if (state) {
                    activity.binding.imgOcAd.isVisible = true

                    if (activity.isDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        adData.destroy()
                        return@let
                    }
                    val adView = activity.layoutInflater.inflate(
                        R.layout.layout_home,
                        null
                    ) as NativeAdView
                    populateNativeAdView(adData, adView)
                    activity.binding.adLayoutAdmob.apply {
                        removeAllViews()
                        addView(adView)
                    }
                    activity.binding.imgOcAd.isVisible = false
                    activity.binding.adLayoutAdmob.isVisible = true
                    incrementOpenCount()
                    clearAd()
                }
            }
        }
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Bind the native ad data to the ad view
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.mediaView = adView.findViewById(R.id.ad_media)

        nativeAd.mediaContent?.let {
            adView.mediaView?.apply { setImageScaleType(ImageView.ScaleType.CENTER_CROP) }?.mediaContent =
                it
        }
        adView.mediaView?.clipToOutline = true
        adView.mediaView?.outlineProvider = GetAdData.getAdC()
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        } else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }
        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        } else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as TextView).text = nativeAd.callToAction
        }
        if (nativeAd.headline == null) {
            adView.headlineView?.visibility = View.INVISIBLE
        } else {
            adView.headlineView?.visibility = View.VISIBLE
            (adView.headlineView as TextView).text = nativeAd.headline
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon?.drawable
            )
            adView.iconView?.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    private var isAppInForeground = false

    override fun onActivityResumed(activity: Activity) {
        isAppInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        isAppInForeground = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}






