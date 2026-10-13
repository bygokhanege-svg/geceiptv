/*
 * This file is part of OpenTV.
 * Copyright (C) 2026 The OpenTV Contributors
 * Licensed under the GNU General Public License v3.0 or later.
 */
package app.opentv.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.opentv.R

/**
 * Modern glassmorphism settings hub configured as a 5-column square grid.
 * Each square card features frosted-glass translucent styling, specular highlight borders,
 * and a centered glass badge housing the feature's icon.
 */
@Composable
fun SettingsHubScreen(
    onOpenProviders: () -> Unit,
    onOpenAddons: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenChannels: () -> Unit,
    onOpenWebManager: () -> Unit,
    onOpenDisplay: () -> Unit,
    onOpenSubtitles: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenParental: () -> Unit,
    onOpenSync: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenProfiles: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenLicense: () -> Unit = {},
    onOpenAbout: () -> Unit,
    onBack: () -> Unit,
) {
    val entries = listOf(
        HubEntry(
            icon = Icons.Filled.WorkspacePremium,
            title = stringResource(R.string.settings_license_title),
            subtitle = stringResource(R.string.settings_license_subtitle),
            onClick = onOpenLicense,
        ),
        HubEntry(
            icon = Icons.AutoMirrored.Filled.PlaylistPlay,
            title = stringResource(R.string.settings_providers_title),
            subtitle = stringResource(R.string.settings_providers_subtitle),
            onClick = onOpenProviders,
        ),
        HubEntry(
            icon = Icons.Filled.Extension,
            title = stringResource(R.string.settings_addons_title),
            subtitle = stringResource(R.string.settings_addons_subtitle),
            onClick = onOpenAddons,
        ),
        HubEntry(
            icon = Icons.Filled.ViewTimeline,
            title = stringResource(R.string.settings_guide_title),
            subtitle = stringResource(R.string.settings_guide_subtitle),
            onClick = onOpenGuide,
        ),
        HubEntry(
            icon = Icons.Filled.LiveTv,
            title = stringResource(R.string.common_channels),
            subtitle = stringResource(R.string.settings_channels_subtitle),
            onClick = onOpenChannels,
        ),
        HubEntry(
            icon = Icons.Filled.QrCode2,
            title = stringResource(R.string.settings_webmanager_title),
            subtitle = stringResource(R.string.settings_webmanager_subtitle),
            onClick = onOpenWebManager,
        ),
        HubEntry(
            icon = Icons.Filled.DisplaySettings,
            title = stringResource(R.string.settings_display_title),
            subtitle = stringResource(R.string.settings_display_subtitle),
            onClick = onOpenDisplay,
        ),
        HubEntry(
            icon = Icons.Filled.Subtitles,
            title = stringResource(R.string.settings_subtitles_hub_title),
            subtitle = stringResource(R.string.settings_subtitles_hub_subtitle),
            onClick = onOpenSubtitles,
        ),
        HubEntry(
            icon = Icons.Filled.Translate,
            title = stringResource(R.string.settings_language_title),
            subtitle = stringResource(R.string.settings_language_subtitle),
            onClick = onOpenLanguage,
        ),
        HubEntry(
            icon = Icons.Filled.Bedtime,
            title = stringResource(R.string.settings_sleep_timer),
            subtitle = stringResource(R.string.settings_sleep_subtitle),
            onClick = onOpenSleepTimer,
        ),
        HubEntry(
            icon = Icons.Filled.VideoLibrary,
            title = stringResource(R.string.settings_recording_title),
            subtitle = stringResource(R.string.settings_recording_subtitle),
            onClick = onOpenRecordings,
        ),
        HubEntry(
            icon = Icons.Filled.Security,
            title = stringResource(R.string.settings_parental_title),
            subtitle = stringResource(R.string.settings_parental_subtitle),
            onClick = onOpenParental,
        ),
        HubEntry(
            icon = Icons.Filled.CloudSync,
            title = stringResource(R.string.settings_sync_title),
            subtitle = stringResource(R.string.settings_sync_subtitle),
            onClick = onOpenSync,
        ),
        HubEntry(
            icon = Icons.Outlined.AccountCircle,
            title = stringResource(R.string.settings_profiles_hub_title),
            subtitle = stringResource(R.string.settings_profiles_hub_subtitle),
            onClick = onOpenProfiles,
        ),
        HubEntry(
            icon = Icons.Filled.Search,
            title = stringResource(R.string.settings_search_hub_title),
            subtitle = stringResource(R.string.settings_search_hub_subtitle),
            onClick = onOpenSearch,
        ),
        HubEntry(
            icon = Icons.Filled.Info,
            title = stringResource(R.string.settings_about_title),
            subtitle = stringResource(R.string.settings_about_subtitle),
            onClick = onOpenAbout,
        ),
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f),
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.05f),
                                )
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))
            HubTextButton(stringResource(R.string.common_done), onBack)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(entries) { entry ->
                HubTile(entry)
            }
        }
    }
}

private data class HubEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

@Composable
private fun HubTile(entry: HubEntry) {
    var focused by remember { mutableStateOf(false) }

    // Frosted glass background brush
    val backgroundBrush = if (focused) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                Color(0xFF161F2E).copy(alpha = 0.90f),
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF232D3F).copy(alpha = 0.52f),
                Color(0xFF111722).copy(alpha = 0.68f),
            )
        )
    }

    // Glass specular light refraction edge border
    val borderBrush = if (focused) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.04f),
            )
        )
    }

    val borderWidth = if (focused) 2.dp else 1.dp
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(backgroundBrush)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
            .clickable(onClick = entry.onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Frosted Glass Icon Badge
            val iconBadgeShape = RoundedCornerShape(16.dp)
            val iconBadgeBg = if (focused) {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f),
                    )
                )
            }
            val iconBadgeBorder = if (focused) {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.40f))
            } else {
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    )
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(iconBadgeShape)
                    .background(iconBadgeBg)
                    .border(iconBadgeBorder, shape = iconBadgeShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = if (focused) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = if (focused) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(3.dp))

            // Subtitle
            Text(
                text = entry.subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                color = if (focused) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                } else {
                    Color.White.copy(alpha = 0.55f)
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun HubTextButton(label: String, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    val bgBrush = if (focused) {
        Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary,
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.04f),
            )
        )
    }

    val border = if (focused) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = 0.25f),
                    Color.White.copy(alpha = 0.05f),
                )
            )
        )
    }

    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (focused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .onFocusChanged { focused = it.isFocused }
            .clip(shape)
            .background(bgBrush)
            .border(border, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 9.dp),
    )
}
