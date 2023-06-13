package com.embiot.esp32.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.embiot.esp32.databinding.ActivityDeviceListBinding
import com.embiot.esp32.databinding.SingleDeviceListBinding
import com.embiot.esp32.model.BtDevice
import com.embiot.esp32.utils.StaticMethods.printToLog
import com.embiot.esp32.utils.StaticMethods.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceListActivity : AppCompatActivity() {
    lateinit var bluetoothManager: BluetoothManager
    lateinit var binding: ActivityDeviceListBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter
        displayDeviceList()

        binding.deviceIvRefresh.setOnClickListener {
            displayDeviceList()
        }
    }

//    override fun startActivityForResult(intent: Intent?, requestCode: Int) {
//        super.startActivityForResult(intent, requestCode)
//        if (bluetoothAdapter?.isEnabled == true)
//            getDeviceList()
//    }

    private fun displayDeviceList() {
        if (ActivityCompat.checkSelfPermission(
                this@DeviceListActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            "displayDeviceList return".printToLog("device")
            return
        }

        lifecycleScope.launch(Dispatchers.IO)
        {
            "displayDeviceList launch started".printToLog("device")
            val devices = bluetoothAdapter?.bondedDevices
            val list = mutableListOf<BtDevice>()

            if (devices != null) {
                if (devices.isNotEmpty()) {
                    for (deviceItem in devices) {
                        val device = BtDevice(name = deviceItem.name, address = deviceItem.address)
                        list.add(device)
                    }
                }
            }

            withContext(Dispatchers.Main)
            {
                if (list.isNotEmpty()) {
                    binding.deviceRv.apply {
                        val deviceAdapter = DeviceAdapter(list = list)
                        adapter = deviceAdapter
                        layoutManager = LinearLayoutManager(
                            this@DeviceListActivity,
                            RecyclerView.VERTICAL,
                            false
                        )
                    }
                } else {
                    binding.deviceIv.visibility = View.VISIBLE
                    toast("No devices found")
                }
            }

        }
    }

    private inner class DeviceAdapter(val list: List<BtDevice>) :
        RecyclerView.Adapter<DeviceAdapter.DeviceHolder>() {
        inner class DeviceHolder(val singleBinding: SingleDeviceListBinding) :
            RecyclerView.ViewHolder(singleBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
            val singleBinding =
                SingleDeviceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DeviceHolder(singleBinding)
        }

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            val listItem = list[position]
            holder.singleBinding.singleDeviceTvName.text = listItem.name
            holder.itemView.setOnClickListener {
                finish()
                val intent = Intent(this@DeviceListActivity, MainActivity::class.java)
                intent.putExtra("address", listItem.address)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }
    }
}