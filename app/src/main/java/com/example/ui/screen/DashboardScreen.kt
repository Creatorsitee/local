package com.example.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.util.QrCodeGenerator
import com.example.viewmodel.ServerViewModel

@Composable
fun DashboardScreen(
  viewModel: ServerViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val status by viewModel.serverStatus.collectAsState()
  var portInput by remember(status.port) { mutableStateOf(status.port.toString()) }

  val qrBitmap = remember(status.lanUrl) {
    if (status.lanUrl.isNotEmpty()) {
      QrCodeGenerator.generateQrBitmap(status.lanUrl, 420)
    } else null
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PureBlack)
      .verticalScroll(scrollState)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "LocalHost Server",
          style = MaterialTheme.typography.titleLarge,
          color = PureWhite,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = if (status.isRunning) "Berjalan di port ${status.port} (${status.uptimeSeconds}s)" else "Server tidak aktif",
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF888888)
        )
      }

      Box(
        modifier = Modifier
          .background(if (status.isRunning) PureWhite else Color(0xFF1E1E1E), RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(if (status.isRunning) PureBlack else Color(0xFF666666), CircleShape)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (status.isRunning) "AKTIF" else "MATI",
            color = if (status.isRunning) PureBlack else Color(0xFF888888),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkCard, RoundedCornerShape(12.dp))
        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
        .padding(18.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
          onClick = {
            if (status.isRunning) {
              viewModel.stopServer()
            } else {
              val parsed = portInput.toIntOrNull() ?: 8080
              viewModel.startServer(parsed)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (status.isRunning) Color(0xFF222222) else PureWhite,
            contentColor = if (status.isRunning) PureWhite else PureBlack
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("server_toggle_button")
        ) {
          Icon(
            imageVector = if (status.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (status.isRunning) "Hentikan Server" else "Mulai Server",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = portInput,
            onValueChange = { portInput = it.filter { char -> char.isDigit() }.take(5) },
            label = { Text("Port Server", color = Color(0xFF888888), fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PureWhite,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = PureWhite,
              unfocusedTextColor = PureWhite,
              focusedContainerColor = DarkSurface,
              unfocusedContainerColor = DarkSurface
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("port_input_field")
          )

          OutlinedButton(
            onClick = {
              val p = portInput.toIntOrNull() ?: 8080
              viewModel.setPort(p)
              if (status.isRunning) {
                viewModel.startServer(p)
              }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(56.dp)
          ) {
            Text("Simpan Port", fontSize = 13.sp)
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkCard, RoundedCornerShape(12.dp))
        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
        .padding(18.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "ALAMAT WEBSITE",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFAAAAAA),
          letterSpacing = 0.5.sp
        )

        AddressRow(
          label = "Localhost (Perangkat ini)",
          url = status.localUrl,
          onCopy = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("URL", status.localUrl))
          },
          onOpen = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.localUrl))
            context.startActivity(intent)
          }
        )

        AddressRow(
          label = "Jaringan WiFi (Bisa diakses HP/Laptop lain)",
          url = status.lanUrl,
          onCopy = {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("URL", status.lanUrl))
          },
          onOpen = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.lanUrl))
            context.startActivity(intent)
          },
          onRefresh = {
            viewModel.refreshLanIp()
          }
        )
      }
    }

    if (qrBitmap != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkCard, RoundedCornerShape(12.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = "QR CODE AKSES JARINGAN LOKAL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAAAAAA),
            letterSpacing = 0.5.sp
          )

          Box(
            modifier = Modifier
              .background(PureWhite, RoundedCornerShape(8.dp))
              .padding(12.dp)
          ) {
            Image(
              bitmap = qrBitmap.asImageBitmap(),
              contentDescription = "QR Code",
              modifier = Modifier
                .size(200.dp)
                .testTag("lan_qr_code")
            )
          }

          Text(
            text = status.lanUrl,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = Color(0xFF888888)
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { viewModel.setShowPreview(true) },
        colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("preview_tab_button")
      ) {
        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Pratinjau", fontWeight = FontWeight.Bold, fontSize = 13.sp)
      }

      OutlinedButton(
        onClick = {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.localUrl))
          context.startActivity(intent)
        },
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
        border = BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("open_external_browser_button")
      ) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Buka di Browser", fontSize = 13.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
  }
}

@Composable
fun AddressRow(
  label: String,
  url: String,
  onCopy: () -> Unit,
  onOpen: () -> Unit,
  onRefresh: (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkSurface, RoundedCornerShape(8.dp))
      .border(1.dp, Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = label, fontSize = 12.sp, color = Color(0xFF888888))
      if (onRefresh != null) {
        IconButton(onClick = onRefresh, modifier = Modifier.size(24.dp)) {
          Icon(Icons.Default.Refresh, contentDescription = "Segarkan", tint = Color(0xFFAAAAAA), modifier = Modifier.size(16.dp))
        }
      }
    }

    Text(
      text = url.ifEmpty { "Menunggu jaringan..." },
      fontFamily = FontFamily.Monospace,
      fontSize = 14.sp,
      fontWeight = FontWeight.SemiBold,
      color = PureWhite
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = PureWhite, modifier = Modifier.size(18.dp))
      }
      IconButton(onClick = onOpen, modifier = Modifier.size(36.dp)) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = "Buka", tint = PureWhite, modifier = Modifier.size(18.dp))
      }
    }
  }
}
