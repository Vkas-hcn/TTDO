package com.pink.hami.melon.dual.option.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.pink.hami.melon.dual.option.bean.VpnServiceBean
import com.github.shadowsocks.database.Profile
import android.util.Base64
import com.pink.hami.melon.dual.option.R
import com.pink.hami.melon.dual.option.app.adload.GetAdData

object DulaShowDataUtils {
    fun String.getDualImage():  Int {
        return when (this) {
            "United States" -> R.drawable.unitedstates
            "Australia" -> R.drawable.australia
            "Canada" -> R.drawable.canada
            "China" -> R.drawable.canada
            "France" -> R.drawable.france
            "Germany" -> R.drawable.germany
            "Hong Kong" -> R.drawable.hongkong
            "India" -> R.drawable.india
            "Japan" -> R.drawable.japan
            "koreasouth" -> R.drawable.koreasouth
            "Singapore" -> R.drawable.singapore
            "Taiwan" -> R.drawable.taiwan
            "Brazil" -> R.drawable.brazil
            "United Kingdom" -> R.drawable.unitedkingdom
            "India" -> R.drawable.india
            "Nether Lands" -> R.drawable.netherlands
            else -> R.drawable.fast
        }
    }
    fun setSkServerData(profile: Profile, bestData: VpnServiceBean): Profile {
        GetAdData.nowLoadIpZone=bestData.ip
        GetAdData.nowLoadCityZone = bestData.city

        profile.name = bestData.country_name + "-" + bestData.city
        profile.host = bestData.ip
        profile.password = bestData.user_pwd
        profile.method = bestData.mode
        profile.remotePort = bestData.port
        return profile
    }
    fun isAppOnline(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        if (networkCapabilities != null) {
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        return false
    }
    fun dropReversed(responseString:String):String{
        val trimmedString = responseString.drop(13)
        val reversedString = trimmedString.reversed()
        return String(Base64.decode(reversedString?.toByteArray(), Base64.DEFAULT))
    }
}