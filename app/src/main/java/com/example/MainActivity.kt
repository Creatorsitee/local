package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ServerTab
import com.example.ui.screen.AccessLogsScreen
import com.example.ui.screen.DashboardScreen
import com.example.ui.screen.FileManagerScreen
import com.example.ui.screen.PreviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.viewmodel.ServerViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: ServerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainApp(viewModel: ServerViewModel) {
  val activeTab by viewModel.activeTab.collectAsState()
  val showPreview by viewModel.showPreview.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearToast()
    }
  }

  BackHandler(enabled = showPreview || selectedFile != null || activeTab != ServerTab.SERVER) {
    when {
      showPreview -> viewModel.setShowPreview(false)
      selectedFile != null -> viewModel.closeEditor()
      activeTab != ServerTab.SERVER -> viewModel.setActiveTab(ServerTab.SERVER)
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = {
      NavigationBar(
        containerColor = PureBlack,
        contentColor = PureWhite,
        tonalElevation = 0.dp,
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .background(PureBlack)
          .testTag("main_navigation_bar")
      ) {
        NavigationBarItem(
          selected = activeTab == ServerTab.SERVER,
          onClick = { viewModel.setActiveTab(ServerTab.SERVER) },
          icon = { Icon(Icons.Default.Dns, contentDescription = "Server") },
          label = { Text("Server", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PureBlack,
            unselectedIconColor = Color(0xFF777777),
            selectedTextColor = PureWhite,
            unselectedTextColor = Color(0xFF777777),
            indicatorColor = PureWhite
          )
        )

        NavigationBarItem(
          selected = activeTab == ServerTab.FILES,
          onClick = { viewModel.setActiveTab(ServerTab.FILES) },
          icon = { Icon(Icons.Default.Folder, contentDescription = "Berkas") },
          label = { Text("Berkas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PureBlack,
            unselectedIconColor = Color(0xFF777777),
            selectedTextColor = PureWhite,
            unselectedTextColor = Color(0xFF777777),
            indicatorColor = PureWhite
          )
        )

        NavigationBarItem(
          selected = activeTab == ServerTab.LOGS,
          onClick = { viewModel.setActiveTab(ServerTab.LOGS) },
          icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Log") },
          label = { Text("Log", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = PureBlack,
            unselectedIconColor = Color(0xFF777777),
            selectedTextColor = PureWhite,
            unselectedTextColor = Color(0xFF777777),
            indicatorColor = PureWhite
          )
        )
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(PureBlack)
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
      when (activeTab) {
        ServerTab.SERVER -> DashboardScreen(viewModel = viewModel)
        ServerTab.FILES -> FileManagerScreen(viewModel = viewModel)
        ServerTab.LOGS -> AccessLogsScreen(viewModel = viewModel)
      }
    }
  }

  if (showPreview) {
    Dialog(
      onDismissRequest = { viewModel.setShowPreview(false) },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(PureBlack)
          .windowInsetsPadding(WindowInsets.statusBars)
          .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        PreviewScreen(viewModel = viewModel)
      }
    }
  }
}
