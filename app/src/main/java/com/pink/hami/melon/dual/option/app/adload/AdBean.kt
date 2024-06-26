package com.pink.hami.melon.dual.option.app.adload

data class AdBean(
    val netw: String,
    val nets: String,
    val netu: String,
    val neta: Int
)

data class AdListBean(
    val net2: Int,
    val net1: Int,
    val hury: MutableList<AdBean>,
    val bathe: MutableList<AdBean>,
    val mouth: MutableList<AdBean>,
    val cheap: MutableList<AdBean>,
    val sadly: MutableList<AdBean>,
)

