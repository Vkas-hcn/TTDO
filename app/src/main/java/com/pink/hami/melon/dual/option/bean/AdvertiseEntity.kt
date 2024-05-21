package com.pink.hami.melon.dual.option.bean

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class EveryADBean(
    @SerializedName("fa_c_eous")
    var adYypeDual: String? = null,

    @SerializedName("fa_c_ail")
    var adNetOperatorDual: String? = null,

    @SerializedName("fa_c_les")
    var adIdDual: String? = null,

    @SerializedName("fa_c_oute")
    var adWeightDual: Int = 0,

    var whereDual: String? = null,

    val adCacheInvalidTime: Int = 2,
)

@Keep
data class AdvertiseEntity(
    @SerializedName("dual_ssss")
    val showMax: Int = 0,
    @SerializedName("dual_cccc")
    val clickMax: Int = 0,

    @SerializedName("open_dual")
    val start: MutableList<EveryADBean>?,

    @SerializedName("home_dual")
    val home: MutableList<EveryADBean>?,

    @SerializedName("end_dual")
    val result: MutableList<EveryADBean>?,

    @SerializedName("connect_dual")
    val connect: MutableList<EveryADBean>?,

    @SerializedName("back_dual")
    val back: MutableList<EveryADBean>?,
)


