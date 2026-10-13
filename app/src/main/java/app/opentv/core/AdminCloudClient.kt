/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Administrative Cloud Client for managing device licenses and tracking users directly from Supabase.
 * Uses administrative credentials with server User-Agent to bypass public RLS restrictions.
 */
class AdminCloudClient(
    private val httpClient: OkHttpClient,
) {

    data class DeviceAdminRecord(
        val mac: String,
        val deviceKey: String,
        val label: String,
        val appVersion: String,
        val createdAt: String,
        val lastSeenAt: String,
        val licenseStatus: String, // trial, active, expired
        val planType: String, // trial_7d, 1_year, lifetime, expired
        val expiresAt: String?,
        val notes: String?,
    )

    suspend fun fetchAllDevices(): List<DeviceAdminRecord> = withContext(Dispatchers.IO) {
        try {
            // 1. Fetch devices
            val devReq = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/devices?select=*&order=created_at.desc")
                .header("apikey", SUPABASE_SECRET)
                .header("Authorization", "Bearer $SUPABASE_SECRET")
                .header("User-Agent", "GeceTvAdmin/1.0")
                .get()
                .build()

            val devicesJson = httpClient.newCall(devReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                resp.body?.string().orEmpty()
            }
            val devArr = JSONArray(devicesJson)

            // 2. Fetch licenses
            val licReq = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/licenses?select=*")
                .header("apikey", SUPABASE_SECRET)
                .header("Authorization", "Bearer $SUPABASE_SECRET")
                .header("User-Agent", "GeceTvAdmin/1.0")
                .get()
                .build()

            val licensesJson = httpClient.newCall(licReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                resp.body?.string().orEmpty()
            }
            val licArr = JSONArray(licensesJson)

            val licenseMap = mutableMapOf<String, JSONObject>()
            for (i in 0 until licArr.length()) {
                val l = licArr.getJSONObject(i)
                val m = l.optString("device_mac", "").uppercase()
                if (m.isNotBlank()) licenseMap[m] = l
            }

            val list = mutableListOf<DeviceAdminRecord>()
            for (i in 0 until devArr.length()) {
                val d = devArr.getJSONObject(i)
                val mac = d.optString("mac_address", "").uppercase()
                val lic = licenseMap[mac]

                val status = lic?.optString("status", "trial") ?: "trial"
                val plan = lic?.optString("plan_type", "trial_7d") ?: "trial_7d"
                val exp = if (lic != null && !lic.isNull("expires_at")) lic.optString("expires_at") else null
                val notes = if (lic != null && !lic.isNull("notes")) lic.optString("notes") else null

                list.add(
                    DeviceAdminRecord(
                        mac = mac,
                        deviceKey = d.optString("device_key", "------"),
                        label = d.optString("label", "Cihaz"),
                        appVersion = d.optString("app_version", "1.0"),
                        createdAt = d.optString("created_at", ""),
                        lastSeenAt = d.optString("last_seen_at", ""),
                        licenseStatus = status,
                        planType = plan,
                        expiresAt = exp,
                        notes = notes,
                    ),
                )
            }
            return@withContext list
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices list", e)
            return@withContext emptyList()
        }
    }

    suspend fun updateDevicePlan(
        mac: String,
        status: String,
        planType: String,
        expiresAtIso: String? = null,
        notes: String? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
                put("plan_type", planType)
                if (expiresAtIso != null) {
                    put("expires_at", expiresAtIso)
                } else if (planType == "lifetime") {
                    put("expires_at", JSONObject.NULL)
                }
                if (notes != null) put("notes", notes)
                put("updated_at", Instant.now().toString())
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val patchReq = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/licenses?device_mac=eq.$mac")
                .header("apikey", SUPABASE_SECRET)
                .header("Authorization", "Bearer $SUPABASE_SECRET")
                .header("User-Agent", "GeceTvAdmin/1.0")
                .header("Prefer", "return=representation")
                .patch(body)
                .build()

            httpClient.newCall(patchReq).execute().use { resp ->
                return@withContext resp.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating device plan", e)
            return@withContext false
        }
    }

    suspend fun setLifetimeVip(mac: String): Boolean =
        updateDevicePlan(mac, status = "active", planType = "lifetime", expiresAtIso = null, notes = "Yönetici: Ömür Boyu VIP")

    suspend fun setOneYearVip(mac: String): Boolean {
        val nextYear = Instant.now().plus(365, ChronoUnit.DAYS).toString()
        return updateDevicePlan(mac, status = "active", planType = "1_year", expiresAtIso = nextYear, notes = "Yönetici: 1 Yıl VIP")
    }

    suspend fun resetTrial(mac: String): Boolean {
        val in7Days = Instant.now().plus(7, ChronoUnit.DAYS).toString()
        return updateDevicePlan(mac, status = "trial", planType = "trial_7d", expiresAtIso = in7Days, notes = "Yönetici: 7 Gün Deneme Sıfırlandı")
    }

    suspend fun expireDevice(mac: String): Boolean =
        updateDevicePlan(mac, status = "expired", planType = "expired", notes = "Yönetici: Lisans İptal Edildi")

    companion object {
        private const val TAG = "AdminCloudClient"
        private const val SUPABASE_URL = "https://yhrftdpzrhtoaqohjmhs.supabase.co"
        private val SUPABASE_SECRET = String(android.util.Base64.decode("c2Jfc2VjcmV0X3QtQ1l6bzVsai1jY2xscC1mRDlUTmdfX05NOUt1dzU=", android.util.Base64.DEFAULT)).trim()
    }
}
