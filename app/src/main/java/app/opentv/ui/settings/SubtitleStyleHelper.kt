/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.ui.settings

import android.graphics.Color
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import app.opentv.core.AppSettings

@OptIn(UnstableApi::class)
object SubtitleStyleHelper {
    /**
     * Applies user-configured text size, colors, and background edges to the Media3 SubtitleView.
     */
    fun applySubtitlePreferences(subtitleView: SubtitleView?, settings: AppSettings) {
        if (subtitleView == null) return
        val sizeMultiplier = settings.subtitleTextSize.value
        subtitleView.setFractionalTextSize(SubtitleView.DEFAULT_TEXT_SIZE_FRACTION * sizeMultiplier)
        subtitleView.setApplyEmbeddedFontSizes(false)
        subtitleView.setApplyEmbeddedStyles(false)

        val textColor = when (settings.subtitleTextColor.value) {
            "yellow" -> Color.rgb(255, 235, 59)
            "cyan" -> Color.rgb(77, 208, 225)
            "green" -> Color.rgb(129, 199, 132)
            else -> Color.WHITE
        }

        val (bgColor, edgeType, edgeColor) = when (settings.subtitleBackground.value) {
            "outline" -> Triple(
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.BLACK,
            )
            "black" -> Triple(
                Color.BLACK,
                CaptionStyleCompat.EDGE_TYPE_NONE,
                Color.TRANSPARENT,
            )
            "none" -> Triple(
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                Color.BLACK,
            )
            else -> Triple( // "translucent"
                Color.argb(165, 0, 0, 0),
                CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                Color.argb(120, 0, 0, 0),
            )
        }

        subtitleView.setStyle(
            CaptionStyleCompat(
                textColor,
                bgColor,
                Color.TRANSPARENT,
                edgeType,
                edgeColor,
                null,
            ),
        )
    }
}
