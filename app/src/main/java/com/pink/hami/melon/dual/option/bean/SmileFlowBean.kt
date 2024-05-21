package com.pink.hami.melon.dual.option.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SmileFlowBean(
    @SerializedName("blydmit")
    val shield: String = "",

    @SerializedName("blymajor")
    val cloak: String = "",

    @SerializedName("blyyarium")
    val flowAround: String = "",

    val cont: String = "0"
) : Serializable
