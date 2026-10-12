package com.example.ui.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.viewmodel.ServerViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewScreen(
  viewModel: ServerViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val status by viewModel.serverStatus.collectAsState()
  var webViewRef: WebView? = remember { null }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(PureBlack)
      .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkCard, RoundedCornerShape(8.dp))
        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
        .padding(horizontal = 6.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { viewModel.setShowPreview(false) },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = PureWhite, modifier = Modifier.size(20.dp))
      }

      IconButton(
        onClick = { if (webViewRef?.canGoBack() == true) webViewRef?.goBack() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mundur", tint = PureWhite, modifier = Modifier.size(18.dp))
      }

      IconButton(
        onClick = { if (webViewRef?.canGoForward() == true) webViewRef?.goForward() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Maju", tint = PureWhite, modifier = Modifier.size(18.dp))
      }

      IconButton(
        onClick = { webViewRef?.reload() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = PureWhite, modifier = Modifier.size(18.dp))
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .background(Color(0xFF0A0A0A), RoundedCornerShape(4.dp))
          .border(1.dp, Color(0xFF222222), RoundedCornerShape(4.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Text(
          text = status.localUrl,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          color = PureWhite,
          maxLines = 1
        )
      }

      IconButton(
        onClick = {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(status.localUrl))
          context.startActivity(intent)
        },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.OpenInBrowser, contentDescription = "Buka Eksternal", tint = PureWhite, modifier = Modifier.size(18.dp))
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .background(PureBlack, RoundedCornerShape(8.dp))
        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
    ) {
      AndroidView(
        factory = { ctx ->
          WebView(ctx).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              allowContentAccess = true
              allowFileAccess = true
              cacheMode = WebSettings.LOAD_NO_CACHE
              useWideViewPort = true
              loadWithOverviewMode = true
              if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
              }
            }
            webViewClient = object : WebViewClient() {
              override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                  view?.loadUrl(url)
                  return true
                }
                return false
              }
            }
            webChromeClient = WebChromeClient()
            loadUrl(status.localUrl)
            webViewRef = this
          }
        },
        update = { wv ->
          webViewRef = wv
          if (wv.url == null || !wv.url.equals(status.localUrl)) {
            wv.loadUrl(status.localUrl)
          }
        },
        modifier = Modifier
          .fillMaxSize()
          .testTag("in_app_webview")
      )
    }
  }
}
