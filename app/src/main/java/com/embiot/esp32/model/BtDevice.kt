package com.embiot.esp32.model

import androidx.annotation.Keep

@Keep
data class BtDevice(
    val name: String? = null,
    val address: String? = null,
)
