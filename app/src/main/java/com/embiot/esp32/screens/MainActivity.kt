package com.embiot.esp32.screens

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.embiot.esp32.databinding.ActivityMainBinding
import com.embiot.esp32.utils.StaticMethods.printToLog
import com.embiot.esp32.utils.StaticMethods.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val strMac = intent.extras?.getString("address")
        "strMac: $strMac".printToLog("main")
        toast("strMac: $strMac")
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        connectToAddress(strMac = strMac)

        binding.mainBtnSendData.setOnClickListener {
            try {
                bluetoothSocket?.outputStream?.write("SHAFIK".toByteArray())
                toast("SHAFIK send to ESP32")

            } catch (e: Exception) {
                toast("error to send SHAFIK; catch: ${e.message}")
            }
        }
    }

    private fun connectToAddress(strMac: String?) {

        lifecycleScope.launch(Dispatchers.IO)
        {
            val isConnected = try {
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }

                val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                val bd: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(strMac)
                bluetoothSocket = bd?.createInsecureRfcommSocketToServiceRecord(myUUID)
                bluetoothAdapter?.cancelDiscovery()
                bluetoothSocket?.connect()
                withContext(Dispatchers.Main) {
                    toast("2. connectToAddress: connected to $strMac")
//                    val bluetoothService = BluetoothService()
//                    bluetoothService.execute()
                }
                true
            } catch (e: Exception) {
                "catch: ${e.message}".printToLog("main")
                bluetoothSocket?.close()
                withContext(Dispatchers.Main) {
                    toast("2. connectToAddress: catch: ${e.message}")
                }
                false
            }

            withContext(Dispatchers.Main)
            {
                toast("2. connectToAddress: isConnected: $isConnected")
                if (!isConnected) {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setMessage("Please Turn on the Embiot Device")
                        .setPositiveButton("Retry") { _, _ ->
                            connectToAddress(strMac = strMac)
                        }
                        .show()
                } else {
//                    val outputStream = bluetoothSocket?.outputStream
//                    val inputStream = bluetoothSocket?.inputStream
                    readData()
                }
            }
        }
    }

    private fun readData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val byte = ByteArray(1024)  //Buffer to store receiving data
            var stringResult = ""
            val value = bluetoothSocket?.inputStream?.read(byte)
            value?.let {
                stringResult += String(byte, 0, it)
            }
            withContext(Dispatchers.Main) {
                toast("Received data: $stringResult")
            }

//            while (true) {
//                //TODO: code for receive data
//            }
        }
    }
}