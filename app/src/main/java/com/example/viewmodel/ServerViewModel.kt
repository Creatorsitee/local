package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AccessLog
import com.example.model.ServerStatus
import com.example.model.ServerTab
import com.example.model.WebFile
import com.example.server.DatabaseEngine
import com.example.server.FullstackServer
import com.example.service.FileManagerService
import com.example.service.GitService
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class ServerViewModel(application: Application) : AndroidViewModel(application) {

  val fileManagerService = FileManagerService(application)
  val databaseEngine = DatabaseEngine(fileManagerService.dataDir)
  private val gitService = GitService(fileManagerService)

  private var activeServer: FullstackServer? = null

  private val _serverStatus = MutableStateFlow(
    ServerStatus(
      isRunning = false,
      port = 8080,
      localUrl = "http://127.0.0.1:8080",
      lanUrl = "http://${NetworkUtils.getLocalIpAddress(application)}:8080"
    )
  )
  val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

  private val _accessLogs = MutableStateFlow<List<AccessLog>>(emptyList())
  val accessLogs: StateFlow<List<AccessLog>> = _accessLogs.asStateFlow()

  private val _currentDirectory = MutableStateFlow(fileManagerService.webRootDir)
  val currentDirectory: StateFlow<File> = _currentDirectory.asStateFlow()

  private val _currentFiles = MutableStateFlow<List<WebFile>>(emptyList())
  val currentFiles: StateFlow<List<WebFile>> = _currentFiles.asStateFlow()

  private val _openFiles = MutableStateFlow<List<File>>(emptyList())
  val openFiles: StateFlow<List<File>> = _openFiles.asStateFlow()

  private val _selectedFile = MutableStateFlow<File?>(null)
  val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

  private val fileBuffers = mutableMapOf<String, String>()

  private val _fileContent = MutableStateFlow("")
  val fileContent: StateFlow<String> = _fileContent.asStateFlow()

  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  private val _activeTab = MutableStateFlow(ServerTab.SERVER)
  val activeTab: StateFlow<ServerTab> = _activeTab.asStateFlow()

  private val _gitRepoUrl = MutableStateFlow("")
  val gitRepoUrl: StateFlow<String> = _gitRepoUrl.asStateFlow()

  private val _isGitLoading = MutableStateFlow(false)
  val isGitLoading: StateFlow<Boolean> = _isGitLoading.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _showPreview = MutableStateFlow(false)
  val showPreview: StateFlow<Boolean> = _showPreview.asStateFlow()

  init {
    fileManagerService.ensureDefaultSite()
    refreshFiles()
    startServer(8080)
    startUptimeTicker()
  }

  fun resetDefaultSite() {
    val success = fileManagerService.resetDefaultSite()
    if (success) {
      refreshFiles()
      _toastMessage.value = "Website dibuat ulang dengan tema hitam putih"
    }
  }

  private fun startUptimeTicker() {
    viewModelScope.launch {
      while (isActive) {
        delay(1000)
        if (_serverStatus.value.isRunning && activeServer != null) {
          _serverStatus.update {
            it.copy(uptimeSeconds = activeServer?.getUptimeSeconds() ?: 0L)
          }
        }
      }
    }
  }

  fun setActiveTab(tab: ServerTab) {
    _activeTab.value = tab
    if (tab != ServerTab.FILES) {
      _selectedFile.value = null
    }
  }

  fun setShowPreview(show: Boolean) {
    _showPreview.value = show
  }

  fun setPort(port: Int) {
    val localIp = NetworkUtils.getLocalIpAddress(getApplication())
    _serverStatus.update {
      it.copy(
        port = port,
        localUrl = "http://127.0.0.1:$port",
        lanUrl = if (localIp.isNotEmpty() && localIp != "127.0.0.1") "http://$localIp:$port" else "http://127.0.0.1:$port"
      )
    }
  }

  fun startServer(port: Int = _serverStatus.value.port) {
    if (_serverStatus.value.isRunning) {
      stopServer()
    }

    val server = FullstackServer(
      port = port,
      wwwDir = fileManagerService.webRootDir,
      databaseEngine = databaseEngine,
      onLog = { log ->
        viewModelScope.launch(Dispatchers.Main) {
          _accessLogs.update { current ->
            val updated = current.toMutableList()
            updated.add(0, log)
            if (updated.size > 500) updated.take(500) else updated
          }
        }
      },
      onClientsCountChanged = { count ->
        _serverStatus.update { it.copy(activeClientsCount = count) }
      }
    )

    val started = server.start()
    if (started) {
      activeServer = server
      val localIp = NetworkUtils.getLocalIpAddress(getApplication())
      _serverStatus.update {
        it.copy(
          isRunning = true,
          port = port,
          localUrl = "http://127.0.0.1:$port",
          lanUrl = if (localIp.isNotEmpty() && localIp != "127.0.0.1") "http://$localIp:$port" else "http://127.0.0.1:$port",
          uptimeSeconds = 0L
        )
      }
      _toastMessage.value = "Server aktif pada port $port"
    } else {
      _toastMessage.value = "Gagal memulai server pada port $port"
    }
  }

  fun stopServer() {
    activeServer?.stop()
    activeServer = null
    _serverStatus.update { it.copy(isRunning = false, activeClientsCount = 0) }
    _toastMessage.value = "Server dinonaktifkan"
  }

  fun refreshLanIp() {
    val localIp = NetworkUtils.getLocalIpAddress(getApplication())
    val port = _serverStatus.value.port
    _serverStatus.update {
      it.copy(
        lanUrl = if (localIp.isNotEmpty() && localIp != "127.0.0.1") "http://$localIp:$port" else "http://127.0.0.1:$port"
      )
    }
  }

  fun clearLogs() {
    _accessLogs.value = emptyList()
    _toastMessage.value = "Log dibersihkan"
  }

  fun refreshFiles() {
    _currentFiles.value = fileManagerService.listFiles(_currentDirectory.value)
  }

  fun navigateToDirectory(dir: File) {
    _currentDirectory.value = dir
    refreshFiles()
  }

  fun navigateUp(): Boolean {
    val current = _currentDirectory.value
    if (current == fileManagerService.webRootDir) return false
    val parent = current.parentFile
    if (parent != null && parent.absolutePath.startsWith(fileManagerService.webRootDir.absolutePath)) {
      _currentDirectory.value = parent
      refreshFiles()
      return true
    }
    return false
  }

  fun loadFile(file: File) {
    if (file.isDirectory) return
    val existsInTabs = _openFiles.value.any { it.absolutePath == file.absolutePath }
    if (!existsInTabs) {
      _openFiles.value = _openFiles.value + file
    }
    _selectedFile.value?.let { current ->
      fileBuffers[current.absolutePath] = _fileContent.value
    }
    if (fileBuffers.containsKey(file.absolutePath)) {
      _selectedFile.value = file
      _fileContent.value = fileBuffers[file.absolutePath] ?: ""
    } else {
      viewModelScope.launch(Dispatchers.IO) {
        val content = fileManagerService.readFile(file)
        withContext(Dispatchers.Main) {
          fileBuffers[file.absolutePath] = content
          _selectedFile.value = file
          _fileContent.value = content
        }
      }
    }
  }

  fun selectTab(file: File) {
    if (_selectedFile.value?.absolutePath == file.absolutePath) return
    _selectedFile.value?.let { current ->
      fileBuffers[current.absolutePath] = _fileContent.value
    }
    _selectedFile.value = file
    if (fileBuffers.containsKey(file.absolutePath)) {
      _fileContent.value = fileBuffers[file.absolutePath] ?: ""
    } else {
      viewModelScope.launch(Dispatchers.IO) {
        val content = fileManagerService.readFile(file)
        withContext(Dispatchers.Main) {
          fileBuffers[file.absolutePath] = content
          _fileContent.value = content
        }
      }
    }
  }

  fun closeTab(file: File) {
    _selectedFile.value?.let { current ->
      fileBuffers[current.absolutePath] = _fileContent.value
    }
    val updatedTabs = _openFiles.value.filter { it.absolutePath != file.absolutePath }
    fileBuffers.remove(file.absolutePath)
    _openFiles.value = updatedTabs
    if (_selectedFile.value?.absolutePath == file.absolutePath) {
      if (updatedTabs.isNotEmpty()) {
        val nextFile = updatedTabs.last()
        _selectedFile.value = nextFile
        _fileContent.value = fileBuffers[nextFile.absolutePath] ?: fileManagerService.readFile(nextFile)
      } else {
        _selectedFile.value = null
        _fileContent.value = ""
        refreshFiles()
      }
    }
  }

  fun closeEditor() {
    _selectedFile.value = null
    _openFiles.value = emptyList()
    _fileContent.value = ""
    fileBuffers.clear()
    refreshFiles()
  }

  fun updateFileContent(content: String) {
    _fileContent.value = content
    _selectedFile.value?.let {
      fileBuffers[it.absolutePath] = content
    }
  }

  fun saveCurrentFile() {
    val file = _selectedFile.value ?: return
    _isSaving.value = true
    viewModelScope.launch(Dispatchers.IO) {
      val success = fileManagerService.writeFile(file, _fileContent.value)
      withContext(Dispatchers.Main) {
        _isSaving.value = false
        if (success) {
          fileBuffers[file.absolutePath] = _fileContent.value
          _toastMessage.value = "Berkas tersimpan"
          refreshFiles()
        } else {
          _toastMessage.value = "Gagal menyimpan berkas"
        }
      }
    }
  }

  fun renameCurrentFile(newName: String) {
    val current = _selectedFile.value ?: return
    val clean = newName.trim()
    if (clean.isEmpty() || clean == current.name) return
    val renamed = fileManagerService.renameTarget(current, clean)
    if (renamed != null) {
      val currentContent = _fileContent.value
      fileBuffers[renamed.absolutePath] = currentContent
      fileBuffers.remove(current.absolutePath)
      _openFiles.value = _openFiles.value.map {
        if (it.absolutePath == current.absolutePath) renamed else it
      }
      _selectedFile.value = renamed
      refreshFiles()
      _toastMessage.value = "Nama berkas diubah: $clean"
    } else {
      _toastMessage.value = "Gagal mengubah nama berkas"
    }
  }

  fun duplicateCurrentFile() {
    val current = _selectedFile.value ?: return
    saveCurrentFile()
    val copy = fileManagerService.duplicateTarget(current)
    if (copy != null) {
      refreshFiles()
      loadFile(copy)
      _toastMessage.value = "Salinan ${copy.name} dibuat"
    } else {
      _toastMessage.value = "Gagal menduplikasi berkas"
    }
  }

  fun deleteCurrentFile() {
    val current = _selectedFile.value ?: return
    val success = fileManagerService.deleteTarget(current)
    if (success) {
      closeTab(current)
      refreshFiles()
      _toastMessage.value = "${current.name} dihapus"
    } else {
      _toastMessage.value = "Gagal menghapus berkas"
    }
  }

  fun createNewFile(name: String, content: String = "") {
    val parent = _currentDirectory.value
    val success = fileManagerService.createFile(parent, name, content)
    if (success) {
      refreshFiles()
      val newFile = File(parent, name)
      loadFile(newFile)
      _toastMessage.value = "Berkas $name dibuat"
    } else {
      _toastMessage.value = "Gagal membuat berkas"
    }
  }

  fun createNewFolder(name: String) {
    val parent = _currentDirectory.value
    val success = fileManagerService.createDirectory(parent, name)
    if (success) {
      refreshFiles()
      _toastMessage.value = "Folder $name dibuat"
    } else {
      _toastMessage.value = "Gagal membuat folder"
    }
  }

  fun deleteTarget(target: File) {
    val success = fileManagerService.deleteTarget(target)
    if (success) {
      if (_selectedFile.value?.absolutePath == target.absolutePath) {
        _selectedFile.value = null
        _fileContent.value = ""
      }
      refreshFiles()
      _toastMessage.value = "${target.name} dihapus"
    } else {
      _toastMessage.value = "Gagal menghapus"
    }
  }

  fun importFile(inputStream: InputStream, originalName: String) {
    val dest = File(_currentDirectory.value, originalName)
    viewModelScope.launch(Dispatchers.IO) {
      val success = fileManagerService.importFromStream(inputStream, dest)
      withContext(Dispatchers.Main) {
        if (success) {
          refreshFiles()
          _toastMessage.value = "Berkas $originalName berhasil diimpor"
        } else {
          _toastMessage.value = "Gagal mengimpor berkas"
        }
      }
    }
  }

  fun importZip(inputStream: InputStream) {
    viewModelScope.launch(Dispatchers.IO) {
      val success = fileManagerService.extractZipArchive(inputStream, _currentDirectory.value)
      withContext(Dispatchers.Main) {
        if (success) {
          refreshFiles()
          _toastMessage.value = "Arsip berhasil diekstrak"
        } else {
          _toastMessage.value = "Gagal mengekstrak arsip"
        }
      }
    }
  }

  fun cloneGitRepo(url: String) {
    val targetUrl = url.trim()
    if (targetUrl.isEmpty()) {
      _toastMessage.value = "Masukkan URL repository Git yang valid"
      return
    }

    _isGitLoading.value = true
    _gitRepoUrl.value = targetUrl

    viewModelScope.launch {
      val result = gitService.cloneOrPull(targetUrl, fileManagerService.webRootDir)
      _isGitLoading.value = false
      result.onSuccess { msg ->
        _toastMessage.value = msg
        refreshFiles()
      }.onFailure { err ->
        _toastMessage.value = "Git Clone Gagal: ${err.message}"
      }
    }
  }

  fun pullGitRepo() {
    val url = _gitRepoUrl.value.trim()
    if (url.isEmpty()) {
      _toastMessage.value = "Belum ada URL repository Git"
      return
    }
    cloneGitRepo(url)
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  override fun onCleared() {
    super.onCleared()
    activeServer?.stop()
  }
}
