package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.Collections

object NetworkUtils {

  fun getLocalIpAddress(context: Context): String {
    try {
      val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
      val wifiInfo = wifiManager?.connectionInfo
      val ipInt = wifiInfo?.ipAddress ?: 0
      if (ipInt != 0) {
        val ip = String.format(
          "%d.%d.%d.%d",
          ipInt and 0xff,
          ipInt shr 8 and 0xff,
          ipInt shr 16 and 0xff,
          ipInt shr 24 and 0xff
        )
        if (ip != "0.0.0.0") return ip
      }
    } catch (_: Exception) {}

    try {
      val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
      for (netInterface in interfaces) {
        if (netInterface.isLoopback || !netInterface.isUp) continue
        val addresses = Collections.list(netInterface.inetAddresses)
        for (address in addresses) {
          if (!address.isLoopbackAddress && address is Inet4Address) {
            val hostAddress = address.hostAddress ?: ""
            if (hostAddress.isNotEmpty() && !hostAddress.startsWith("127.")) {
              return hostAddress
            }
          }
        }
      }
    } catch (_: Exception) {}

    return "127.0.0.1"
  }

  fun isPortAvailable(port: Int): Boolean {
    if (port < 1024 || port > 65535) return false
    var socket: ServerSocket? = null
    return try {
      socket = ServerSocket(port)
      socket.reuseAddress = true
      true
    } catch (_: Exception) {
      false
    } finally {
      try {
        socket?.close()
      } catch (_: Exception) {}
    }
  }
}
