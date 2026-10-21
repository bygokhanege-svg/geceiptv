/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.ui.channels

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.opentv.R

/**
 * A d-pad-navigable on-screen keyboard, shared by every screen that needs text entry on a TV.
 * Android TV's system text field punts to "type on your phone"; this keeps entry on the remote.
 */
@Composable
internal fun OnScreenKeyboard(
    onKey: (String) -> Unit,
    onSpace: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
) {
    var isUppercase by remember { mutableStateOf(false) }
    val firstKey = remember { FocusRequester() }
    val context = LocalContext.current

    val row0 = "1234567890"
    val row1 = if (isUppercase) "QWERTYUIOP" else "qwertyuiop"
    val row2 = if (isUppercase) "ASDFGHJKL" else "asdfghjkl"
    val row3 = if (isUppercase) "ZXCVBNM" else "zxcvbnm"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row0.forEachIndexed { colIndex, ch ->
                KeyCap(
                    label = ch.toString(),
                    focusRequester = if (colIndex == 0) firstKey else null,
                    onClick = { onKey(ch.toString()) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row1.forEach { ch ->
                KeyCap(
                    label = ch.toString(),
                    onClick = { onKey(ch.toString()) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row2.forEach { ch ->
                KeyCap(
                    label = ch.toString(),
                    onClick = { onKey(ch.toString()) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeyCap(
                label = stringResource(R.string.kbd_caps),
                wide = 1.5f,
                isActive = isUppercase,
                onClick = { isUppercase = !isUppercase },
            )
            row3.forEach { ch ->
                KeyCap(
                    label = ch.toString(),
                    onClick = { onKey(ch.toString()) },
                )
            }
            KeyCap(
                label = stringResource(R.string.kbd_del),
                icon = true,
                wide = 1.5f,
                onClick = onBackspace,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeyCap(label = stringResource(R.string.kbd_space), wide = 5f, onClick = onSpace)
            KeyCap(
                label = stringResource(R.string.kbd_paste),
                wide = 2.5f,
                onClick = {
                    val pasted = readClipboardText(context)
                    if (pasted != null) onKey(pasted)
                    else Toast.makeText(context, context.getString(R.string.kbd_clipboard_empty), Toast.LENGTH_SHORT).show()
                },
            )
            KeyCap(label = stringResource(R.string.kbd_clear), wide = 2.5f, onClick = onClear)
        }
    }

    LaunchedEffect(Unit) { runCatching { firstKey.requestFocus() } }
}

@Composable
private fun KeyCap(
    label: String,
    onClick: () -> Unit,
    wide: Float = 1f,
    isActive: Boolean = false,
    icon: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val baseBg = when {
        focused -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when {
        focused -> MaterialTheme.colorScheme.onPrimary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val widthDp = (48f * wide + (wide - 1f) * 8f).dp

    Box(
        Modifier
            .height(48.dp)
            .width(widthDp)
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(8.dp))
            .background(baseBg)
            .then(
                if (focused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else if (isActive) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else Modifier,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (icon) {
            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = label, tint = fg)
        } else {
            Text(label, style = MaterialTheme.typography.titleMedium, color = fg, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Plain text currently on the system clipboard, trimmed — or null when there's nothing to paste.
 * Shared by this keyboard's Paste key and the provider form's paste buttons so both read the
 * clipboard the same way.
 */
internal fun readClipboardText(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    val clip = clipboard.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    val text = clip.getItemAt(0)?.coerceToText(context)?.toString()
    return text?.trim()?.takeIf { it.isNotEmpty() }
}
