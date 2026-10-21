/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.pairing

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer

/**
 * Ultra low-latency UDP Virtual Mouse Receiver for TV.
 * Receives touch/drag packets over UDP port 8766 from paired phones and
 * dispatches hardware-accelerated pointer updates and synthetic touch clicks.
 */
class VirtualMouseServer(private val scope: CoroutineScope) {

    private var socket: DatagramSocket? = null
    @Volatile private var isRunning = false
    private var activityRef: Activity? = null

    private val _pointerX = MutableStateFlow(960f)
    val pointerX = _pointerX.asStateFlow()

    private val _pointerY = MutableStateFlow(540f)
    val pointerY = _pointerY.asStateFlow()

    private val _isPointerVisible = MutableStateFlow(false)
    val isPointerVisible = _isPointerVisible.asStateFlow()

    private val _isClicking = MutableStateFlow(false)
    val isClicking = _isClicking.asStateFlow()

    @Volatile var isSearchActiveOnTv = false

    private var screenWidth = 1920f
    private var screenHeight = 1080f
    private var autoHideJob: Job? = null

    fun attachActivity(activity: Activity) {
        activityRef = activity
        val metrics = activity.resources.displayMetrics
        screenWidth = metrics.widthPixels.toFloat().coerceAtLeast(1280f)
        screenHeight = metrics.heightPixels.toFloat().coerceAtLeast(720f)
        _pointerX.value = screenWidth / 2f
        _pointerY.value = screenHeight / 2f
    }

    fun detachActivity() {
        activityRef = null
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        scope.launch(Dispatchers.IO) {
            val s = try {
                DatagramSocket(UDP_PORT)
            } catch (e: Exception) {
                Log.w(TAG, "Could not open UDP socket on $UDP_PORT", e)
                return@launch
            }
            socket = s
            val buffer = ByteArray(64)
            val packet = DatagramPacket(buffer, buffer.size)

            while (isRunning) {
                try {
                    s.receive(packet)
                    handlePacket(buffer, packet.length)
                } catch (e: Exception) {
                    if (!isRunning) break
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        runCatching { socket?.close() }
        socket = null
        _isPointerVisible.value = false
        autoHideJob?.cancel()
    }

    fun activatePointer() {
        resetAutoHide()
    }

    fun hidePointer() {
        _isPointerVisible.value = false
        autoHideJob?.cancel()
    }

    fun triggerClick() {
        resetAutoHide()
        val activity = activityRef ?: return
        val px = _pointerX.value
        val py = _pointerY.value

        activity.runOnUiThread {
            scope.launch {
                _isClicking.value = true
                delay(130)
                _isClicking.value = false
            }
            val now = SystemClock.uptimeMillis()
            val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, px, py, 0)
            val up = MotionEvent.obtain(now, now + 35, MotionEvent.ACTION_UP, px, py, 0)
            try {
                activity.window.decorView.dispatchTouchEvent(down)
                activity.window.decorView.dispatchTouchEvent(up)
            } catch (_: Exception) {
            } finally {
                down.recycle()
                up.recycle()
            }
        }
    }

    fun triggerBack() {
        resetAutoHide()
        activityRef?.let { act ->
            act.runOnUiThread {
                (act as? ComponentActivity)?.onBackPressedDispatcher?.onBackPressed()
                    ?: act.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
            }
        }
    }

    private fun resetAutoHide() {
        _isPointerVisible.value = true
        autoHideJob?.cancel()
        autoHideJob = scope.launch {
            delay(AUTO_HIDE_DELAY_MS)
            _isPointerVisible.value = false
        }
    }

    private fun handlePacket(data: ByteArray, length: Int) {
        if (length < 1) return
        val bb = ByteBuffer.wrap(data, 0, length)
        val type = bb.get().toInt()

        when (type) {
            // Type 1: Move: float dx, float dy
            1 -> {
                if (length >= 9) {
                    val dx = bb.float
                    val dy = bb.float
                    val sensitivity = 1.35f
                    val newX = (_pointerX.value + dx * sensitivity).coerceIn(0f, screenWidth)
                    val newY = (_pointerY.value + dy * sensitivity).coerceIn(0f, screenHeight)
                    _pointerX.value = newX
                    _pointerY.value = newY
                    resetAutoHide()
                }
            }
            // Type 2: Left Click (Tap)
            2 -> {
                triggerClick()
            }
            // Type 3: Scroll: float scrollY
            3 -> {
                if (length >= 5) {
                    val scrollY = bb.float
                    resetAutoHide()
                    triggerScroll(scrollY)
                }
            }
            // Type 4: Back / Right click
            4 -> {
                triggerBack()
            }
            // Type 5: Activate
            5 -> {
                resetAutoHide()
            }
            // Type 6: Deactivate / Hide pointer
            6 -> {
                hidePointer()
            }
        }
    }

    private fun triggerScroll(scrollY: Float) {
        val activity = activityRef ?: return
        val px = _pointerX.value
        val py = _pointerY.value

        activity.runOnUiThread {
            val now = SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_SCROLL, px, py, 0).apply {
                setSource(android.view.InputDevice.SOURCE_MOUSE or android.view.InputDevice.SOURCE_CLASS_POINTER)
            }
            try {
                activity.window.decorView.dispatchGenericMotionEvent(event)
            } catch (_: Exception) {
            } finally {
                event.recycle()
            }
        }
    }

    companion object {
        const val TAG = "VirtualMouseServer"
        const val UDP_PORT = 8766
        private const val AUTO_HIDE_DELAY_MS = 12_000L
    }
}
