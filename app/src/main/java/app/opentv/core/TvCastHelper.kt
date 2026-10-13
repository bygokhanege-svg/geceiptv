/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.core

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Helper to cast channels, movies, and streams to paired TV and sync local providers.
 */
object TvCastHelper {
    const val PREFS_NAME = "opentv_remote"
    const val KEY_TV_URL = "last_tv_url"
    const val KEY_TV_TOKEN = "last_tv_token"

    fun getTvUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TV_URL, "").orEmpty()
    }

    fun isTvPaired(context: Context): Boolean = getTvUrl(context).isNotBlank()

    fun castChannel(context: Context, scope: CoroutineScope, channelId: Long, channelName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tvUrl = prefs.getString(KEY_TV_URL, "").orEmpty()
        val tvToken = prefs.getString(KEY_TV_TOKEN, "").orEmpty()
        if (tvUrl.isBlank()) {
            Toast.makeText(context, "Önce Kumanda menüsünden TV'yi bağlayın.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (tvUrl.endsWith("/")) "${tvUrl}play?t=$tvToken" else "$tvUrl/play?t=$tvToken"
                val body = """{"channelId":$channelId}"""
                val req = Request.Builder()
                    .url(endpoint)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                val httpClient = ServiceLocator.get(context).httpClient
                val res = httpClient.newCall(req).execute()
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(context, "✓ '$channelName' TV'ye gönderildi!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "TV'ye gönderilemedi: HTTP ${res.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "TV Gönderme Hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun castMediaUrl(context: Context, scope: CoroutineScope, url: String, title: String, isLive: Boolean = false) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tvUrl = prefs.getString(KEY_TV_URL, "").orEmpty()
        val tvToken = prefs.getString(KEY_TV_TOKEN, "").orEmpty()
        if (tvUrl.isBlank()) {
            Toast.makeText(context, "Önce Kumanda menüsünden TV'yi bağlayın.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (tvUrl.endsWith("/")) "${tvUrl}play?t=$tvToken" else "$tvUrl/play?t=$tvToken"
                val body = buildString {
                    append("{\"url\":\"").append(url.replace("\"", "\\\"")).append("\"")
                    append(",\"title\":\"").append(title.replace("\"", "\\\"")).append("\"")
                    append(",\"isLive\":").append(isLive)
                    append("}")
                }
                val req = Request.Builder()
                    .url(endpoint)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()
                val httpClient = ServiceLocator.get(context).httpClient
                val res = httpClient.newCall(req).execute()
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful) {
                        Toast.makeText(context, "✓ '$title' TV'ye gönderildi!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "TV'ye gönderilemedi: HTTP ${res.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "TV Gönderme Hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun syncSourcesToTv(
        context: Context,
        scope: CoroutineScope,
        tvUrl: String,
        tvToken: String,
        onComplete: ((Boolean, Int) -> Unit)? = null
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val sourceRepo = ServiceLocator.get(context).sourceRepository
                val localSources = sourceRepo.enabled()
                if (localSources.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Telefonda kayıtlı sağlayıcı bulunamadı.", Toast.LENGTH_SHORT).show()
                        onComplete?.invoke(true, 0)
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🔄 ${localSources.size} sağlayıcı TV'ye aktarılıyor...", Toast.LENGTH_SHORT).show()
                }
                val endpoint = if (tvUrl.endsWith("/")) "${tvUrl}source?t=$tvToken" else "$tvUrl/source?t=$tvToken"
                val httpClient = ServiceLocator.get(context).httpClient
                var successCount = 0
                for (source in localSources) {
                    val jsonBody = buildString {
                        append("{\"kind\":\"").append(source.kind.name.lowercase()).append("\"")
                        append(",\"name\":\"").append(source.name.replace("\"", "\\\"")).append("\"")
                        append(",\"url\":\"").append(source.url.replace("\"", "\\\"")).append("\"")
                        source.username?.let { append(",\"username\":\"").append(it.replace("\"", "\\\"")).append("\"") }
                        source.password?.let { append(",\"password\":\"").append(it.replace("\"", "\\\"")).append("\"") }
                        source.macAddress?.let { append(",\"mac\":\"").append(it.replace("\"", "\\\"")).append("\"") }
                        append("}")
                    }
                    val req = Request.Builder()
                        .url(endpoint)
                        .post(jsonBody.toRequestBody("application/json".toMediaType()))
                        .build()
                    runCatching {
                        httpClient.newCall(req).execute().use { res ->
                            if (res.isSuccessful) successCount++
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✓ $successCount sağlayıcı TV'ye başarıyla aktarıldı!", Toast.LENGTH_LONG).show()
                    onComplete?.invoke(true, successCount)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Sağlayıcı aktarımı hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke(false, 0)
                }
            }
        }
    }
}
