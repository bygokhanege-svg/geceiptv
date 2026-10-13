/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.ui.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import app.opentv.core.ServiceLocator
import app.opentv.pairing.QrCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val PREFS_NAME = "opentv_remote"
private const val KEY_TV_URL = "last_tv_url"
private const val KEY_TV_TOKEN = "last_tv_token"

/**
 * Mobile and Tablet Remote Control Screen.
 * Allows phones and tablets to connect to the TV via QR code scanning or direct IP,
 * turning the device into an interactive smart TV remote control.
 */
@Composable
fun RemoteScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val httpClient = remember { ServiceLocator.get(context).httpClient }

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var tvUrl by remember { mutableStateOf(prefs.getString(KEY_TV_URL, "").orEmpty()) }
    var tvToken by remember { mutableStateOf(prefs.getString(KEY_TV_TOKEN, "").orEmpty()) }

    var isConnected by remember { mutableStateOf(tvUrl.isNotBlank()) }
    var showQrScanner by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showNumpad by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }
    var isSendingCmd by remember { mutableStateOf(false) }
    var lastCmdStatus by remember { mutableStateOf<String?>(null) }

    fun vibrateDevice() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    fun parseAndConnect(rawQr: String) {
        val trimmed = rawQr.trim()
        val uri = runCatching { Uri.parse(trimmed) }.getOrNull()

        var hostBase = ""
        var token = ""

        if (uri != null && uri.scheme != null && uri.host != null) {
            val portPart = if (uri.port != -1) ":${uri.port}" else ""
            hostBase = "${uri.scheme}://${uri.host}$portPart"
            token = uri.getQueryParameter("t").orEmpty()
        } else {
            // Raw ip:port or ip:port?t=token
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                val qIdx = trimmed.indexOf("?t=")
                if (qIdx != -1) {
                    hostBase = trimmed.substring(0, qIdx).trimEnd('/')
                    token = trimmed.substring(qIdx + 3)
                } else {
                    hostBase = trimmed.trimEnd('/')
                }
            } else {
                val qIdx = trimmed.indexOf("?t=")
                if (qIdx != -1) {
                    hostBase = "http://" + trimmed.substring(0, qIdx).trimEnd('/')
                    token = trimmed.substring(qIdx + 3)
                } else {
                    hostBase = "http://" + trimmed.trimEnd('/')
                }
            }
        }

        if (hostBase.isNotBlank()) {
            tvUrl = hostBase
            tvToken = token
            prefs.edit().putString(KEY_TV_URL, hostBase).putString(KEY_TV_TOKEN, token).apply()
            isConnected = true
            showQrScanner = false
            showManualDialog = false
            vibrateDevice()
            Toast.makeText(context, "✓ Televizyona Bağlandı!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Geçersiz QR kodu veya IP formatı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendCommand(cmd: String, value: String? = null) {
        if (tvUrl.isBlank()) return
        vibrateDevice()
        isSendingCmd = true

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val endpoint = if (tvUrl.endsWith("/")) "${tvUrl}remote?t=$tvToken" else "$tvUrl/remote?t=$tvToken"
                val body = buildString {
                    append("{\"cmd\":\"").append(cmd).append("\"")
                    if (value != null) {
                        append(",\"value\":\"").append(value.replace("\"", "\\\"")).append("\"")
                    }
                    append("}")
                }
                val req = Request.Builder()
                    .url(endpoint)
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                httpClient.newCall(req).execute().use { res ->
                    withContext(Dispatchers.Main) {
                        isSendingCmd = false
                        if (!res.isSuccessful) {
                            lastCmdStatus = "Hata: HTTP ${res.code}"
                        } else {
                            lastCmdStatus = null
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isSendingCmd = false
                    lastCmdStatus = "Bağlantı koptu"
                }
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C15))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Tv, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "GECE TV Kumandası",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isConnected) Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isConnected) {
                                    val displayHost = tvUrl.removePrefix("http://").removePrefix("https://").take(22)
                                    "Bağlı: $displayHost"
                                } else {
                                    "Televizyon Bağlı Değil"
                                },
                                fontSize = 11.sp,
                                color = if (isConnected) Color(0xFF34D399) else Color(0xFFF87171),
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // QR Scanner Trigger Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showQrScanner = true },
                        color = Color(0x223B82F6),
                        border = BorderStroke(1.dp, Color(0x443B82F6)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Tara", tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isConnected) "Değiştir" else "QR Okut", fontSize = 12.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (isConnected) {
                        Spacer(Modifier.width(8.dp))
                        // TV Standby / Power Button
                        IconButton(
                            onClick = { sendCommand("power") },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EF4444))
                                .border(1.dp, Color(0x66EF4444), CircleShape),
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Güç", tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            if (!isConnected) {
                // Not Connected State: Welcome & QR Prompt Card
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111728)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0x333B82F6)),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0x223B82F6))
                                .border(1.dp, Color(0x443B82F6), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(36.dp),
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Telefonunuzu Kumandaya Dönüştürün",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Televizyonunuzda Ayarlar > Telefon ile Yönet (Web Yönetimi) menüsündeki QR kodu okutarak kumanda moduna geçebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = { showQrScanner = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                            ),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Kamera ile QR Kodu Tara", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showManualDialog = true },
                            color = Color.Transparent,
                        ) {
                            Text(
                                "Veya TV IP Adresi ile Manuel Bağlan ›",
                                fontSize = 13.sp,
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                            )
                        }

                        if (tvUrl.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { isConnected = true },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2210B981)),
                                border = BorderStroke(1.dp, Color(0x4410B981)),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Son Bağlantıyı Kullan ($tvUrl)", fontSize = 13.sp, color = Color(0xFF34D399))
                            }
                        }
                    }
                }
            } else {
                // Connected State: Full Interactive Remote Control
                Spacer(Modifier.height(8.dp))

                // Search / Text Input Bar (Send text directly to TV)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, Color(0x22FFFFFF)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = { searchInput = it },
                            placeholder = { Text("TV'ye metin veya arama gönder…", fontSize = 13.sp, color = Color(0xFF64748B)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (searchInput.isNotBlank()) {
                                    sendCommand("search", searchInput)
                                    keyboardController?.hide()
                                }
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = {
                                if (searchInput.isNotBlank()) {
                                    sendCommand("search", searchInput)
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Navigation D-Pad (Circular Obsidian Controller Wheel)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF1E2846), Color(0xFF0F1526))
                            )
                        )
                        .border(2.dp, Color(0x333B82F6), CircleShape)
                        .shadow(12.dp, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    // UP Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(70.dp, 60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { sendCommand("up") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı", tint = Color.White, modifier = Modifier.size(34.dp))
                    }

                    // DOWN Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .size(70.dp, 60.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { sendCommand("down") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı", tint = Color.White, modifier = Modifier.size(34.dp))
                    }

                    // LEFT Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 8.dp)
                            .size(60.dp, 70.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { sendCommand("left") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Sol", tint = Color.White, modifier = Modifier.size(34.dp))
                    }

                    // RIGHT Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp)
                            .size(60.dp, 70.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .clickable { sendCommand("right") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Sağ", tint = Color.White, modifier = Modifier.size(34.dp))
                    }

                    // CENTER OK BUTTON
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                )
                            )
                            .border(2.dp, Color(0xFF60A5FA), CircleShape)
                            .clickable { sendCommand("ok") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("OK", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Core Navigation Buttons (BACK, HOME, MENU)
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    RemoteIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "Geri",
                        onClick = { sendCommand("back") },
                    )
                    RemoteIconButton(
                        icon = Icons.Default.Home,
                        label = "Ana Menü",
                        onClick = { sendCommand("home") },
                    )
                    RemoteIconButton(
                        icon = Icons.Default.Menu,
                        label = "Rehber",
                        onClick = { sendCommand("ok") },
                    )
                }

                Spacer(Modifier.height(18.dp))

                // Volume & Channel Vertical Rockers
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Volume Column (Pill shape)
                    Column(
                        modifier = Modifier
                            .width(68.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .background(Color(0xFF161F38))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(34.dp))
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { sendCommand("vol_up") }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Ses Aç", tint = Color.White)
                        }
                        Text("SES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        IconButton(onClick = { sendCommand("vol_down") }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Ses Kıs", tint = Color.White)
                        }
                    }

                    // Center Control Buttons (Mute & Play/Pause & Numpad Toggle)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Play / Pause
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { sendCommand("play_pause") },
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0x443B82F6)),
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Oynat/Duraklat", tint = Color(0xFF60A5FA), modifier = Modifier.size(28.dp))
                            }
                        }

                        // Mute
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { sendCommand("mute") },
                            color = Color(0xFF1A2234),
                            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.VolumeMute, contentDescription = "Sessiz", tint = Color(0xFFCBD5E1), modifier = Modifier.size(20.dp))
                            }
                        }

                        // Numpad Toggle Button
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable { showNumpad = !showNumpad },
                            color = if (showNumpad) Color(0xFF2563EB) else Color(0xFF1A2234),
                            border = BorderStroke(1.dp, if (showNumpad) Color(0xFF60A5FA) else Color(0x22FFFFFF)),
                            shape = CircleShape,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Dialpad, contentDescription = "Sayılar", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Channel Column (Pill shape)
                    Column(
                        modifier = Modifier
                            .width(68.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .background(Color(0xFF161F38))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(34.dp))
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(onClick = { sendCommand("ch_up") }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Kanal İleri", tint = Color.White)
                        }
                        Text("KANAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                        IconButton(onClick = { sendCommand("ch_down") }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Kanal Geri", tint = Color.White)
                        }
                    }
                }

                // Expandable Numeric Keypad (1 - 9, 0)
                AnimatedVisibility(
                    visible = showNumpad,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF111728))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Kanal Numarası Tuşla", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        val numbers = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("", "0", ""),
                        )
                        numbers.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                row.forEach { num ->
                                    if (num.isNotEmpty()) {
                                        Surface(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .clickable { sendCommand("num", num) },
                                            color = Color(0xFF1E263E),
                                            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                                            shape = CircleShape,
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(num, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    } else {
                                        Spacer(Modifier.size(54.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Fullscreen QR Code Camera Scanner Modal
    if (showQrScanner) {
        CameraScannerDialog(
            onDismiss = { showQrScanner = false },
            onQrCodeDetected = { rawCode ->
                parseAndConnect(rawCode)
            },
        )
    }

    // Manual IP Dialog
    if (showManualDialog) {
        var manualInput by remember { mutableStateOf(tvUrl) }
        Dialog(onDismissRequest = { showManualDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                border = BorderStroke(1.dp, Color(0x333B82F6)),
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("TV IP Adresini Girin", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("Televizyon ekranında yazan adresi veya IP'yi girin (Örn: 192.168.1.35:45678/?t=abc)", fontSize = 12.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        placeholder = { Text("http://192.168.1.X:PORT/?t=TOKEN", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showManualDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("İptal", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                if (manualInput.isNotBlank()) parseAndConnect(manualInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Bağlan", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            color = Color(0xFF141C30),
            border = BorderStroke(1.dp, Color(0x22FFFFFF)),
            shape = CircleShape,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}

/**
 * Camera Viewfinder Dialog with Scanner Reticle and Real-Time QR Decoding.
 */
@Composable
fun CameraScannerDialog(
    onDismiss: () -> Unit,
    onQrCodeDetected: (String) -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            if (hasCameraPermission) {
                // Live Camera SurfaceView
                CameraSurfacePreview(onQrCodeDetected = onQrCodeDetected)

                // Laser Scanning Line Animation
                val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
                val laserPosition by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 240f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "laser_y",
                )

                // Scanner Viewfinder Overlay
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(3.dp, Color(0xFF3B82F6), RoundedCornerShape(24.dp)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = laserPosition.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0xFF60A5FA), Color(0xFF38BDF8), Color.Transparent)
                                )
                            )
                    )
                }

                // Top Close Button and Guidance
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("TV'deki QR Kodu Hizalayın", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000)),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xCC0A0F1D),
                        border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    ) {
                        Text(
                            "Televizyondaki QR kodunu karenin içine getirin",
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            } else {
                // Permission Denied View
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Kamera İzni Gerekiyor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    Text("TV kumandasıyla eşleşmek için kameranın QR kodu okumasına izin vermelisiniz.", color = Color(0xFF94A3B8), textAlign = TextAlign.Center, fontSize = 13.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    ) {
                        Text("İzin Ver", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                        Text("Vazgeç", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

/**
 * SurfaceView hosting the camera hardware and streaming frames to ZXing decoder.
 */
@Suppress("DEPRECATION")
@Composable
private fun CameraSurfacePreview(
    onQrCodeDetected: (String) -> Unit,
) {
    var camera by remember { mutableStateOf<Camera?>(null) }
    var hasDecoded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                camera?.setPreviewCallback(null)
                camera?.stopPreview()
                camera?.release()
                camera = null
            } catch (_: Exception) {}
        }
    }

    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        try {
                            val cam = Camera.open()
                            camera = cam
                            cam.setDisplayOrientation(90)
                            val params = cam.parameters
                            val focusModes = params.supportedFocusModes
                            if (focusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                            }
                            cam.parameters = params
                            cam.setPreviewDisplay(holder)
                            cam.setPreviewCallback { data, c ->
                                if (hasDecoded || data == null) return@setPreviewCallback
                                val size = c.parameters.previewSize ?: return@setPreviewCallback
                                CoroutineScope(Dispatchers.Default).launch {
                                    val qr = QrCodes.decodeYuv(data, size.width, size.height)
                                    if (!qr.isNullOrBlank() && !hasDecoded) {
                                        hasDecoded = true
                                        withContext(Dispatchers.Main) {
                                            onQrCodeDetected(qr)
                                        }
                                    }
                                }
                            }
                            cam.startPreview()
                        } catch (e: Exception) {
                            Log.e("CameraSurfacePreview", "Kamera başlatılamadı", e)
                        }
                    }

                    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        try {
                            camera?.setPreviewCallback(null)
                            camera?.stopPreview()
                            camera?.release()
                            camera = null
                        } catch (_: Exception) {}
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}
