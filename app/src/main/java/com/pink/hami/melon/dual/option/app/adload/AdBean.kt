package com.pink.hami.melon.dual.option.app.adload

import androidx.annotation.Keep

@Keep
data class AdBean(
    val netw: String,
    val nets: String,
    val netu: String,
    val neta: Int,
    var ttd_load_ip: String = "",
    var ttd_load_city: String = "",
    var ttd_show_ip: String = "",
    var ttd_show_city: String = ""
)
@Keep
data class AdListBean(
    val net2: Int,
    val net1: Int,
    val hury: MutableList<AdBean>,
    val bathe: MutableList<AdBean>,
    val mouth: MutableList<AdBean>,
    val cheap: MutableList<AdBean>,
    val sadly: MutableList<AdBean>,
)

