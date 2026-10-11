package com.example.server

import android.util.Base64
import com.example.model.AccessLog
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class FullstackServer(
  private val port: Int,
  private val wwwDir: File,
  private val databaseEngine: DatabaseEngine,
  private val onLog: (AccessLog) -> Unit,
  private val onClientsCountChanged: (Int) -> Unit
) {

  private var serverSocket: ServerSocket? = null
  private val isRunning = AtomicBoolean(false)
  private val threadPool = Executors.newCachedThreadPool()
  private val wsClients = CopyOnWriteArrayList<Socket>()
  private val startTime = System.currentTimeMillis()

  fun start(): Boolean {
    return try {
      val socket = ServerSocket(port)
      socket.reuseAddress = true
      serverSocket = socket
      isRunning.set(true)

      threadPool.execute {
        while (isRunning.get() && !socket.isClosed) {
          try {
            val clientSocket = socket.accept()
            threadPool.execute {
              handleClient(clientSocket)
            }
          } catch (_: Exception) {}
        }
      }
      true
    } catch (_: Exception) {
      false
    }
  }

  fun stop() {
    isRunning.set(false)
    for (ws in wsClients) {
      try {
        ws.close()
      } catch (_: Exception) {}
    }
    wsClients.clear()
    onClientsCountChanged(0)

    try {
      serverSocket?.close()
    } catch (_: Exception) {}
    serverSocket = null
  }

  fun getUptimeSeconds(): Long {
    return if (isRunning.get()) (System.currentTimeMillis() - startTime) / 1000L else 0L
  }

  fun broadcastWebSocketMessage(text: String) {
    val payload = text.toByteArray(StandardCharsets.UTF_8)
    val frame = createWebSocketFrame(payload)
    val deadSockets = mutableListOf<Socket>()

    for (client in wsClients) {
      try {
        if (!client.isClosed && client.isConnected) {
          val out = client.getOutputStream()
          out.write(frame)
          out.flush()
        } else {
          deadSockets.add(client)
        }
      } catch (_: Exception) {
        deadSockets.add(client)
      }
    }

    if (deadSockets.isNotEmpty()) {
      wsClients.removeAll(deadSockets)
      onClientsCountChanged(wsClients.size)
    }
  }

  private fun handleClient(clientSocket: Socket) {
    val requestStartTime = System.currentTimeMillis()
    var rawMethod = "GET"
    var rawPath = "/"
    var clientIp = clientSocket.inetAddress?.hostAddress ?: "127.0.0.1"

    try {
      val input = BufferedInputStream(clientSocket.getInputStream())
      val output = BufferedOutputStream(clientSocket.getOutputStream())

      val requestHeaders = readRequestHeaders(input)
      if (requestHeaders.isEmpty()) {
        clientSocket.close()
        return
      }

      val requestLine = requestHeaders[0]
      val parts = requestLine.split(" ")
      if (parts.size < 2) {
        clientSocket.close()
        return
      }

      rawMethod = parts[0].uppercase(Locale.ROOT)
      rawPath = parts[1]

      val headersMap = mutableMapOf<String, String>()
      for (i in 1 until requestHeaders.size) {
        val line = requestHeaders[i]
        val colonIdx = line.indexOf(":")
        if (colonIdx > 0) {
          val key = line.substring(0, colonIdx).trim().lowercase(Locale.ROOT)
          val value = line.substring(colonIdx + 1).trim()
          headersMap[key] = value
        }
      }

      val userAgent = headersMap["user-agent"] ?: ""

      val isWebSocketUpgrade = headersMap["upgrade"]?.equals("websocket", ignoreCase = true) == true
      if (isWebSocketUpgrade && (rawPath == "/ws" || rawPath.startsWith("/ws?"))) {
        val secKey = headersMap["sec-websocket-key"] ?: ""
        if (secKey.isNotEmpty()) {
          val acceptKey = computeSecWebSocketAccept(secKey)
          val handshakeResponse = "HTTP/1.1 101 Switching Protocols\r\n" +
            "Upgrade: websocket\r\n" +
            "Connection: Upgrade\r\n" +
            "Sec-WebSocket-Accept: $acceptKey\r\n\r\n"

          output.write(handshakeResponse.toByteArray(StandardCharsets.UTF_8))
          output.flush()

          wsClients.add(clientSocket)
          onClientsCountChanged(wsClients.size)

          val duration = System.currentTimeMillis() - requestStartTime
          emitLog(rawMethod, rawPath, 101, 0, duration, clientIp, userAgent, "websocket", "WS")
          handleWebSocketConnection(clientSocket, input, userAgent)
          return
        }
      }

      var requestBody = ""
      val contentLength = headersMap["content-length"]?.toIntOrNull() ?: 0
      if (contentLength > 0) {
        val bodyBytes = ByteArray(contentLength)
        var totalRead = 0
        while (totalRead < contentLength) {
          val read = input.read(bodyBytes, totalRead, contentLength - totalRead)
          if (read == -1) break
          totalRead += read
        }
        requestBody = String(bodyBytes, 0, totalRead, StandardCharsets.UTF_8)
      }

      if (rawMethod == "OPTIONS") {
        sendCorsPreflightResponse(output)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(rawMethod, rawPath, 204, 0, duration, clientIp, userAgent, "")
        output.flush()
        clientSocket.close()
        return
      }

      if (rawPath.startsWith("/api/")) {
        handleApiRequest(rawMethod, rawPath, headersMap, requestBody, output, clientIp, userAgent, requestStartTime)
        output.flush()
        clientSocket.close()
        return
      }

      handleStaticFileRequest(rawMethod, rawPath, headersMap, output, clientIp, userAgent, requestStartTime)
      output.flush()
      clientSocket.close()
    } catch (_: Exception) {
      try {
        clientSocket.close()
      } catch (_: Exception) {}
    }
  }

  private fun handleApiRequest(
    method: String,
    path: String,
    headers: Map<String, String>,
    body: String,
    output: OutputStream,
    clientIp: String,
    userAgent: String,
    requestStartTime: Long
  ) {
    val cleanPath = if (path.contains("?")) path.substring(0, path.indexOf("?")) else path
    val queryParams = extractQueryParams(path)

    when {
      cleanPath == "/api/status" -> {
        val json = JSONObject().apply {
          put("status", "running")
          put("port", port)
          put("uptimeSeconds", getUptimeSeconds())
          put("webSocketClients", wsClients.size)
          put("timestamp", System.currentTimeMillis())
        }.toString()
        sendJsonResponse(output, 200, json)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(method, path, 200, json.length.toLong(), duration, clientIp, userAgent, "application/json")
      }

      cleanPath == "/api/time" -> {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val json = JSONObject().apply {
          put("timestamp", System.currentTimeMillis())
          put("utcIso", sdf.format(Date()))
        }.toString()
        sendJsonResponse(output, 200, json)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(method, path, 200, json.length.toLong(), duration, clientIp, userAgent, "application/json")
      }

      cleanPath == "/api/echo" -> {
        val json = JSONObject().apply {
          put("method", method)
          put("path", path)
          put("clientIp", clientIp)
          put("headers", JSONObject(headers))
          put("query", JSONObject(queryParams))
          put("body", body)
        }.toString()
        sendJsonResponse(output, 200, json)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(method, path, 200, json.length.toLong(), duration, clientIp, userAgent, "application/json")
      }

      cleanPath == "/api/files" -> {
        val files = wwwDir.listFiles()?.map { it.name } ?: emptyList()
        val json = JSONObject().apply {
          put("files", files)
          put("count", files.size)
        }.toString()
        sendJsonResponse(output, 200, json)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(method, path, 200, json.length.toLong(), duration, clientIp, userAgent, "application/json")
      }

      cleanPath.startsWith("/api/db/") -> {
        val collection = cleanPath.removePrefix("/api/db/").trim()
        val id = queryParams["id"]

        when (method) {
          "GET" -> {
            if (id != null) {
              val item = databaseEngine.getItem(collection, id)
              val duration = System.currentTimeMillis() - requestStartTime
              if (item != null) {
                sendJsonResponse(output, 200, item)
                emitLog(method, path, 200, item.length.toLong(), duration, clientIp, userAgent, "application/json")
              } else {
                val err = JSONObject().apply { put("error", "Item not found") }.toString()
                sendJsonResponse(output, 404, err)
                emitLog(method, path, 404, err.length.toLong(), duration, clientIp, userAgent, "application/json")
              }
            } else {
              val items = databaseEngine.getCollection(collection)
              sendJsonResponse(output, 200, items)
              val duration = System.currentTimeMillis() - requestStartTime
              emitLog(method, path, 200, items.length.toLong(), duration, clientIp, userAgent, "application/json")
            }
          }

          "POST" -> {
            val inserted = databaseEngine.insertItem(collection, body.ifEmpty { "{}" })
            sendJsonResponse(output, 201, inserted)
            val duration = System.currentTimeMillis() - requestStartTime
            emitLog(method, path, 201, inserted.length.toLong(), duration, clientIp, userAgent, "application/json")
            broadcastWebSocketMessage(JSONObject().apply {
              put("event", "database_change")
              put("action", "insert")
              put("collection", collection)
              put("data", JSONObject(inserted))
            }.toString())
          }

          "PUT" -> {
            val duration = System.currentTimeMillis() - requestStartTime
            if (id != null) {
              val updated = databaseEngine.updateItem(collection, id, body.ifEmpty { "{}" })
              if (updated != null) {
                sendJsonResponse(output, 200, updated)
                emitLog(method, path, 200, updated.length.toLong(), duration, clientIp, userAgent, "application/json")
                broadcastWebSocketMessage(JSONObject().apply {
                  put("event", "database_change")
                  put("action", "update")
                  put("collection", collection)
                  put("data", JSONObject(updated))
                }.toString())
              } else {
                val err = JSONObject().apply { put("error", "Item not found") }.toString()
                sendJsonResponse(output, 404, err)
                emitLog(method, path, 404, err.length.toLong(), duration, clientIp, userAgent, "application/json")
              }
            } else {
              val err = JSONObject().apply { put("error", "Missing id query parameter") }.toString()
              sendJsonResponse(output, 400, err)
              emitLog(method, path, 400, err.length.toLong(), duration, clientIp, userAgent, "application/json")
            }
          }

          "DELETE" -> {
            val duration = System.currentTimeMillis() - requestStartTime
            if (id != null) {
              val deleted = databaseEngine.deleteItem(collection, id)
              val res = JSONObject().apply { put("success", deleted) }.toString()
              sendJsonResponse(output, if (deleted) 200 else 404, res)
              emitLog(method, path, if (deleted) 200 else 404, res.length.toLong(), duration, clientIp, userAgent, "application/json")
              if (deleted) {
                broadcastWebSocketMessage(JSONObject().apply {
                  put("event", "database_change")
                  put("action", "delete")
                  put("collection", collection)
                  put("id", id)
                }.toString())
              }
            } else {
              databaseEngine.clearCollection(collection)
              val res = JSONObject().apply { put("cleared", true) }.toString()
              sendJsonResponse(output, 200, res)
              emitLog(method, path, 200, res.length.toLong(), duration, clientIp, userAgent, "application/json")
            }
          }

          else -> {
            val err = JSONObject().apply { put("error", "Method not allowed") }.toString()
            sendJsonResponse(output, 405, err)
            val duration = System.currentTimeMillis() - requestStartTime
            emitLog(method, path, 405, err.length.toLong(), duration, clientIp, userAgent, "application/json")
          }
        }
      }

      else -> {
        val err = JSONObject().apply { put("error", "Endpoint not found") }.toString()
        sendJsonResponse(output, 404, err)
        val duration = System.currentTimeMillis() - requestStartTime
        emitLog(method, path, 404, err.length.toLong(), duration, clientIp, userAgent, "application/json")
      }
    }
  }

  private fun handleStaticFileRequest(
    method: String,
    path: String,
    headers: Map<String, String>,
    output: OutputStream,
    clientIp: String,
    userAgent: String,
    requestStartTime: Long
  ) {
    val cleanPath = try {
      val raw = if (path.contains("?")) path.substring(0, path.indexOf("?")) else path
      URLDecoder.decode(raw, "UTF-8")
    } catch (_: Exception) {
      "/"
    }

    var targetPath = cleanPath.trimStart('/')
    if (targetPath.isEmpty()) targetPath = "index.html"

    var targetFile = File(wwwDir, targetPath)
    if (targetFile.isDirectory) {
      val indexFile = File(targetFile, "index.html")
      if (indexFile.exists()) targetFile = indexFile
    }

    if (!targetFile.exists() || !targetFile.isFile) {
      val availableFiles = wwwDir.listFiles() ?: emptyArray()
      val responseHtml = if (availableFiles.isEmpty()) {
        "<!DOCTYPE html><html><head><meta charset='utf-8'><title>LocalHost Server</title>" +
          "<style>body{background:#000;color:#fff;font-family:monospace;padding:32px;}h1{font-size:18px;}p{color:#888;font-size:13px;}</style></head>" +
          "<body><h1>Server Aktif</h1><p>Direktori web kosong. Tambahkan berkas Anda melalui aplikasi.</p></body></html>"
      } else {
        val listItems = availableFiles.joinToString("") {
          "<li><a href='/${it.name}' style='color:#fff;'>${it.name}</a></li>"
        }
        "<!DOCTYPE html><html><head><meta charset='utf-8'><title>LocalHost Index</title>" +
          "<style>body{background:#000;color:#fff;font-family:monospace;padding:32px;}ul{line-height:2;}a{text-decoration:none;}</style></head>" +
          "<body><h1>Direktori Berkas:</h1><ul>$listItems</ul></body></html>"
      }

      val bytes = responseHtml.toByteArray(StandardCharsets.UTF_8)
      val statusCode = if (targetPath == "index.html") 200 else 404
      val statusText = if (statusCode == 200) "OK" else "Not Found"
      sendHttpResponse(output, statusCode, statusText, "text/html; charset=utf-8", bytes)
      val duration = System.currentTimeMillis() - requestStartTime
      emitLog(method, path, statusCode, bytes.size.toLong(), duration, clientIp, userAgent, "text/html")
      return
    }

    val mimeType = getMimeType(targetFile.name)
    val fileLength = targetFile.length()
    val rangeHeader = headers["range"]

    if (rangeHeader != null && rangeHeader.startsWith("bytes=") && method == "GET") {
      servePartialFile(targetFile, rangeHeader, mimeType, output, method, path, clientIp, userAgent, requestStartTime)
    } else {
      val headerStr = "HTTP/1.1 200 OK\r\n" +
        "Content-Type: $mimeType\r\n" +
        "Content-Length: $fileLength\r\n" +
        "Access-Control-Allow-Origin: *\r\n" +
        "Cache-Control: no-cache\r\n" +
        "Accept-Ranges: bytes\r\n\r\n"
      output.write(headerStr.toByteArray(StandardCharsets.UTF_8))

      if (method == "GET") {
        val fis = FileInputStream(targetFile)
        val buffer = ByteArray(8192)
        var read: Int
        while (fis.read(buffer).also { read = it } != -1) {
          output.write(buffer, 0, read)
        }
        fis.close()
      }

      val duration = System.currentTimeMillis() - requestStartTime
      emitLog(method, path, 200, fileLength, duration, clientIp, userAgent, mimeType)
    }
  }

  private fun servePartialFile(
    file: File,
    rangeHeader: String,
    mimeType: String,
    output: OutputStream,
    method: String,
    path: String,
    clientIp: String,
    userAgent: String,
    requestStartTime: Long
  ) {
    val fileLength = file.length()
    val range = rangeHeader.removePrefix("bytes=").trim()
    val rangeParts = range.split("-")
    val start = rangeParts[0].toLongOrNull() ?: 0L
    val end = if (rangeParts.size > 1 && rangeParts[1].isNotEmpty()) {
      rangeParts[1].toLongOrNull() ?: (fileLength - 1)
    } else {
      fileLength - 1
    }

    val contentLength = end - start + 1
    val headerStr = "HTTP/1.1 206 Partial Content\r\n" +
      "Content-Type: $mimeType\r\n" +
      "Content-Length: $contentLength\r\n" +
      "Content-Range: bytes $start-$end/$fileLength\r\n" +
      "Access-Control-Allow-Origin: *\r\n" +
      "Accept-Ranges: bytes\r\n\r\n"
    output.write(headerStr.toByteArray(StandardCharsets.UTF_8))

    val fis = FileInputStream(file)
    fis.skip(start)
    val buffer = ByteArray(8192)
    var remaining = contentLength
    while (remaining > 0) {
      val toRead = minOf(buffer.size.toLong(), remaining).toInt()
      val read = fis.read(buffer, 0, toRead)
      if (read == -1) break
      output.write(buffer, 0, read)
      remaining -= read
    }
    fis.close()

    val duration = System.currentTimeMillis() - requestStartTime
    emitLog(method, path, 206, contentLength, duration, clientIp, userAgent, mimeType)
  }

  private fun handleWebSocketConnection(clientSocket: Socket, input: InputStream, userAgent: String) {
    val clientIp = clientSocket.inetAddress?.hostAddress ?: "127.0.0.1"
    try {
      while (isRunning.get() && !clientSocket.isClosed) {
        val b1 = input.read()
        if (b1 == -1) break
        val b2 = input.read()
        if (b2 == -1) break

        val opcode = b1 and 0x0F
        if (opcode == 0x08) {
          break
        }

        val isMasked = (b2 and 0x80) != 0
        var payloadLen = (b2 and 0x7F).toLong()

        if (payloadLen == 126L) {
          val byte1 = input.read()
          val byte2 = input.read()
          if (byte1 == -1 || byte2 == -1) break
          payloadLen = ((byte1 shl 8) or byte2).toLong()
        } else if (payloadLen == 127L) {
          var len = 0L
          for (i in 0 until 8) {
            val byte = input.read()
            if (byte == -1) break
            len = (len shl 8) or (byte.toLong() and 0xFF)
          }
          payloadLen = len
        }

        val maskKey = ByteArray(4)
        if (isMasked) {
          var maskRead = 0
          while (maskRead < 4) {
            val r = input.read(maskKey, maskRead, 4 - maskRead)
            if (r == -1) break
            maskRead += r
          }
        }

        val payload = ByteArray(payloadLen.toInt())
        var totalPayloadRead = 0
        while (totalPayloadRead < payloadLen) {
          val r = input.read(payload, totalPayloadRead, payloadLen.toInt() - totalPayloadRead)
          if (r == -1) break
          totalPayloadRead += r
        }

        if (isMasked) {
          for (i in payload.indices) {
            payload[i] = (payload[i].toInt() xor maskKey[i % 4].toInt()).toByte()
          }
        }

        if (opcode == 0x01) {
          val message = String(payload, StandardCharsets.UTF_8)
          emitLog("WS_MSG", "/ws", 200, payload.size.toLong(), 0L, clientIp, userAgent, "text/plain", "WS")
          broadcastWebSocketMessage(JSONObject().apply {
            put("event", "message")
            put("client", clientIp)
            put("message", message)
            put("timestamp", System.currentTimeMillis())
          }.toString())
        }
      }
    } catch (_: Exception) {} finally {
      wsClients.remove(clientSocket)
      onClientsCountChanged(wsClients.size)
      try {
        clientSocket.close()
      } catch (_: Exception) {}
    }
  }

  private fun createWebSocketFrame(payload: ByteArray): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.write(0x81)

    if (payload.size <= 125) {
      baos.write(payload.size)
    } else if (payload.size <= 65535) {
      baos.write(126)
      baos.write((payload.size shr 8) and 0xFF)
      baos.write(payload.size and 0xFF)
    } else {
      baos.write(127)
      for (i in 7 downTo 0) {
        baos.write(((payload.size.toLong() shr (i * 8)) and 0xFF).toInt())
      }
    }
    baos.write(payload)
    return baos.toByteArray()
  }

  private fun computeSecWebSocketAccept(key: String): String {
    val magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
    val digest = MessageDigest.getInstance("SHA-1").digest((key + magic).toByteArray(StandardCharsets.UTF_8))
    return Base64.encodeToString(digest, Base64.NO_WRAP)
  }

  private fun sendJsonResponse(output: OutputStream, statusCode: Int, json: String) {
    val bytes = json.toByteArray(StandardCharsets.UTF_8)
    val statusText = when (statusCode) {
      200 -> "OK"
      201 -> "Created"
      204 -> "No Content"
      400 -> "Bad Request"
      404 -> "Not Found"
      405 -> "Method Not Allowed"
      else -> "Internal Server Error"
    }
    sendHttpResponse(output, statusCode, statusText, "application/json; charset=utf-8", bytes)
  }

  private fun sendCorsPreflightResponse(output: OutputStream) {
    val headerStr = "HTTP/1.1 204 No Content\r\n" +
      "Access-Control-Allow-Origin: *\r\n" +
      "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS\r\n" +
      "Access-Control-Allow-Headers: *\r\n" +
      "Content-Length: 0\r\n\r\n"
    output.write(headerStr.toByteArray(StandardCharsets.UTF_8))
  }

  private fun sendHttpResponse(
    output: OutputStream,
    statusCode: Int,
    statusText: String,
    contentType: String,
    body: ByteArray
  ) {
    val headerStr = "HTTP/1.1 $statusCode $statusText\r\n" +
      "Content-Type: $contentType\r\n" +
      "Content-Length: ${body.size}\r\n" +
      "Access-Control-Allow-Origin: *\r\n" +
      "Cache-Control: no-cache\r\n\r\n"
    output.write(headerStr.toByteArray(StandardCharsets.UTF_8))
    output.write(body)
  }

  private fun readRequestHeaders(input: InputStream): List<String> {
    val headers = mutableListOf<String>()
    val currentLine = StringBuilder()
    var prevChar = 0

    while (true) {
      val c = input.read()
      if (c == -1) break
      if (c == '\n'.code && prevChar == '\r'.code) {
        val line = currentLine.toString().trimEnd('\r')
        if (line.isEmpty()) break
        headers.add(line)
        currentLine.clear()
      } else {
        currentLine.append(c.toChar())
      }
      prevChar = c
    }
    return headers
  }

  private fun extractQueryParams(path: String): Map<String, String> {
    val params = mutableMapOf<String, String>()
    val questionIdx = path.indexOf("?")
    if (questionIdx != -1 && questionIdx < path.length - 1) {
      val query = path.substring(questionIdx + 1)
      val pairs = query.split("&")
      for (pair in pairs) {
        val eqIdx = pair.indexOf("=")
        if (eqIdx != -1) {
          val key = URLDecoder.decode(pair.substring(0, eqIdx), "UTF-8")
          val value = URLDecoder.decode(pair.substring(eqIdx + 1), "UTF-8")
          params[key] = value
        }
      }
    }
    return params
  }

  private fun emitLog(
    method: String,
    path: String,
    statusCode: Int,
    responseSize: Long,
    durationMs: Long,
    clientIp: String,
    userAgent: String = "",
    contentType: String = "",
    protocol: String = "HTTP/1.1"
  ) {
    onLog(
      AccessLog(
        id = UUID.randomUUID().toString(),
        timestamp = System.currentTimeMillis(),
        method = method,
        path = path,
        statusCode = statusCode,
        responseSize = responseSize,
        durationMs = durationMs,
        clientIp = clientIp,
        userAgent = userAgent,
        contentType = contentType,
        protocol = protocol
      )
    )
  }

  private fun getMimeType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    return when (ext) {
      "html", "htm" -> "text/html; charset=utf-8"
      "css" -> "text/css; charset=utf-8"
      "js", "mjs" -> "application/javascript; charset=utf-8"
      "json" -> "application/json; charset=utf-8"
      "png" -> "image/png"
      "jpg", "jpeg" -> "image/jpeg"
      "gif" -> "image/gif"
      "svg" -> "image/svg+xml"
      "webp" -> "image/webp"
      "ico" -> "image/x-icon"
      "woff" -> "font/woff"
      "woff2" -> "font/woff2"
      "ttf" -> "font/ttf"
      "otf" -> "font/otf"
      "mp4" -> "video/mp4"
      "webm" -> "video/webm"
      "mp3" -> "audio/mpeg"
      "wav" -> "audio/wav"
      "pdf" -> "application/pdf"
      "txt" -> "text/plain; charset=utf-8"
      "wasm" -> "application/wasm"
      "xml" -> "application/xml"
      else -> "application/octet-stream"
    }
  }
}
