/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.core

import android.util.Log
import app.opentv.data.model.LiveStreamFormat
import app.opentv.data.model.Source
import app.opentv.data.model.SourceKind
import app.opentv.data.repo.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Handles communication with Supabase cloud backend for device registration,
 * real-time license verification, and remote playlist delivery.
 */
class LicenseCloudClient(
    private val httpClient: OkHttpClient,
    private val sourceRepository: SourceRepository,
) {

    data class CloudSyncResult(
        val success: Boolean,
        val status: String = "",
        val isPremium: Boolean = false,
        val daysRemaining: Int = 0,
        val expiryMillis: Long = 0L,
        val newPlaylistsAdded: Int = 0,
        val errorMessage: String? = null,
    )

    suspend fun syncDevice(
        mac: String,
        key: String,
        appVersion: String = "1.0",
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        try {
            val jsonPayload = JSONObject().apply {
                put("p_mac", mac)
                put("p_key", key)
                put("p_version", appVersion)
            }

            val requestBody = jsonPayload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/rpc/verify_or_register_device")
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer $SUPABASE_KEY")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = response.body?.string() ?: "HTTP ${response.code}"
                    Log.w(TAG, "Cloud license check failed: $err")
                    return@withContext CloudSyncResult(
                        success = false,
                        errorMessage = "Sunucu yanıt vermedi: HTTP ${response.code}",
                    )
                }

                val bodyStr = response.body?.string() ?: "{}"
                val obj = JSONObject(bodyStr)

                val status = obj.optString("status", "trial")
                val isPremium = obj.optBoolean("is_premium", false)
                val daysRemaining = obj.optInt("days_remaining", 7)
                val expiresAtStr = obj.optString("expires_at", "")

                val expiryMillis = if (expiresAtStr.isNotBlank() && expiresAtStr != "null") {
                    runCatching {
                        Instant.parse(expiresAtStr).toEpochMilli()
                    }.getOrDefault(0L)
                } else 0L

                // Auto-import cloud playlists if configured from Web Portal
                var addedCount = 0
                val playlistsArr = obj.optJSONArray("playlists") ?: JSONArray()
                if (playlistsArr.length() > 0) {
                    val existing = sourceRepository.enabled()
                    val existingUrls = existing.map { it.url }.toSet()

                    for (i in 0 until playlistsArr.length()) {
                        val p = playlistsArr.getJSONObject(i)
                        val pUrl = p.optString("url", "").trim()
                        if (pUrl.isNotBlank() && pUrl !in existingUrls) {
                            val pName = p.optString("name", "Bulut Listesi").ifBlank { "Bulut Listesi" }
                            val pKindStr = p.optString("kind", "XTREAM").uppercase()
                            val pKind = when (pKindStr) {
                                "M3U" -> SourceKind.M3U
                                "STALKER" -> SourceKind.STALKER
                                else -> SourceKind.XTREAM
                            }
                            val pUser = p.optString("username", "")
                            val pPass = p.optString("password", "")
                            val pMac = p.optString("mac", "")

                            val newSource = Source(
                                name = pName,
                                kind = pKind,
                                url = pUrl,
                                username = pUser,
                                password = pPass,
                                macAddress = pMac,
                                liveFormat = LiveStreamFormat.AUTO,
                            )
                            sourceRepository.save(newSource)
                            addedCount++
                        }
                    }
                }

                return@withContext CloudSyncResult(
                    success = true,
                    status = status,
                    isPremium = isPremium,
                    daysRemaining = daysRemaining,
                    expiryMillis = expiryMillis,
                    newPlaylistsAdded = addedCount,
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing with Supabase", e)
            return@withContext CloudSyncResult(
                success = false,
                errorMessage = e.localizedMessage ?: "Bağlantı hatası",
            )
        }
    }

    companion object {
        private const val TAG = "LicenseCloudClient"
        const val SUPABASE_URL = "https://yhrftdpzrhtoaqohjmhs.supabase.co"
        const val SUPABASE_KEY = "sb_publishable_KEatc4BhnVMiAv4LXxbnmQ_lKSINSYd"
    }
}
