package com.embiot.esp32.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.embiot.esp32.databinding.ActivityEnableBluetoothBinding
import com.embiot.esp32.utils.StaticMethods.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnableBluetoothActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnableBluetoothBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnableBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        displayData(bluetoothAdapter = bluetoothAdapter)

        binding.enableBtBtn.setOnClickListener {
            if (bluetoothAdapter == null) {
                toast("Bluetooth is not available in your device")
                return@setOnClickListener
            }

            if (!bluetoothAdapter!!.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkBluetoothPermission()
                } else {
                    btIsDisable()
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    resultLauncher.launch(enableBtIntent)
                }
            }
            //bluetooth is disable
            else {
                goToNextActivity()
            }
//            if (ActivityCompat.checkSelfPermission(
//                    this@EnableBluetoothActivity,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                toast("Please allow Bluetooth permission from Settings")
//                return@setOnClickListener
//            }
//
//            bluetoothAdapter?.enable()
        }
    }

    @SuppressLint("InlinedApi")
    private fun checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (!bluetoothAdapter!!.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                resultLauncher.launch(enableBtIntent)
            } else {
                goToNextActivity()
            }
        } else {
            btIsDisable()
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            goToNextActivity()
        } else {
            Toast.makeText(
                this,
                "Please turn on Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun btIsEnable() {
        binding.enableBtBtn.isEnabled = true
        binding.enableBtBtn.text = "Turn on"
    }

    private fun btIsDisable()
    {

    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (!it.value) {
                    permissionGranted = false
                }
            }

            if (permissionGranted) {
                if (!bluetoothAdapter!!.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    resultLauncher.launch(enableBtIntent)
                } else {
                    goToNextActivity()
                }
            }
        }

    private fun goToNextActivity() {
        finish()
        startActivity(
            Intent(
                this@EnableBluetoothActivity,
                DeviceListActivity::class.java
            )
        )
    }

    @SuppressLint("SetTextI18n")
    private fun displayData(bluetoothAdapter: BluetoothAdapter?) {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                if (bluetoothAdapter!!.isEnabled) {
                    goToNextActivity()
                } else {
                    toast("Bluetooth is not Turn On.\nPlease Click on Button Again")

                    binding.enableBtBtn.isEnabled = true
                    binding.enableBtBtn.text = "Turn on"
                }
            }
        }
    }
}