package com.embiot.esp32.utils

import android.app.Activity
import android.util.Log
import android.widget.Toast

/**
 * @Author: Shafik Shaikh
 * @Date: 09-06-2023
 */
object StaticMethods {
    fun Any?.printToLog(tag: String = "DEBUG_LOG") {
        Log.e(tag, toString())
    }

    fun Activity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}