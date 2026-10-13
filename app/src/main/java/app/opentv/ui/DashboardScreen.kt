package app.opentv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import app.opentv.R
import app.opentv.core.AppSettings
import app.opentv.data.model.Movie
import app.opentv.data.model.Series
import app.opentv.data.model.Source
import app.opentv.data.parser.displayTitle
import app.opentv.ui.SourcesViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onOpenMovie: (Movie) -> Unit,
    onOpenSeries: (Series) -> Unit,
    onResume: (mediaKey: String, url: String, title: String) -> Unit,
    onOpenLicense: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenMultiView: () -> Unit = {},
    onAddSource: () -> Unit = {},
    activeProfileName: String = "Me",
    onOpenProfiles: () -> Unit = {},
    isTelevision: Boolean = true,
    onNavigateTab: ((Tab) -> Unit)? = null,
    vodViewModel: VodViewModel = viewModel(),
    sourcesViewModel: SourcesViewModel = viewModel(),
) {
    val context = LocalContext.current
    val settings = remember { AppSettings.get(context) }
    val activeSourceId by settings.activeSourceId.collectAsState()
    val sourcesUi by sourcesViewModel.ui.collectAsState()
    var providerMenuExpanded by remember { mutableStateOf(false) }

    val recentMovies by vodViewModel.recentlyAddedMovies.collectAsState()
    val recentSeries by vodViewModel.recentlyAddedSeries.collectAsState()
    val recommendedMovies by vodViewModel.recommendedMovies.collectAsState()
    val continueWatching by vodViewModel.continueWatching.collectAsState()

    var currentTime by remember { mutableStateOf(Calendar.getInstance().time) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = Calendar.getInstance().time
        }
    }
    
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy EEEE", Locale("tr", "TR")) }

    val totalMoviesCount by vodViewModel.totalMoviesCount.collectAsState()
    val totalSeriesCount by vodViewModel.totalSeriesCount.collectAsState()
    val totalChannelsCount by vodViewModel.totalChannelsCount.collectAsState()
    val accountExpiryMillis by vodViewModel.accountExpiryMillis.collectAsState()
    val expiryFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale("tr", "TR")) }
    val expiryDisplayText = remember(accountExpiryMillis, totalChannelsCount) {
        when {
            accountExpiryMillis > 0L -> expiryFormatter.format(Date(accountExpiryMillis))
            totalChannelsCount > 0 -> "Süresiz"
            else -> "-"
        }
    }
    val numberFormatter = remember { java.text.NumberFormat.getInstance(Locale("tr", "TR")) }
    val formattedSeries = remember(totalSeriesCount) { numberFormatter.format(totalSeriesCount) }
    val formattedMovies = remember(totalMoviesCount) { numberFormatter.format(totalMoviesCount) }
    val formattedChannels = remember(totalChannelsCount) { numberFormatter.format(totalChannelsCount) }
    val formattedShowcase = remember(recentMovies, recentSeries) {
        val total = recentMovies.size + recentSeries.size
        numberFormatter.format(if (total > 0) total else recentMovies.size)
    }

    if (!isTelevision) {
        PhoneDashboardContent(
            recentMovies = recentMovies,
            recentSeries = recentSeries,
            recommendedMovies = recommendedMovies,
            continueWatching = continueWatching,
            formattedSeries = formattedSeries,
            formattedMovies = formattedMovies,
            formattedChannels = formattedChannels,
            formattedShowcase = formattedShowcase,
            expiryDisplayText = expiryDisplayText,
            currentTime = currentTime,
            dateFormatter = dateFormatter,
            timeFormatter = timeFormatter,
            onOpenMovie = onOpenMovie,
            onOpenSeries = onOpenSeries,
            onResume = onResume,
            onOpenLicense = onOpenLicense,
            onOpenSearch = onOpenSearch,
            onOpenSettings = onOpenSettings,
            onOpenMultiView = onOpenMultiView,
            onAddSource = onAddSource,
            activeProfileName = activeProfileName,
            onOpenProfiles = onOpenProfiles,
            sources = sourcesUi.sources,
            activeSourceId = activeSourceId,
            onSelectSource = { settings.setActiveSourceId(it.id) },
            onNavigateTab = onNavigateTab,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background,
                    ),
                    radius = 1600f,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            // Top Header & Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Logo & Title Area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_opentv_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GECE IPTV",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Yeni içerikler • Canlı Yayınlar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                // Modern Stat Cards & Status & Clock Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    StatCard("TOPLAM DİZİ", formattedSeries, "adet")
                    StatCard("TOPLAM FİLM", formattedMovies, "adet")
                    StatCard("CANLI KANAL", formattedChannels, "adet")
                    StatCard("YENİ İÇERİK", formattedShowcase, "vitrin")
                    
                    // Active Status Card with Expiry Date & Quick License Access
                    val statusInteractionSource = remember { MutableInteractionSource() }
                    val isStatusFocused by statusInteractionSource.collectIsFocusedAsState()

                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isStatusFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface,
                            )
                            .border(
                                1.dp,
                                if (isStatusFocused) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp),
                            )
                            .clickable(
                                interactionSource = statusInteractionSource,
                                indication = null,
                                onClick = onOpenLicense,
                            )
                            .focusable(interactionSource = statusInteractionSource)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    "DURUM",
                                    color = if (isStatusFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E)),
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = expiryDisplayText,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    // Clock Card - Directly next to Durum card
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Column {
                            Text(
                                dateFormatter.format(currentTime),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 7.5.sp,
                                maxLines = 1,
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                timeFormatter.format(currentTime),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sağlayıcı Seçimi Akordiyon Menüsü (Kullanıcı seçebilir ve yeni ekleyebilir)
            ProviderAccordionBar(
                sources = sourcesUi.sources,
                activeSourceId = activeSourceId,
                onSelectSource = { settings.setActiveSourceId(it.id) },
                onAddSource = onAddSource,
                expanded = providerMenuExpanded,
                onToggleExpand = { providerMenuExpanded = !providerMenuExpanded },
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Content Area (3 Columns)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Left Column: Yeni Diziler
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ListColumn(
                        title = "Yeni Diziler",
                        badge = "SON ${recentSeries.take(10).size}",
                        items = recentSeries.take(10),
                        isMovie = false,
                        onItemClick = { onOpenSeries(it as Series) },
                    )
                }

                // Center Column: İzlemeye Devam Et (Continue Watching)
                Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                    ContinueWatchingColumn(
                        continueWatching = continueWatching,
                        recommendedFallback = recommendedMovies.firstOrNull(),
                        onResume = onResume,
                        onOpenMovie = onOpenMovie,
                    )
                }

                // Right Column: Yeni Filmler
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ListColumn(
                        title = "Yeni Filmler",
                        badge = "SON ${recentMovies.take(10).size}",
                        items = recentMovies.take(10),
                        isMovie = true,
                        onItemClick = { onOpenMovie(it as Movie) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alt Grid: Ayarlar, Arama, Çoklu TV Özelleştirme
            BottomActionGrid(
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onOpenMultiView = onOpenMultiView,
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, unit: String) {
    Box(
        modifier = Modifier
            .width(88.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    unit,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.5.sp,
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingColumn(
    continueWatching: List<VodViewModel.ResumeItem>,
    recommendedFallback: Movie?,
    onResume: (mediaKey: String, url: String, title: String) -> Unit,
    onOpenMovie: (Movie) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏱️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "İzlemeye Devam Et",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    if (continueWatching.isNotEmpty()) "${continueWatching.size} İÇERİK" else "ÖNERİLEN",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (continueWatching.isNotEmpty()) {
            val primary = continueWatching.first()
            var primaryFocused by remember { mutableStateOf(false) }

            // Hero Continue Watching Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (continueWatching.size > 1) 1.4f else 1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (primaryFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        if (primaryFocused) 2.dp else 1.dp,
                        if (primaryFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp),
                    )
                    .clickable { onResume(primary.mediaKey, primary.streamUrl, primary.title) }
                    .onFocusChanged { primaryFocused = it.isFocused }
                    .focusable()
                    .padding(10.dp),
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Poster with embedded progress bar
                    Box(
                        modifier = Modifier
                            .weight(0.42f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        AsyncImage(
                            model = primary.posterUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                        )
                        // Dark bottom overlay for progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.75f)),
                        ) {
                            LinearProgressIndicator(
                                progress = { primary.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.White.copy(alpha = 0.2f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Info Column
                    Column(
                        modifier = Modifier.weight(0.58f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            val percent = (primary.progress * 100).toInt().coerceIn(1, 99)
                            Text(
                                "▶ KALDIĞIN YERDEN • %$percent",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = primary.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "İzlemeye devam etmek için tıklayın.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                "▶ Hemen Oynat",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            // Secondary Continue Watching Items (if more than 1)
            if (continueWatching.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(continueWatching.drop(1).take(2)) { item ->
                        var subFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (subFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    if (subFocused) 1.5.dp else 0.dp,
                                    if (subFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp),
                                )
                                .clickable { onResume(item.mediaKey, item.streamUrl, item.title) }
                                .onFocusChanged { subFocused = it.isFocused }
                                .focusable()
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp)),
                            ) {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                val percent = (item.progress * 100).toInt().coerceIn(1, 99)
                                Text(
                                    text = "%$percent İZLENDİ",
                                    color = if (subFocused) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = item.title,
                                    color = if (subFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "▶",
                                color = if (subFocused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        } else {
            // Fallback when no continue watching items exist yet
            if (recommendedFallback != null) {
                var recFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (recFocused) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            if (recFocused) 2.dp else 1.dp,
                            if (recFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { onOpenMovie(recommendedFallback) }
                        .onFocusChanged { recFocused = it.isFocused }
                        .focusable()
                        .padding(10.dp),
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(0.42f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp)),
                        ) {
                            AsyncImage(
                                model = recommendedFallback.posterUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(0.58f).fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    "✨ ÖNERİLEN İÇERİK",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = recommendedFallback.displayTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "İzlediğiniz dizi veya filmler yarım kaldığında burada otomatik listelenir.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    "▶ İzlemeye Başla",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "İzlediğiniz dizi ve filmler burada listelenir.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun ListColumn(
    title: String,
    badge: String,
    items: List<Any>,
    isMovie: Boolean,
    onItemClick: (Any) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📺", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            ) {
                Text(
                    badge,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { item ->
                ListItemCard(item = item, isMovie = isMovie, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun ListItemCard(item: Any, isMovie: Boolean, onClick: () -> Unit) {
    val title = if (isMovie) (item as Movie).displayTitle else (item as Series).name
    val poster = if (isMovie) (item as Movie).posterUrl else (item as Series).posterUrl
    val year = if (isMovie) (item as Movie).year?.toString() ?: "" else (item as Series).year?.toString() ?: ""
    
    var focused by remember { mutableStateOf(false) }
    val bg = if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (focused) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val subColor = if (focused) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(
                if (focused) 1.5.dp else 0.dp,
                if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(8.dp),
            )
            .clickable { onClick() }
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = poster,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(34.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.DarkGray),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = "TR | $year | ${if (isMovie) "FİLM" else "DİZİ"}",
                color = subColor,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = title,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Mobile smartphone optimized Dashboard layout.
 * Features a single-column, vertically scrollable feed with touch cards,
 * continue-watching hero, and horizontal carousels (LazyRow).
 */
@Composable
private fun PhoneDashboardContent(
    recentMovies: List<Movie>,
    recentSeries: List<Series>,
    recommendedMovies: List<Movie>,
    continueWatching: List<VodViewModel.ResumeItem>,
    formattedSeries: String,
    formattedMovies: String,
    formattedChannels: String,
    formattedShowcase: String,
    expiryDisplayText: String,
    currentTime: Date,
    dateFormatter: SimpleDateFormat,
    timeFormatter: SimpleDateFormat,
    onOpenMovie: (Movie) -> Unit,
    onOpenSeries: (Series) -> Unit,
    onResume: (mediaKey: String, url: String, title: String) -> Unit,
    onOpenLicense: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMultiView: () -> Unit,
    onAddSource: () -> Unit,
    activeProfileName: String,
    onOpenProfiles: () -> Unit,
    sources: List<Source>,
    activeSourceId: Long,
    onSelectSource: (Source) -> Unit,
    onNavigateTab: ((Tab) -> Unit)?,
) {
    val scrollState = rememberScrollState()
    var phoneProviderMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0A0F1D),
                        MaterialTheme.colorScheme.background,
                    )
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            // 1. Welcome & Status Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenLicense)
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090D16))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_opentv_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GECE IPTV",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E)),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Abonelik: $expiryDisplayText",
                                    color = Color(0xFF22C55E),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = timeFormatter.format(currentTime),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = dateFormatter.format(currentTime),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.5.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. ALT GRİD: Logo, Profil, Arama, Çoklu TV ve Ayarlar Barı (Kullanıcı isteğiyle üste abonelik/saat, alta bu bar alındı)
            PhoneActionBarCard(
                title = "GECE IPTV",
                activeProfileName = activeProfileName,
                onOpenSearch = onOpenSearch,
                onOpenMultiView = onOpenMultiView,
                onOpenSettings = onOpenSettings,
                onOpenProfiles = onOpenProfiles,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Sağlayıcı Seçimi Akordiyon Menüsü
            ProviderAccordionBar(
                sources = sources,
                activeSourceId = activeSourceId,
                onSelectSource = onSelectSource,
                onAddSource = onAddSource,
                expanded = phoneProviderMenuExpanded,
                onToggleExpand = { phoneProviderMenuExpanded = !phoneProviderMenuExpanded },
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 2x2 Quick Navigation Menu Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PhoneMenuCard(
                    title = "CANLI TV",
                    count = formattedChannels,
                    unit = "Kanal",
                    icon = "📺",
                    gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab?.invoke(Tab.LIVE) },
                )
                PhoneMenuCard(
                    title = "FİLMLER",
                    count = formattedMovies,
                    unit = "Film",
                    icon = "🎬",
                    gradientColors = listOf(Color(0xFF7E22CE), Color(0xFF6B21A8)),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab?.invoke(Tab.MOVIES) },
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PhoneMenuCard(
                    title = "DİZİLER",
                    count = formattedSeries,
                    unit = "Dizi",
                    icon = "🍿",
                    gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309)),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab?.invoke(Tab.SHOWS) },
                )
                PhoneMenuCard(
                    title = "YENİ VİTRİN",
                    count = formattedShowcase,
                    unit = "İçerik",
                    icon = "✨",
                    gradientColors = listOf(Color(0xFF059669), Color(0xFF047857)),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTab?.invoke(Tab.MOVIES) },
                )
            }

            // 3. Continue Watching Hero
            if (continueWatching.isNotEmpty()) {
                val primary = continueWatching.first()
                val percent = (primary.progress * 100).toInt().coerceIn(1, 99)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏱️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "İzlemeye Devam Et",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "${continueWatching.size} İÇERİK",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onResume(primary.mediaKey, primary.streamUrl, primary.title) }
                        .padding(10.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(98.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        ) {
                            AsyncImage(
                                model = primary.posterUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.7f)),
                            ) {
                                LinearProgressIndicator(
                                    progress = { primary.progress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    "▶ %$percent KALDIĞIN YERDEN",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = primary.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    "▶ Hemen İzle",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            } else if (recommendedMovies.isNotEmpty()) {
                val rec = recommendedMovies.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Günün Önerisi",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .clickable { onOpenMovie(rec) }
                        .padding(10.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(98.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        ) {
                            AsyncImage(
                                model = rec.posterUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TR | ${rec.year ?: ""} | ÖNERİLEN",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.displayTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    "▶ Detayları Gör",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }

            // 4. Yeni Diziler Carousel (LazyRow)
            if (recentSeries.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📺", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Yeni Diziler",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Tümünü Gör >",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onNavigateTab?.invoke(Tab.SHOWS) },
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(recentSeries.take(12)) { series ->
                        PhonePosterCard(
                            title = series.name,
                            posterUrl = series.posterUrl,
                            tag = "DİZİ",
                            year = series.year?.toString() ?: "",
                            onClick = { onOpenSeries(series) },
                        )
                    }
                }
            }

            // 5. Yeni Filmler Carousel (LazyRow)
            if (recentMovies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Yeni Filmler",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "Tümünü Gör >",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onNavigateTab?.invoke(Tab.MOVIES) },
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(recentMovies.take(12)) { movie ->
                        PhonePosterCard(
                            title = movie.displayTitle,
                            posterUrl = movie.posterUrl,
                            tag = "FİLM",
                            year = movie.year?.toString() ?: "",
                            onClick = { onOpenMovie(movie) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alt Grid: Ayarlar, Arama, Çoklu TV Özelleştirme
            BottomActionGrid(
                onOpenSearch = onOpenSearch,
                onOpenSettings = onOpenSettings,
                onOpenMultiView = onOpenMultiView,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhoneMenuCard(
    title: String,
    count: String,
    unit: String,
    icon: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = count,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(bottom = 1.dp),
                    )
                }
            }
            Text(
                text = icon,
                fontSize = 22.sp,
            )
        }
    }
}

@Composable
private fun PhonePosterCard(
    title: String,
    posterUrl: String?,
    tag: String,
    year: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(105.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .width(105.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF141926))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .padding(5.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            ) {
                Text(
                    text = if (year.isNotBlank()) "$year • $tag" else tag,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Alt Grid İşlem Paneli:
 * Ayarlar, Arama ve Çoklu TV Özelleştirme hızlı işlem kartları.
 * Hem TV kumandası D-Pad ile odaklanabilir (focus border, glow) hem de dokunmatik çalışır.
 */
@Composable
private fun BottomActionGrid(
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMultiView: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BottomActionCard(
            title = "Arama",
            subtitle = "Tüm kanal, film ve dizilerde ara",
            icon = "🔍",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
            borderColor = Color(0xFF38BDF8),
            modifier = Modifier.weight(1f),
            onClick = onOpenSearch,
        )
        BottomActionCard(
            title = "Ayarlar",
            subtitle = "Oynatıcı, ses ve sistem ayarları",
            icon = "⚙️",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
            borderColor = Color(0xFFA855F7),
            modifier = Modifier.weight(1f),
            onClick = onOpenSettings,
        )
        BottomActionCard(
            title = "Çoklu TV Özelleştirme",
            subtitle = "Aynı ekranda birden fazla yayın izle",
            icon = "📺",
            gradientColors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)),
            borderColor = Color(0xFF10B981),
            modifier = Modifier.weight(1.2f),
            onClick = onOpenMultiView,
        )
    }
}

@Composable
private fun BottomActionCard(
    title: String,
    subtitle: String,
    icon: String,
    gradientColors: List<Color>,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    if (isFocused) listOf(borderColor.copy(alpha = 0.28f), Color(0xFF1E293B))
                    else gradientColors
                )
            )
            .border(
                if (isFocused) 2.dp else 1.dp,
                if (isFocused) borderColor else borderColor.copy(alpha = 0.35f),
                RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isFocused) borderColor else Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = icon,
                        fontSize = 18.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        color = if (isFocused) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = subtitle,
                        color = if (isFocused) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text = "▶",
                color = if (isFocused) borderColor else Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Sağlayıcı Seçimi Akordiyon Menüsü:
 * Kullanıcının yüklediği sağlayıcıları listeler, aktif olanı seçtirir
 * ve panelden doğrudan "Yeni Sağlayıcı Ekle" seçeneği sunar.
 */
@Composable
private fun ProviderAccordionBar(
    sources: List<Source>,
    activeSourceId: Long,
    onSelectSource: (Source) -> Unit,
    onAddSource: () -> Unit,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val activeSource = remember(sources, activeSourceId) {
        sources.find { it.id == activeSourceId } ?: sources.firstOrNull()
    }

    val headerInteraction = remember { MutableInteractionSource() }
    val isHeaderFocused by headerInteraction.collectIsFocusedAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D1527))
            .border(
                if (isHeaderFocused) 1.5.dp else 1.dp,
                if (isHeaderFocused) Color(0xFF38BDF8) else Color(0xFF38BDF8).copy(alpha = 0.25f),
                RoundedCornerShape(10.dp),
            )
    ) {
        // Accordion Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clickable(
                    interactionSource = headerInteraction,
                    indication = null,
                    onClick = onToggleExpand,
                )
                .focusable(interactionSource = headerInteraction)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌐", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AKTİF SAĞLAYICI:",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = activeSource?.name ?: "Yüklü Sağlayıcı Yok",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (activeSource != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            activeSource.kind.name,
                            color = Color(0xFF38BDF8),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "(${sources.size} Sağlayıcı)",
                    color = Color(0xFF64748B),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isHeaderFocused) Color(0xFF0284C7) else Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (expanded) "Menüyü Kapat ▲" else "Sağlayıcı Değiştir / Ekle ▼",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Expanded Accordion Panel
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF090E1A))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(sources, key = { it.id }) { src ->
                        val isSelected = activeSource?.id == src.id
                        val itemInteraction = remember { MutableInteractionSource() }
                        val isItemFocused by itemInteraction.collectIsFocusedAsState()

                        Box(
                            modifier = Modifier
                                .width(170.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isItemFocused) Color(0xFF0284C7).copy(alpha = 0.35f)
                                    else if (isSelected) Color(0xFF1E293B)
                                    else Color(0xFF141D2F)
                                )
                                .border(
                                    if (isItemFocused) 2.dp else if (isSelected) 1.5.dp else 1.dp,
                                    if (isItemFocused) Color(0xFF38BDF8)
                                    else if (isSelected) Color(0xFF22C55E)
                                    else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(
                                    interactionSource = itemInteraction,
                                    indication = null,
                                    onClick = { onSelectSource(src) },
                                )
                                .focusable(interactionSource = itemInteraction)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = src.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp),
                                        ) {
                                            Text(
                                                "✔ AKTİF",
                                                color = Color(0xFF22C55E),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${src.kind.name} · ${hostOf(src.url)}",
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    // Add Provider Card Button
                    item {
                        val addInteraction = remember { MutableInteractionSource() }
                        val isAddFocused by addInteraction.collectIsFocusedAsState()

                        Box(
                            modifier = Modifier
                                .width(150.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAddFocused) Color(0xFF0284C7).copy(alpha = 0.4f) else Color(0xFF0F172A))
                                .border(
                                    if (isAddFocused) 2.dp else 1.dp,
                                    if (isAddFocused) Color(0xFF38BDF8) else Color(0xFF0284C7).copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(
                                    interactionSource = addInteraction,
                                    indication = null,
                                    onClick = onAddSource,
                                )
                                .focusable(interactionSource = addInteraction)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("➕", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Yeni Sağlayıcı Ekle",
                                    color = Color(0xFF38BDF8),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Host only — the full URL carries the login and has no place on a settings list. */
private fun hostOf(url: String): String =
    runCatching { java.net.URI(url).host ?: url }.getOrDefault(url)

/**
 * Mobil / Tablet Aksiyon Barı:
 * Logo, Profil ve hızlı işlem butonları (Arama, Çoklu TV, Ayarlar).
 * Kullanıcının isteği üzerine abonelik kartının hemen altına yerleştirilmiştir.
 */
@Composable
private fun PhoneActionBarCard(
    title: String,
    activeProfileName: String,
    onOpenSearch: () -> Unit,
    onOpenMultiView: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfiles: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xF90A0F1D))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(46.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // App Logo & Brand Title + Profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onOpenProfiles),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF090D16))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_opentv_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E)),
                        )
                    }
                    if (activeProfileName.isNotBlank() && activeProfileName != "Varsayılan") {
                        Text(
                            text = activeProfileName,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            // Action Icons (Arama, Çoklu TV, Ayarlar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Arama",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onOpenMultiView,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Çoklu TV",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Ayarlar",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

