package com.example.model

import java.io.File

data class AccessLog(
  val id: String,
  val timestamp: Long,
  val method: String,
  val path: String,
  val statusCode: Int,
  val responseSize: Long,
  val durationMs: Long = 0L,
  val clientIp: String,
  val userAgent: String = "",
  val contentType: String = "",
  val protocol: String = "HTTP/1.1"
)

data class ServerStatus(
  val isRunning: Boolean = false,
  val port: Int = 8080,
  val localUrl: String = "http://127.0.0.1:8080",
  val lanUrl: String = "",
  val activeClientsCount: Int = 0,
  val uptimeSeconds: Long = 0L
)

data class WebFile(
  val file: File,
  val name: String,
  val path: String,
  val isDirectory: Boolean,
  val size: Long,
  val lastModified: Long
)

enum class ServerTab(val label: String) {
  SERVER("Server"),
  FILES("Berkas"),
  LOGS("Log")
}
