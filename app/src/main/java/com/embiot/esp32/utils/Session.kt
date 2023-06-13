package com.embiot.esp32.utils

import android.content.Context
import android.content.SharedPreferences

public class Session(context: Context) {
    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("embiot", Context.MODE_PRIVATE)

    fun setBtMac(btMac: String) {
        sharedPref.edit().putString("mac", btMac).apply()
    }

    fun getBtMac(): String? {
        return sharedPref.getString("mac", null)
    }
}