package com.example.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AccessLog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.viewmodel.ServerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccessLogsScreen(
  viewModel: ServerViewModel,
  modifier: Modifier = Modifier
) {
  val logs by viewModel.accessLogs.collectAsState()
  var filter by remember { mutableStateOf("ALL") }
  var selectedLogDetail by remember { mutableStateOf<AccessLog?>(null) }
  val listState = rememberLazyListState()

  val filteredLogs = remember(logs, filter) {
    when (filter) {
      "2XX" -> logs.filter { it.statusCode in 200..299 }
      "ERR" -> logs.filter { it.statusCode >= 400 }
      "API" -> logs.filter { it.path.startsWith("/api/") }
      "STATIC" -> logs.filter { !it.path.startsWith("/api/") && it.method != "WS" && it.method != "WS_MSG" }
      else -> logs
    }
  }

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) {
      listState.scrollToItem(0)
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PureBlack)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Log Akses Real-Time",
          style = MaterialTheme.typography.titleLarge,
          color = PureWhite,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${logs.size} permintaan terekam",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF888888)
        )
      }

      Button(
        onClick = { viewModel.clearLogs() },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222), contentColor = PureWhite),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.testTag("clear_logs_button")
      ) {
        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Bersihkan", color = PureWhite, fontSize = 12.sp)
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterTabPill(label = "Semua", isSelected = filter == "ALL") { filter = "ALL" }
      FilterTabPill(label = "2xx Sukses", isSelected = filter == "2XX") { filter = "2XX" }
      FilterTabPill(label = "Galat", isSelected = filter == "ERR") { filter = "ERR" }
      FilterTabPill(label = "API", isSelected = filter == "API") { filter = "API" }
      FilterTabPill(label = "Statis", isSelected = filter == "STATIC") { filter = "STATIC" }
    }

    if (filteredLogs.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(DarkCard, RoundedCornerShape(8.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF555555), modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("Belum ada log akses terekam", color = Color(0xFF777777), fontSize = 13.sp)
        }
      }
    } else {
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(DarkCard, RoundedCornerShape(8.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
          .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(filteredLogs, key = { it.id }) { log ->
          LogItemCard(
            log = log,
            onClick = { selectedLogDetail = log }
          )
        }
      }
    }
  }

  selectedLogDetail?.let { log ->
    val fullDate = remember(log.timestamp) {
      val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
      sdf.format(Date(log.timestamp))
    }

    AlertDialog(
      onDismissRequest = { selectedLogDetail = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .background(PureWhite, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(text = log.method, color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Detail Permintaan", color = PureWhite, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DetailRow(label = "Path", value = log.path)
          DetailRow(label = "Status", value = "${log.statusCode} (${getStatusText(log.statusCode)})")
          DetailRow(label = "Durasi Latensi", value = "${log.durationMs} ms")
          DetailRow(label = "Ukuran Data", value = "${log.responseSize} byte")
          DetailRow(label = "IP Klien", value = log.clientIp)
          DetailRow(label = "Waktu Lengkap", value = fullDate)
          if (log.contentType.isNotEmpty()) {
            DetailRow(label = "Content-Type", value = log.contentType)
          }
          if (log.userAgent.isNotEmpty()) {
            DetailRow(label = "User-Agent", value = log.userAgent)
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedLogDetail = null }) {
          Text("Tutup", color = PureWhite, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface
    )
  }
}

@Composable
fun DetailRow(label: String, value: String) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(text = label, color = Color(0xFF888888), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    Text(
      text = value,
      color = PureWhite,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}

@Composable
fun FilterTabPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .background(if (isSelected) PureWhite else DarkSurface, RoundedCornerShape(6.dp))
      .border(1.dp, if (isSelected) PureWhite else DarkBorder, RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) PureBlack else PureWhite,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun LogItemCard(log: AccessLog, onClick: () -> Unit) {
  val timeFormatted = remember(log.timestamp) {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    sdf.format(Date(log.timestamp))
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurface, RoundedCornerShape(6.dp))
      .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
      .clickable(onClick = onClick)
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .background(
          when (log.method) {
            "GET" -> PureWhite
            "POST" -> Color(0xFFDDDDDD)
            "PUT" -> Color(0xFFBBBBBB)
            "DELETE" -> Color(0xFF999999)
            "WS", "WS_MSG" -> Color(0xFFCCCCCC)
            else -> PureWhite
          },
          RoundedCornerShape(4.dp)
        )
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = log.method,
        color = PureBlack,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Box(
      modifier = Modifier
        .background(
          when {
            log.statusCode in 200..299 || log.statusCode == 101 -> Color(0xFF1E1E1E)
            log.statusCode in 300..399 -> Color(0xFF282828)
            else -> Color(0xFF441111)
          },
          RoundedCornerShape(4.dp)
        )
        .border(
          1.dp,
          if (log.statusCode >= 400) Color(0xFFFF5555) else Color(0xFF333333),
          RoundedCornerShape(4.dp)
        )
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = log.statusCode.toString(),
        color = if (log.statusCode >= 400) Color(0xFFFF5555) else PureWhite,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = log.path,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = PureWhite
      )
      Text(
        text = "$timeFormatted | ${log.durationMs}ms | ${log.clientIp} | ${log.responseSize} B",
        color = Color(0xFF777777),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

fun getStatusText(code: Int): String {
  return when (code) {
    200 -> "OK"
    201 -> "Created"
    204 -> "No Content"
    206 -> "Partial Content"
    304 -> "Not Modified"
    400 -> "Bad Request"
    404 -> "Not Found"
    405 -> "Method Not Allowed"
    500 -> "Internal Error"
    101 -> "Switching Protocols"
    else -> "HTTP Response"
  }
}
