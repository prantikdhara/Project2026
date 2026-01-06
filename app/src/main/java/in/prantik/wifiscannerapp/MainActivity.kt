package `in`.prantik.wifiscannerapp

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var wifiManager: WifiManager
    private lateinit var scanButton: Button
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val wifiList = mutableListOf<String>()
    private val permissionCode = 101
    private lateinit var wifiReceiver: WifiScanReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initWifi()
    }

    private fun initViews() {
        scanButton = findViewById(R.id.btnScan)
        listView = findViewById(R.id.listViewWifi)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, wifiList)
        listView.adapter = adapter

        scanButton.setOnClickListener {
            checkPermissionsAndScan()
        }
    }

    private fun initWifi() {
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiReceiver = WifiScanReceiver { results ->
            wifiList.clear()
            wifiList.addAll(results)
            adapter.notifyDataSetChanged()
        }
    }

    private fun checkPermissionsAndScan() {
        // Checking for both Fine Location and Nearby Wifi Devices (for Android 13+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ),
                permissionCode
            )
        } else {
            startWifiScan()
        }
    }

    private fun startWifiScan() {
        // Note: isWifiEnabled is deprecated in newer APIs but still used for older ones [cite: 75-77]
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Enabling Wi-Fi...", Toast.LENGTH_SHORT).show()
            wifiManager.isWifiEnabled = true
        }

        registerReceiver(
            wifiReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        val success = wifiManager.startScan()
        if (success) {
            Toast.makeText(this, "Scanning Wi-Fi networks...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Scan failed (Throttled)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startWifiScan()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            // Remove the space between 'wifi' and 'Receiver'
            unregisterReceiver(wifiReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}