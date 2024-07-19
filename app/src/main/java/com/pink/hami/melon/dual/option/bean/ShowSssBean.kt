package com.pink.hami.melon.dual.option.bean

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DualFFFFFFBean(
    val code: Int,
    val `data`: Data,
    val msg: String
)
@Keep
data class Data(
    @SerializedName("CWywWBwl")
    val server_list: MutableList<VpnServiceBean>,
    @SerializedName("CEFWfua")
    val smart_list: MutableList<VpnServiceBean>
)
@Keep
data class VpnServiceBean(
    @SerializedName("PVTzJURHO")
    var city: String = "",

    @SerializedName("cmzOH")
    var country_name: String = "",

    @SerializedName("uzDlYUg")
    val ip: String = "",

    @SerializedName("rsB")
    val mode: String = "",

    @SerializedName("rYlCpPO")
    val port: Int = 0,

    @SerializedName("yQJzw")
    val user_pwd: String = "",

    var best_dualLoad: Boolean = false,
    var hideViewShow: Boolean = false,
    var check_dualLoad: Boolean = false,
)
