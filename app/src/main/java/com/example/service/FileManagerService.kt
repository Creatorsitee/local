package com.example.service

import android.content.Context
import com.example.model.WebFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class FileManagerService(private val context: Context) {

  val webRootDir: File by lazy {
    val dir = File(context.filesDir, "www")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    val indexFile = File(dir, "index.html")
    if (!indexFile.exists() || indexFile.length() == 0L) {
      indexFile.writeText(DefaultSite.HTML)
    }
    dir
  }

  val dataDir: File by lazy {
    val dir = File(context.filesDir, "data")
    if (!dir.exists()) {
      dir.mkdirs()
    }
    dir
  }

  fun ensureDefaultSite() {
    val indexFile = File(webRootDir, "index.html")
    if (!indexFile.exists() || indexFile.length() == 0L) {
      indexFile.writeText(DefaultSite.HTML)
    }
  }

  fun resetDefaultSite(): Boolean {
    return try {
      val indexFile = File(webRootDir, "index.html")
      indexFile.writeText(DefaultSite.HTML)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun listFiles(dir: File = webRootDir): List<WebFile> {
    val items = dir.listFiles() ?: return emptyList()
    return items.map {
      WebFile(
        file = it,
        name = it.name,
        path = it.relativeTo(webRootDir).path,
        isDirectory = it.isDirectory,
        size = if (it.isDirectory) 0L else it.length(),
        lastModified = it.lastModified()
      )
    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
  }

  fun createFile(parent: File, name: String, initialContent: String = ""): Boolean {
    return try {
      val file = File(parent, name)
      if (file.exists()) return false
      file.writeText(initialContent)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun createDirectory(parent: File, name: String): Boolean {
    return try {
      val dir = File(parent, name)
      if (dir.exists()) return false
      dir.mkdirs()
    } catch (_: Exception) {
      false
    }
  }

  fun deleteTarget(target: File): Boolean {
    return try {
      target.deleteRecursively()
    } catch (_: Exception) {
      false
    }
  }

  fun readFile(file: File): String {
    return try {
      file.readText()
    } catch (_: Exception) {
      ""
    }
  }

  fun writeFile(file: File, content: String): Boolean {
    return try {
      file.writeText(content)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun renameTarget(target: File, newName: String): File? {
    return try {
      val parent = target.parentFile ?: return null
      val dest = File(parent, newName)
      if (dest.exists()) return null
      if (target.renameTo(dest)) dest else null
    } catch (_: Exception) {
      null
    }
  }

  fun duplicateTarget(target: File): File? {
    return try {
      val parent = target.parentFile ?: return null
      val baseName = target.nameWithoutExtension
      val ext = if (target.extension.isNotEmpty()) ".${target.extension}" else ""
      var newName = "${baseName}_salinan$ext"
      var counter = 1
      var dest = File(parent, newName)
      while (dest.exists()) {
        counter++
        newName = "${baseName}_salinan$counter$ext"
        dest = File(parent, newName)
      }
      if (target.isDirectory) {
        target.copyRecursively(dest, overwrite = false)
      } else {
        target.copyTo(dest, overwrite = false)
      }
      dest
    } catch (_: Exception) {
      null
    }
  }

  fun importFromStream(inputStream: InputStream, destination: File): Boolean {
    return try {
      val out = FileOutputStream(destination)
      val buffer = ByteArray(8192)
      var bytesRead: Int
      while (inputStream.read(buffer).also { bytesRead = it } != -1) {
        out.write(buffer, 0, bytesRead)
      }
      out.flush()
      out.close()
      inputStream.close()
      true
    } catch (_: Exception) {
      false
    }
  }

  fun extractZipArchive(inputStream: InputStream, targetDir: File): Boolean {
    return try {
      targetDir.listFiles()?.forEach { it.deleteRecursively() }

      val zis = ZipInputStream(inputStream)
      val entries = mutableListOf<Pair<java.util.zip.ZipEntry, ByteArray>>()
      
      var entry = zis.nextEntry
      val buffer = ByteArray(8192)

      while (entry != null) {
        if (!entry.isDirectory) {
          val baos = ByteArrayOutputStream()
          var count: Int
          while (zis.read(buffer).also { count = it } != -1) {
            baos.write(buffer, 0, count)
          }
          entries.add(Pair(entry, baos.toByteArray()))
        } else {
          entries.add(Pair(entry, byteArrayOf()))
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
      zis.close()
      inputStream.close()

      val fileEntries = entries.filter { !it.first.isDirectory }
      val firstPath = fileEntries.firstOrNull()?.first?.name ?: ""
      val commonPrefix = if (firstPath.contains("/")) {
        val potentialPrefix = firstPath.substring(0, firstPath.indexOf("/") + 1)
        if (fileEntries.all { it.first.name.startsWith(potentialPrefix) }) potentialPrefix else ""
      } else ""

      for ((zipEntry, data) in entries) {
        var entryName = zipEntry.name
        if (commonPrefix.isNotEmpty() && entryName.startsWith(commonPrefix)) {
          entryName = entryName.removePrefix(commonPrefix)
        }
        if (entryName.isNotEmpty()) {
          val destFile = File(targetDir, entryName)
          if (zipEntry.isDirectory) {
            destFile.mkdirs()
          } else {
            destFile.parentFile?.mkdirs()
            destFile.writeBytes(data)
          }
        }
      }
      true
    } catch (_: Exception) {
      false
    }
  }
}
