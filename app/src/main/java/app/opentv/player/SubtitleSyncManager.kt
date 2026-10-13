/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.player

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/**
 * Manages subtitle synchronization, timestamp shifting (+/- offsets, FPS PAL/NTSC compensation),
 * and local caching of active subtitle files.
 */
class SubtitleSyncManager(
    private val httpClient: OkHttpClient,
) {
    private val _currentOffsetMs = MutableStateFlow(0L)
    val currentOffsetMs: StateFlow<Long> = _currentOffsetMs.asStateFlow()

    private val _activeSubtitle = MutableStateFlow<OnlineSubtitle?>(null)
    val activeSubtitle: StateFlow<OnlineSubtitle?> = _activeSubtitle.asStateFlow()

    private var rawSubtitleContent: String? = null
    private var currentSpeedRatio: Float = 1.0f

    /**
     * Downloads an online subtitle file, caches it, and prepares it for instant playback and sync adjustment.
     */
    suspend fun downloadAndPrepare(context: Context, subtitle: OnlineSubtitle): File? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(subtitle.url)
            .header("User-Agent", "Mozilla/5.0")
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bytes = response.body?.bytes() ?: return@withContext null
                val body = decodeSubtitleBytes(bytes)
                if (body.isBlank()) return@withContext null

                rawSubtitleContent = body
                _activeSubtitle.value = subtitle
                _currentOffsetMs.value = 0L
                currentSpeedRatio = 1.0f

                val outFile = File(context.cacheDir, "active_synced_subtitle.srt")
                outFile.writeText(body, Charsets.UTF_8)
                outFile
            }
        }.onFailure { Log.w(TAG, "Failed to download subtitle file", it) }
            .getOrNull()
    }

    private fun decodeSubtitleBytes(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        // Check for GZIP header (0x1f, 0x8b)
        if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
            return runCatching {
                java.util.zip.GZIPInputStream(bytes.inputStream()).bufferedReader(Charsets.UTF_8).readText()
            }.getOrDefault("")
        }
        // Check for ZIP header ('PK')
        if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte()) {
            return runCatching {
                val zis = java.util.zip.ZipInputStream(bytes.inputStream())
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".srt", ignoreCase = true) || entry.name.endsWith(".vtt", ignoreCase = true)) {
                        return@runCatching zis.bufferedReader(Charsets.UTF_8).readText()
                    }
                    entry = zis.nextEntry
                }
                ""
            }.getOrDefault("")
        }
        // Try UTF-8 first
        val utf8Text = String(bytes, Charsets.UTF_8)
        // If UTF-8 contains replacement character \uFFFD (bad encoding), try windows-1254 (Turkish standard for legacy SRT)
        if (utf8Text.contains("\uFFFD")) {
            return runCatching {
                String(bytes, java.nio.charset.Charset.forName("windows-1254"))
            }.getOrDefault(utf8Text)
        }
        return utf8Text
    }

    /**
     * Adjusts the subtitle timing by [deltaMs] milliseconds and writes the updated subtitle to cache.
     */
    suspend fun adjustOffset(context: Context, deltaMs: Long): File? = withContext(Dispatchers.IO) {
        val raw = rawSubtitleContent ?: return@withContext null
        val newOffset = _currentOffsetMs.value + deltaMs
        _currentOffsetMs.value = newOffset
        writeShiftedContent(context, raw, newOffset, currentSpeedRatio)
    }

    /**
     * Resets offset to 0.0s.
     */
    suspend fun resetOffset(context: Context): File? = withContext(Dispatchers.IO) {
        val raw = rawSubtitleContent ?: return@withContext null
        _currentOffsetMs.value = 0L
        currentSpeedRatio = 1.0f
        writeShiftedContent(context, raw, 0L, 1.0f)
    }

    /**
     * Applies framerate speed compensation (e.g. 23.976 fps to 25 fps PAL conversion).
     */
    suspend fun applyFpsCorrection(context: Context, from23to25: Boolean): File? = withContext(Dispatchers.IO) {
        val raw = rawSubtitleContent ?: return@withContext null
        currentSpeedRatio = if (from23to25) (23.976f / 25.0f) else (25.0f / 23.976f)
        writeShiftedContent(context, raw, _currentOffsetMs.value, currentSpeedRatio)
    }

    fun clear() {
        rawSubtitleContent = null
        _activeSubtitle.value = null
        _currentOffsetMs.value = 0L
        currentSpeedRatio = 1.0f
    }

    private fun writeShiftedContent(
        context: Context,
        rawContent: String,
        offsetMs: Long,
        speedRatio: Float,
    ): File? {
        return runCatching {
            val shifted = shiftSrtTimestamps(rawContent, offsetMs, speedRatio)
            val outFile = File(context.cacheDir, "active_synced_subtitle.srt")
            outFile.writeText(shifted, Charsets.UTF_8)
            outFile
        }.onFailure { Log.w(TAG, "Failed to rewrite shifted subtitle", it) }
            .getOrNull()
    }

    private fun shiftSrtTimestamps(
        content: String,
        offsetMs: Long,
        speedRatio: Float,
    ): String {
        return TIMESTAMP_REGEX.replace(content) { match ->
            val startMs = parseMillis(
                match.groupValues[1],
                match.groupValues[2],
                match.groupValues[3],
                match.groupValues[4],
            )
            val endMs = parseMillis(
                match.groupValues[5],
                match.groupValues[6],
                match.groupValues[7],
                match.groupValues[8],
            )

            val newStart = ((startMs.toDouble() * speedRatio.toDouble()) + offsetMs.toDouble()).toLong().coerceAtLeast(0L)
            val newEnd = ((endMs.toDouble() * speedRatio.toDouble()) + offsetMs.toDouble()).toLong().coerceAtLeast(0L)

            "${formatMillis(newStart)} --> ${formatMillis(newEnd)}"
        }
    }

    private fun parseMillis(h: String, m: String, s: String, ms: String): Long {
        return h.toLong() * 3600000L + m.toLong() * 60000L + s.toLong() * 1000L + ms.toLong()
    }

    private fun formatMillis(millis: Long): String {
        val totalSec = millis / 1000L
        val ms = millis % 1000L
        val s = totalSec % 60L
        val m = (totalSec / 60L) % 60L
        val h = totalSec / 3600L
        return "%02d:%02d:%02d,%03d".format(h, m, s, ms)
    }

    companion object {
        private const val TAG = "SubtitleSyncManager"
        private val TIMESTAMP_REGEX = Regex("(\\d{2}):(\\d{2}):(\\d{2})[,.](\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2})[,.](\\d{3})")
    }
}
