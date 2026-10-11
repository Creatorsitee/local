package com.example.server

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class DatabaseEngine(private val dataDir: File) {

  init {
    if (!dataDir.exists()) {
      dataDir.mkdirs()
    }
  }

  @Synchronized
  fun getCollection(name: String): String {
    val file = File(dataDir, "$name.json")
    if (!file.exists()) {
      return "[]"
    }
    return try {
      file.readText()
    } catch (_: Exception) {
      "[]"
    }
  }

  @Synchronized
  fun getItem(name: String, id: String): String? {
    val content = getCollection(name)
    val array = JSONArray(content)
    for (i in 0 until array.length()) {
      val obj = array.getJSONObject(i)
      if (obj.optString("id") == id) {
        return obj.toString()
      }
    }
    return null
  }

  @Synchronized
  fun insertItem(name: String, jsonString: String): String {
    val file = File(dataDir, "$name.json")
    val array = try {
      if (file.exists()) JSONArray(file.readText()) else JSONArray()
    } catch (_: Exception) {
      JSONArray()
    }

    val newObj = try {
      JSONObject(jsonString)
    } catch (_: Exception) {
      JSONObject().apply { put("value", jsonString) }
    }

    if (!newObj.has("id")) {
      newObj.put("id", UUID.randomUUID().toString().take(8))
    }
    newObj.put("createdAt", System.currentTimeMillis())

    array.put(newObj)
    file.writeText(array.toString(2))
    return newObj.toString()
  }

  @Synchronized
  fun updateItem(name: String, id: String, jsonString: String): String? {
    val file = File(dataDir, "$name.json")
    if (!file.exists()) return null

    val array = try {
      JSONArray(file.readText())
    } catch (_: Exception) {
      return null
    }

    var updatedObj: JSONObject? = null
    val newArray = JSONArray()

    for (i in 0 until array.length()) {
      val item = array.getJSONObject(i)
      if (item.optString("id") == id) {
        val updatePayload = try {
          JSONObject(jsonString)
        } catch (_: Exception) {
          JSONObject()
        }
        val keys = updatePayload.keys()
        while (keys.hasNext()) {
          val key = keys.next()
          item.put(key, updatePayload.get(key))
        }
        item.put("updatedAt", System.currentTimeMillis())
        updatedObj = item
      }
      newArray.put(item)
    }

    if (updatedObj != null) {
      file.writeText(newArray.toString(2))
      return updatedObj.toString()
    }
    return null
  }

  @Synchronized
  fun deleteItem(name: String, id: String): Boolean {
    val file = File(dataDir, "$name.json")
    if (!file.exists()) return false

    val array = try {
      JSONArray(file.readText())
    } catch (_: Exception) {
      return false
    }

    var found = false
    val newArray = JSONArray()

    for (i in 0 until array.length()) {
      val item = array.getJSONObject(i)
      if (item.optString("id") == id) {
        found = true
      } else {
        newArray.put(item)
      }
    }

    if (found) {
      file.writeText(newArray.toString(2))
      return true
    }
    return false
  }

  @Synchronized
  fun clearCollection(name: String): Boolean {
    val file = File(dataDir, "$name.json")
    return if (file.exists()) {
      file.writeText("[]")
      true
    } else {
      false
    }
  }

  @Synchronized
  fun listCollections(): List<String> {
    return dataDir.listFiles { file -> file.extension == "json" }
      ?.map { it.nameWithoutExtension }
      ?: emptyList()
  }
}
