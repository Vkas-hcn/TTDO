package com.github.shadowsocks

import androidx.annotation.Keep

@Keep
data class AppData(
    var vpn_online_data_dualLoad: String = "",
    var uuid_dualLoadile: String = "",
    var check_service: String = "",

    var vpn_ip_dualLoad: String = "",
    var vpn_city: String = "",
    var connection_mode: String = "",
    var local_clock: String = "",
    var ip_lo_dualLoad: String = "",
    var ip_gsd: String = "",
    var ip_gsd_oth: String = "",
    var gidData: String = "",

    var dual_sp_dow: String = "",
    var dual_sp_up: String = "",
)
