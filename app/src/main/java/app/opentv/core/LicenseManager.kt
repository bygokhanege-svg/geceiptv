/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.core

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random

/**
 * Manages device identity (MAC-style ID and Security Key) and subscription / trial licensing.
 * Designed for commercial IPTV player architecture (TiviMate / IBO Player model).
 */
class LicenseManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gece_license_prefs", Context.MODE_PRIVATE)

    sealed interface LicenseStatus {
        data class Trial(val daysRemaining: Int) : LicenseStatus
        data class Premium(val isLifetime: Boolean, val expiryMillis: Long) : LicenseStatus
        data object Expired : LicenseStatus
    }

    val rawDeviceId: String
    val formattedMac: String
    val deviceKey: String

    private val _isPremium: MutableStateFlow<Boolean>
    val isPremium: StateFlow<Boolean>

    private val _premiumExpiryMillis: MutableStateFlow<Long>
    val premiumExpiryMillis: StateFlow<Long>

    private val _licenseStatus: MutableStateFlow<LicenseStatus>
    val licenseStatus: StateFlow<LicenseStatus>

    init {
        // 1. Initialize or load persistent Device ID
        var storedId = prefs.getString(KEY_RAW_DEVICE_ID, null)
        if (storedId.isNullOrBlank()) {
            val androidId = runCatching {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }.getOrNull()

            val candidate = if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                androidId
            } else {
                UUID.randomUUID().toString().replace("-", "")
            }

            // Derive a stable 12-character hex ID (standard MAC length)
            val hash = sha256("gece-device-$candidate").take(12)
            storedId = hash.lowercase()
            prefs.edit().putString(KEY_RAW_DEVICE_ID, storedId).apply()
        }
        rawDeviceId = storedId
        formattedMac = storedId.chunked(2).joinToString(":").uppercase()

        // 2. Initialize or load 6-digit Device Security Key
        var storedKey = prefs.getString(KEY_DEVICE_KEY, null)
        if (storedKey.isNullOrBlank() || storedKey.length != 6) {
            val generatedKey = (100_000 + Random.nextInt(900_000)).toString()
            prefs.edit().putString(KEY_DEVICE_KEY, generatedKey).apply()
            storedKey = generatedKey
        }
        deviceKey = storedKey

        // 3. Initialize First Launch Time (for 7-day trial)
        val firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        if (firstLaunch == 0L) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis()).apply()
        }

        // 4. Load Premium Status
        val isPrem = prefs.getBoolean(KEY_IS_PREMIUM, false)
        val expMillis = prefs.getLong(KEY_PREMIUM_EXPIRY, 0L)

        _isPremium = MutableStateFlow(isPrem)
        isPremium = _isPremium.asStateFlow()

        _premiumExpiryMillis = MutableStateFlow(expMillis)
        premiumExpiryMillis = _premiumExpiryMillis.asStateFlow()

        _licenseStatus = MutableStateFlow(computeStatus(isPrem, expMillis, getFirstLaunch()))
        licenseStatus = _licenseStatus.asStateFlow()
    }

    private fun getFirstLaunch(): Long {
        return prefs.getLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
    }

    /** Recomputes license status based on current time and trial expiration. */
    fun refreshStatus() {
        val status = computeStatus(_isPremium.value, _premiumExpiryMillis.value, getFirstLaunch())
        _licenseStatus.value = status
    }

    private fun computeStatus(isPrem: Boolean, expMillis: Long, firstLaunch: Long): LicenseStatus {
        val now = System.currentTimeMillis()
        if (isPrem) {
            return if (expMillis == 0L || expMillis > now) {
                LicenseStatus.Premium(isLifetime = expMillis == 0L, expiryMillis = expMillis)
            } else {
                LicenseStatus.Expired
            }
        }

        val trialDurationMs = TRIAL_DAYS * 24 * 60 * 60 * 1000L
        val elapsed = now - firstLaunch
        val remainingMs = trialDurationMs - elapsed

        return if (remainingMs > 0) {
            val daysRemaining = ((remainingMs + 86_399_999L) / (24 * 60 * 60 * 1000L)).toInt().coerceIn(1, TRIAL_DAYS)
            LicenseStatus.Trial(daysRemaining)
        } else {
            LicenseStatus.Expired
        }
    }

    /**
     * Synchronizes device license status and cloud playlists with Supabase.
     */
    suspend fun syncWithCloud(cloudClient: LicenseCloudClient): LicenseCloudClient.CloudSyncResult {
        val res = cloudClient.syncDevice(formattedMac, deviceKey)
        if (res.success) {
            if (res.isPremium) {
                setPremium(isLifetime = res.expiryMillis == 0L, expiryMillis = res.expiryMillis)
            } else if (res.status == "expired") {
                prefs.edit().putBoolean(KEY_IS_PREMIUM, false).apply()
                _isPremium.value = false
                _licenseStatus.value = LicenseStatus.Expired
            } else {
                prefs.edit().putBoolean(KEY_IS_PREMIUM, false).apply()
                _isPremium.value = false
                _licenseStatus.value = LicenseStatus.Trial(res.daysRemaining)
            }
        }
        return res
    }

    /**
     * Activates device using an offline license code or master key.
     * Generates expected code based on Device ID and Device Key hash.
     */
    fun activateWithCode(inputCode: String): Boolean {
        val cleaned = inputCode.trim().replace("-", "").uppercase()
        if (cleaned.isBlank()) return false

        // Master keys for testing/development
        val masterKeys = setOf("GECE2026", "GECEVIP", "PREMIUM2026", "OPENTVPRO")
        if (cleaned in masterKeys) {
            setPremium(isLifetime = true, expiryMillis = 0L)
            return true
        }

        // Device-specific algorithmic license code check
        val expected = generateDeviceLicenseCode(formattedMac, deviceKey).replace("-", "").uppercase()
        if (cleaned == expected) {
            setPremium(isLifetime = true, expiryMillis = 0L)
            return true
        }

        return false
    }

    /** Sets the premium status and persists to disk. */
    fun setPremium(isLifetime: Boolean, expiryMillis: Long = 0L) {
        val targetExp = if (isLifetime) 0L else expiryMillis
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, true)
            .putLong(KEY_PREMIUM_EXPIRY, targetExp)
            .apply()

        _isPremium.value = true
        _premiumExpiryMillis.value = targetExp
        refreshStatus()
    }

    /** Reset trial for testing purposes. */
    fun resetTrial() {
        prefs.edit()
            .putBoolean(KEY_IS_PREMIUM, false)
            .putLong(KEY_PREMIUM_EXPIRY, 0L)
            .putLong(KEY_FIRST_LAUNCH, System.currentTimeMillis())
            .apply()

        _isPremium.value = false
        _premiumExpiryMillis.value = 0L
        refreshStatus()
    }

    /** Web activation portal URL with device parameters. */
    val webActivationUrl: String
        get() = "https://gecetv.app/activate?mac=$formattedMac&key=$deviceKey"

    companion object {
        const val TRIAL_DAYS = 7

        private const val KEY_RAW_DEVICE_ID = "raw_device_id"
        private const val KEY_DEVICE_KEY = "device_key"
        private const val KEY_FIRST_LAUNCH = "first_launch_time"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_PREMIUM_EXPIRY = "premium_expiry_millis"

        @Volatile
        private var instance: LicenseManager? = null

        fun get(context: Context): LicenseManager =
            instance ?: synchronized(this) {
                instance ?: LicenseManager(context.applicationContext).also { instance = it }
            }

        /**
         * Computes the algorithmic license code for a given MAC and Key.
         * Format: `XXXX-XXXX` (8 characters hex)
         */
        fun generateDeviceLicenseCode(mac: String, key: String): String {
            val input = "GECE-TV-LIC-${mac.replace(":", "").uppercase()}-$key"
            val hash = sha256(input)
            val p1 = hash.substring(0, 4).uppercase()
            val p2 = hash.substring(4, 8).uppercase()
            return "$p1-$p2"
        }

        private fun sha256(input: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
