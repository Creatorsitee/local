package com.example.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class GitService(private val fileManagerService: FileManagerService) {

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  suspend fun cloneOrPull(repoUrl: String, targetDir: File): Result<String> = withContext(Dispatchers.IO) {
    try {
      val trimmedUrl = repoUrl.trim().removeSuffix(".git").removeSuffix("/")
      val downloadUrls = buildArchiveUrls(trimmedUrl)

      var extracted = false
      var lastError = "Gagal mengunduh repository dari URL yang diberikan."

      for (archiveUrl in downloadUrls) {
        val request = Request.Builder().url(archiveUrl).build()
        try {
          val response = httpClient.newCall(request).execute()
          if (response.isSuccessful) {
            val body = response.body
            if (body != null) {
              val success = fileManagerService.extractZipArchive(body.byteStream(), targetDir)
              if (success) {
                extracted = true
                break
              }
            }
          } else {
            lastError = "HTTP ${response.code}: ${response.message}"
          }
        } catch (e: Exception) {
          lastError = e.message ?: "Koneksi terputus"
        }
      }

      if (extracted) {
        Result.success("Repository berhasil disinkronisasi.")
      } else {
        Result.failure(Exception(lastError))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun buildArchiveUrls(repoUrl: String): List<String> {
    val urls = mutableListOf<String>()

    if (repoUrl.contains("github.com")) {
      urls.add("$repoUrl/archive/refs/heads/main.zip")
      urls.add("$repoUrl/archive/refs/heads/master.zip")
    } else if (repoUrl.contains("gitlab.com")) {
      val projectPath = repoUrl.removePrefix("https://gitlab.com/").removePrefix("http://gitlab.com/")
      val projectName = projectPath.substringAfterLast("/")
      urls.add("$repoUrl/-/archive/main/$projectName-main.zip")
      urls.add("$repoUrl/-/archive/master/$projectName-master.zip")
    } else {
      if (repoUrl.endsWith(".zip")) {
        urls.add(repoUrl)
      } else {
        urls.add("$repoUrl/archive/refs/heads/main.zip")
        urls.add("$repoUrl/archive/refs/heads/master.zip")
      }
    }

    return urls
  }
}
