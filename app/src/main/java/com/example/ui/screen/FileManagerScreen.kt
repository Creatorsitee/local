package com.example.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WebFile
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.util.SyntaxHighlighter
import com.example.viewmodel.ServerViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileManagerScreen(
  viewModel: ServerViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentDir by viewModel.currentDirectory.collectAsState()
  val files by viewModel.currentFiles.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val fileContent by viewModel.fileContent.collectAsState()
  val isSaving by viewModel.isSaving.collectAsState()
  val isGitLoading by viewModel.isGitLoading.collectAsState()
  val gitRepoUrl by viewModel.gitRepoUrl.collectAsState()

  var showNewFileDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var showGitCloneDialog by remember { mutableStateOf(false) }
  var fileToDelete by remember { mutableStateOf<File?>(null) }
  var isLineWrapEnabled by remember { mutableStateOf(false) }

  var inputNewFileName by remember { mutableStateOf("") }
  var inputNewFolderName by remember { mutableStateOf("") }
  var inputGitUrl by remember(gitRepoUrl) { mutableStateOf(gitRepoUrl) }

  val symbols = listOf("<", ">", "/", "\"", "'", "{", "}", "(", ")", "[", "]", ";", "=", "!", "-", "_", "&", "$", ":", "?")

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val contentResolver = context.contentResolver
      val inputStream = contentResolver.openInputStream(it)
      if (inputStream != null) {
        var fileName = "file"
        contentResolver.query(it, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
          if (cursor.moveToFirst() && nameIndex >= 0) {
            fileName = cursor.getString(nameIndex)
          }
        }
        if (fileName.endsWith(".zip", ignoreCase = true)) {
          viewModel.importZip(inputStream)
        } else {
          viewModel.importFile(inputStream, fileName)
        }
      }
    }
  }

  if (selectedFile != null) {
    val fileExtension = selectedFile?.extension ?: "txt"
    val visualTransformation = remember(fileExtension) {
      SyntaxHighlighter.createVisualTransformation(fileExtension)
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val linesCount = fileContent.count { it == '\n' } + 1
    val charCount = fileContent.length

    Column(
      modifier = modifier
        .fillMaxSize()
        .background(PureBlack)
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { viewModel.closeEditor() },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = PureWhite)
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = selectedFile?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .background(Color(0xFF222222), RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = fileExtension.uppercase(Locale.ROOT),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = PureWhite
                )
              }
            }
            Text(
              text = "$linesCount baris | $charCount karakter",
              style = MaterialTheme.typography.bodySmall,
              color = Color(0xFF888888),
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          IconButton(
            onClick = { isLineWrapEnabled = !isLineWrapEnabled },
            modifier = Modifier
              .size(36.dp)
              .background(if (isLineWrapEnabled) Color(0xFF2A2A2A) else Color.Transparent, RoundedCornerShape(6.dp))
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft,
              contentDescription = "Wrap Teks",
              tint = if (isLineWrapEnabled) PureWhite else Color(0xFF888888),
              modifier = Modifier.size(18.dp)
            )
          }

          Button(
            onClick = { viewModel.saveCurrentFile() },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.testTag("save_file_button")
          ) {
            if (isSaving) {
              CircularProgressIndicator(color = PureBlack, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkCard, RoundedCornerShape(6.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        symbols.forEach { sym ->
          Box(
            modifier = Modifier
              .background(DarkSurface, RoundedCornerShape(4.dp))
              .border(1.dp, Color(0xFF282828), RoundedCornerShape(4.dp))
              .clickable { viewModel.updateFileContent(fileContent + sym) }
              .padding(horizontal = 10.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = sym,
              color = PureWhite,
              fontFamily = FontFamily.Monospace,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      val lineNumbersText = (1..linesCount).joinToString("\n")

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(DarkCard, RoundedCornerShape(8.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
          .padding(8.dp)
      ) {
        Row(modifier = Modifier.fillMaxSize()) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .verticalScroll(verticalScrollState)
              .padding(end = 8.dp)
          ) {
            Text(
              text = lineNumbersText,
              color = Color(0xFF555555),
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              lineHeight = 20.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          Box(
            modifier = Modifier
              .width(1.dp)
              .fillMaxHeight()
              .background(Color(0xFF222222))
          )

          val editorModifier = if (isLineWrapEnabled) {
            Modifier
              .fillMaxSize()
              .padding(start = 8.dp)
              .verticalScroll(verticalScrollState)
          } else {
            Modifier
              .fillMaxSize()
              .padding(start = 8.dp)
              .verticalScroll(verticalScrollState)
              .horizontalScroll(horizontalScrollState)
          }

          Box(modifier = editorModifier) {
            BasicTextField(
              value = fileContent,
              onValueChange = { viewModel.updateFileContent(it) },
              textStyle = TextStyle(
                color = PureWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 20.sp
              ),
              cursorBrush = SolidColor(PureWhite),
              visualTransformation = visualTransformation,
              modifier = Modifier
                .fillMaxSize()
                .testTag("code_editor_field")
            )
          }
        }
      }
    }
  } else {
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
            text = "Berkas Website",
            style = MaterialTheme.typography.titleLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Kelola direktori & berkas lokal",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF888888)
          )
        }

        if (isGitLoading) {
          CircularProgressIndicator(
            color = PureWhite,
            strokeWidth = 2.dp,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(DarkCard, RoundedCornerShape(8.dp))
          .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.navigateUp() },
          enabled = currentDir != viewModel.fileManagerService.webRootDir,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Folder Naik",
            tint = if (currentDir != viewModel.fileManagerService.webRootDir) PureWhite else Color(0xFF444444)
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "/" + currentDir.relativeTo(viewModel.fileManagerService.webRootDir).path.replace(".", ""),
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp,
          color = PureWhite,
          modifier = Modifier.weight(1f)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = {
            inputNewFileName = ""
            showNewFileDialog = true
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
          border = BorderStroke(1.dp, DarkBorder),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ File", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = {
            inputNewFolderName = ""
            showNewFolderDialog = true
          },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
          border = BorderStroke(1.dp, DarkBorder),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Folder", fontSize = 12.sp)
        }

        OutlinedButton(
          onClick = { filePickerLauncher.launch("*/*") },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
          border = BorderStroke(1.dp, DarkBorder),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Impor", fontSize = 12.sp)
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { showGitCloneDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = PureWhite, contentColor = PureBlack),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Git Clone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = { viewModel.pullGitRepo() },
          enabled = !isGitLoading,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
          border = BorderStroke(1.dp, DarkBorder),
          shape = RoundedCornerShape(6.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Git Pull", fontSize = 12.sp)
        }
      }

      if (files.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(DarkCard, RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF444444), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Belum ada berkas", color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tambahkan berkas baru, impor dari penyimpanan, atau klon repositori Git.",
              color = Color(0xFF777777),
              fontSize = 12.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(DarkCard, RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .padding(4.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          items(files, key = { it.file.absolutePath }) { webFile ->
            FileItemRow(
              webFile = webFile,
              onClick = {
                if (webFile.isDirectory) {
                  viewModel.navigateToDirectory(webFile.file)
                } else {
                  viewModel.loadFile(webFile.file)
                }
              },
              onDelete = {
                fileToDelete = webFile.file
              }
            )
          }
        }
      }
    }
  }

  if (showNewFileDialog) {
    AlertDialog(
      onDismissRequest = { showNewFileDialog = false },
      title = { Text("Buat Berkas Baru", color = PureWhite) },
      text = {
        OutlinedTextField(
          value = inputNewFileName,
          onValueChange = { inputNewFileName = it },
          label = { Text("Nama Berkas (misal: index.html)", color = Color(0xFF888888)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PureWhite,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
          ),
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (inputNewFileName.isNotBlank()) {
              viewModel.createNewFile(inputNewFileName.trim())
              showNewFileDialog = false
            }
          }
        ) {
          Text("Buat", color = PureWhite, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFileDialog = false }) {
          Text("Batal", color = Color(0xFF888888))
        }
      },
      containerColor = DarkSurface
    )
  }

  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = { Text("Buat Folder Baru", color = PureWhite) },
      text = {
        OutlinedTextField(
          value = inputNewFolderName,
          onValueChange = { inputNewFolderName = it },
          label = { Text("Nama Folder", color = Color(0xFF888888)) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PureWhite,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = PureWhite,
            unfocusedTextColor = PureWhite
          ),
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (inputNewFolderName.isNotBlank()) {
              viewModel.createNewFolder(inputNewFolderName.trim())
              showNewFolderDialog = false
            }
          }
        ) {
          Text("Buat", color = PureWhite, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) {
          Text("Batal", color = Color(0xFF888888))
        }
      },
      containerColor = DarkSurface
    )
  }

  if (showGitCloneDialog) {
    AlertDialog(
      onDismissRequest = { showGitCloneDialog = false },
      title = { Text("Git Clone", color = PureWhite) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "URL repository publik (GitHub / GitLab):",
            fontSize = 13.sp,
            color = Color(0xFFAAAAAA)
          )
          OutlinedTextField(
            value = inputGitUrl,
            onValueChange = { inputGitUrl = it },
            placeholder = { Text("https://github.com/user/repo", color = Color(0xFF666666)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = PureWhite,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = PureWhite,
              unfocusedTextColor = PureWhite
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (inputGitUrl.isNotBlank()) {
              viewModel.cloneGitRepo(inputGitUrl.trim())
              showGitCloneDialog = false
            }
          }
        ) {
          Text("Klon", color = PureWhite, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showGitCloneDialog = false }) {
          Text("Batal", color = Color(0xFF888888))
        }
      },
      containerColor = DarkSurface
    )
  }

  if (fileToDelete != null) {
    AlertDialog(
      onDismissRequest = { fileToDelete = null },
      title = { Text("Hapus Berkas?", color = PureWhite) },
      text = {
        Text(
          text = "Hapus '${fileToDelete?.name}' secara permanen?",
          color = Color(0xFFCCCCCC),
          fontSize = 13.sp
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            fileToDelete?.let { viewModel.deleteTarget(it) }
            fileToDelete = null
          }
        ) {
          Text("Hapus", color = Color(0xFFFF5555), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { fileToDelete = null }) {
          Text("Batal", color = Color(0xFF888888))
        }
      },
      containerColor = DarkSurface
    )
  }
}

@Composable
fun FileItemRow(
  webFile: WebFile,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormatted = remember(webFile.lastModified) {
    val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    sdf.format(Date(webFile.lastModified))
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (webFile.isDirectory) Icons.Default.Folder else Icons.Default.Description,
      contentDescription = null,
      tint = if (webFile.isDirectory) PureWhite else Color(0xFFAAAAAA),
      modifier = Modifier.size(20.dp)
    )

    Spacer(modifier = Modifier.width(10.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = webFile.name,
        color = PureWhite,
        fontSize = 14.sp,
        fontWeight = if (webFile.isDirectory) FontWeight.SemiBold else FontWeight.Normal
      )
      Text(
        text = if (webFile.isDirectory) "Folder" else "${formatFileSize(webFile.size)} - $dateFormatted",
        color = Color(0xFF777777),
        fontSize = 11.sp
      )
    }

    IconButton(
      onClick = onDelete,
      modifier = Modifier.size(32.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Hapus",
        tint = Color(0xFF666666),
        modifier = Modifier.size(16.dp)
      )
    }
  }
}

fun formatFileSize(bytes: Long): String {
  if (bytes < 1024) return "$bytes B"
  val kb = bytes / 1024.0
  if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
  val mb = kb / 1024.0
  return String.format(Locale.US, "%.1f MB", mb)
}
