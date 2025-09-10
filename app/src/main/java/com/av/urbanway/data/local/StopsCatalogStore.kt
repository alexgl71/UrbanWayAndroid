package com.av.urbanway.data.local

import android.content.Context
import com.av.urbanway.data.models.StopInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class StopsCatalogStore private constructor(private val appContext: Context) {
    private val gson = Gson()
    private val fileName = "stops_catalog.json"

    private fun file(): File = File(appContext.filesDir, fileName)

    fun exists(): Boolean = file().exists()

    fun save(items: List<StopInfo>) {
        val json = gson.toJson(items)
        file().writeText(json)
    }

    fun load(): List<StopInfo> {
        val f = file()
        if (!f.exists()) return emptyList()
        val json = f.readText()
        val type = object : TypeToken<List<StopInfo>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    companion object {
        fun getInstance(context: Context): StopsCatalogStore {
            val appCtx = context.applicationContext
            return StopsCatalogStore(appCtx)
        }
    }
}

