package com.embiot.esp32.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.embiot.esp32.R
import com.embiot.esp32.utils.StaticMethods.printToLog
import com.embiot.esp32.utils.StaticMethods.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        lifecycleScope.launch(Dispatchers.IO) {
            delay(2000)
            withContext(Dispatchers.Main) {
                if (bluetoothAdapter == null) {
                    toast("Bluetooth is not available in your device")
                } else if (!bluetoothAdapter.isEnabled) {
                    finish()
                    startActivity(Intent(this@SplashActivity, EnableBluetoothActivity::class.java))
                } else {
                    finish()
                    startActivity(Intent(this@SplashActivity, DeviceListActivity::class.java))
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            "action: ${intent.action}".printToLog("splash")
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            "android.bluetooth.device.extra.DEVICE",
                            BluetoothDevice::class.java
                        )
                    } else {
                        intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")
                    }

                    val strMacAddress = device?.address
                    "SelectedMacAddress: $strMacAddress".printToLog("splash")
                }
            }
        }
    }
}