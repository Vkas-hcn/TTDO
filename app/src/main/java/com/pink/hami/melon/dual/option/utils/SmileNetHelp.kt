package com.pink.hami.melon.dual.option.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.android.installreferrer.api.ReferrerDetails
import com.pink.hami.melon.dual.option.app.App.Companion.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object SmileNetHelp {
    val smileNetManager = SmileNetManager()

    suspend fun getOnlyIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ifconfig.me/ip")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }

            SmileKey.ip_lo_sm = content
        } catch (e: Exception) {
        }
    }

    suspend fun getLoadIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.infoip.io/")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country_short\":\"")[1].split("\"")[0]
            SmileKey.ip_gsd = data
        } catch (e: Exception) {
        }
    }

    suspend fun getLoadOthIp() = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://ipinfo.io/json")
            val inputStream = url.openStream()
            val content = inputStream.bufferedReader().use { it.readText() }
            val data = content.split("\"country\":\"")[1].split("\"")[0]
            SmileKey.ip_gsd_oth = data
        } catch (e: Exception) {
        }
    }

    suspend fun getBlackData(context: Context) = withContext(Dispatchers.IO) {
        val data = SmileKey.local_clock
        if (data.isEmpty()) {
            val params = blackData(context)
            try {
                smileNetManager.getMapRequest(
                    "https://emery.bloomingunlimited.com/assam/meander",
                    params,
                    object : SmileNetManager.Callback {
                        override fun onSuccess(response: String) {
                            SmileKey.local_clock = response
                        }

                        override fun onFailure(error: String) {
                            nextBlackFun(context)
                        }
                    })
            } catch (e: Exception) {
                nextBlackFun(context)
            }
        }
    }

    private fun nextBlackFun(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(10000)
            getBlackData(context)
        }
    }


    @SuppressLint("HardwareIds")
    fun blackData(context: Context): Map<String, Any> {
        return mapOf<String, Any>(
            //distinct_id
            "haze" to SmileKey.uuid_smile,
            //client_ts
            "rabbet" to (System.currentTimeMillis()),//日志发生的客户端时间，毫秒数
            //device_model
            "sorrow" to Build.MODEL,
            //bundle_id
            "celia" to ("com.blooming.unlimited.fast"),//当前的包名称，a.b.c
            //os_version
            "va" to Build.VERSION.RELEASE,
            //gaid
            "catkin" to SmileKey.gidData,
            //android_id
            "negligee" to Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ),
            //os
            "blade" to "director",
            //app_version
            "freemen" to (getAppVersion(context) ?: ""),
            //operator
            "somatic" to "",
            //sdk_ver
            "armata" to ""
        )
    }

    private fun getAppVersion(context: Context): String? {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun postSessionData(context: Context) {
        val data = PutDataUtils.getSessionJson(context)
        Log.e(TAG, "postSessionData: data=${data}")

        smileNetManager.postPutData(
            SmileKey.put_data_url,
            data,
            object : SmileNetManager.Callback {
                override fun onSuccess(response: String) {
                    Log.e(TAG, "postSessionData: onSuccess=${response}")
                }

                override fun onFailure(error: String) {
                    Log.e(TAG, "postSessionData: onFailure=${error}")

                }
            })
    }

//    fun postAdData(
//        context: Context, adValue: AdValue,
//        responseInfo: ResponseInfo,
//        AdvertiseEntity: AdvertiseEntity
//    ) {
//        val data = PutDataUtils.getAdJson(context, adValue, responseInfo, AdvertiseEntity)
//        Log.e(TAG, "postAdData: data=${data}")
//        smileNetManager.postPutData(
//            SmileKey.put_data_url,
//            data,
//            object : SmileNetManager.Callback {
//                override fun onSuccess(response: String) {
//                    Log.e(TAG, "postAdData: onSuccess=${response}")
//                }
//
//                override fun onFailure(error: String) {
//                    Log.e(TAG, "postAdData: onFailure=${error}")
//                }
//            })
//        PutDataUtils.postAdOnline(adValue.valueMicros)
//    }

    fun postInstallData(context: Context, rd: ReferrerDetails) {
        if (SmileKey.isInstall == "1") {
            return
        }
        val data = PutDataUtils.getInstallJson(rd, context)
        Log.e(TAG, "postInstallData: data=${data}")

        try {
            smileNetManager.postPutData(
                SmileKey.put_data_url,
                data,
                object : SmileNetManager.Callback {
                    override fun onSuccess(response: String) {
                        SmileKey.isInstall = "1"
                        Log.e(TAG, "postInstallData: onSuccess=${response}")

                    }

                    override fun onFailure(error: String) {
                        SmileKey.isInstall = "0"
                        Log.e(TAG, "postInstallData: onFailure=${error}")

                    }
                })
        } catch (e: Exception) {
            SmileKey.isInstall = "0"
            Log.e(TAG, "postInstallData: Exception=${e}")

        }
    }

    fun postPotIntData(context: Context, name: String,key:String?=null,time:Int=0) {
        val data =  if(key!=null){
            PutDataUtils.getTbaTimeDataJson(context, name, key, time)
        }else{
            PutDataUtils.getTbaDataJson(context, name)
        }
        Log.e(TAG, "postPotIntData--${name}: data=${data}")
        try {
            smileNetManager.postPutData(
                SmileKey.put_data_url,
                data,
                object : SmileNetManager.Callback {
                    override fun onSuccess(response: String) {
                        Log.e(TAG, "postPotIntData--${name}: onSuccess=${response}")
                    }

                    override fun onFailure(error: String) {
                        Log.e(TAG, "postPotIntData--${name}: onFailure=${error}")

                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "postPotIntData--${name}: Exception=${e}")
        }
    }


    fun getOnlineSmData(context: Context){
        val timeStart = System.currentTimeMillis()
        postPotIntData(context,"blom1")
        smileNetManager.getServiceData(context, SmileKey.put_sm_service_data_url,{
                val data = SmileUtils.decodeTheData(it)
            SmileKey.vpn_online_data = data
            Log.e(TAG, "getOnlineSmData: ${SmileKey.vpn_online_data}", )
            val timeEnd = (System.currentTimeMillis()-timeStart)/1000
            postPotIntData(context,"blom2t","time",timeEnd.toInt())
            postPotIntData(context,"blom2")

        },{
            Log.e(TAG, "getOnlineSmData---error=: ${it}", )

        })
    }
}