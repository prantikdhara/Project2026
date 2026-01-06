package `in`.prantik.wifiscannerapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat

class WifiScanReceiver(
    private val onResultsReady: (List<String>) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return // Ensure context is not null

        val wifiManager = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Correctly check for permission before accessing scanResults to avoid the error
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val results = wifiManager.scanResults
            val wifiInfoList = mutableListOf<String>()

            for (result in results) {
                val info = """
        SSID: ${result.SSID}
        BSSID: ${result.BSSID}
        Signal Strength: ${result.level} dBm
        Frequency: ${result.frequency} MHz
    """.trimIndent()
                wifiInfoList.add(info)
            }
            onResultsReady(wifiInfoList)
        }
    }
}