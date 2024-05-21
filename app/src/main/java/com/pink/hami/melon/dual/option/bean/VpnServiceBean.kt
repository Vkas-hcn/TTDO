package com.pink.hami.melon.dual.option.bean

import com.google.gson.annotations.SerializedName


data class OnlineBean(
    val code: Int,
    val `data`: Data,
    val msg: String
)

data class Data(
    @SerializedName("lYUV")
    val server_list: MutableList<VpnServiceBean>,
    @SerializedName("dXNJbL")
    val smart_list: MutableList<VpnServiceBean>
)

data class VpnServiceBean(
    @SerializedName("HrRKz")
    var city: String = "",

    @SerializedName("lEiDC")
    var country_name: String = "",

    @SerializedName("SThwT")
    val ip: String = "",

    @SerializedName("KrJRsKxr")
    val mode: String = "",

    @SerializedName("wvAKuJN")
    val port: Int = 0,

    @SerializedName("VVlFAaD")
    val user_pwd: String = "",

    var best_smart: Boolean = false,
    var smart_smart: Boolean = false,
    var check_smart: Boolean = false,
)
