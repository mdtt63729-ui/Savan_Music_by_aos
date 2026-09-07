package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.crypto.AudioQuality
import com.example.model.PlaybackStatus
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.ui.theme.CleanMinimalActiveRed
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalOnPrimaryContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalSecondaryContainer
import com.example.ui.theme.CleanMinimalSurfaceContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    song: Song?,
    playbackStatus: PlaybackStatus,
    currentPositionMs: Long,
    durationMs: Long,
    audioQuality: AudioQuality,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isFavorite: Boolean,
    queue: List<Song>,
    lyrics: String?,
    isLoadingLyrics: Boolean,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSelectQuality: (AudioQuality) -> Unit,
    onQueueSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragSliderValue by remember { mutableFloatStateOf(0f) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Player, 1: Lyrics, 2: Queue

    val totalDuration = if (durationMs > 0) durationMs else (song.durationSeconds * 1000L).coerceAtLeast(1L)
    val sliderProgress = if (isDraggingSlider) {
        dragSliderValue
    } else {
        (currentPositionMs.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CleanMinimalBackground)
            .statusBarsPadding()
            .testTag("now_playing_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("collapse_now_playing")
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = CleanMinimalTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM SAAVN DEV API",
                        style = MaterialTheme.typography.labelSmall,
                        color = CleanMinimalTextSecondary,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = song.album.ifBlank { "Ultra HD Audio" },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = CleanMinimalTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Audio Quality Pill Badge
                Surface(
                    onClick = { showQualityDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = CleanMinimalPrimaryContainer,
                    modifier = Modifier.testTag("quality_selector_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.HighQuality,
                            contentDescription = "Audio Quality",
                            tint = CleanMinimalOnPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (audioQuality == AudioQuality.KBPS_320) "320K" else audioQuality.bitrate + "K",
                            color = CleanMinimalOnPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs: Main Player / Lyrics / Up Next Queue
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = CleanMinimalPrimary,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CleanMinimalPrimary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Song",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) CleanMinimalPrimary else CleanMinimalTextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lyrics,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 1) CleanMinimalPrimary else CleanMinimalTextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Lyrics",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) CleanMinimalPrimary else CleanMinimalTextSecondary
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == 2) CleanMinimalPrimary else CleanMinimalTextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Queue (${queue.size})",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 2) CleanMinimalPrimary else CleanMinimalTextSecondary
                            )
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // MAIN PLAYER VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 500x500 High-Res Album Art
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            shadowElevation = 8.dp,
                            color = CleanMinimalSurfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CleanMinimalBorder),
                            modifier = Modifier.size(280.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(id = R.drawable.app_logo),
                                error = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Album Art for ${song.title}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(28.dp))
                            )
                        }

                        // Title, Artist and Favorite
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CleanMinimalTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CleanMinimalTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = onToggleFavorite,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("now_playing_favorite")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Favorited" else "Favorite",
                                    tint = if (isFavorite) CleanMinimalActiveRed else CleanMinimalTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Slider & Timestamps
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(
                                value = sliderProgress,
                                onValueChange = {
                                    isDraggingSlider = true
                                    dragSliderValue = it
                                },
                                onValueChangeFinished = {
                                    isDraggingSlider = false
                                    val targetMs = (dragSliderValue * totalDuration).toLong()
                                    onSeek(targetMs)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = CleanMinimalPrimary,
                                    activeTrackColor = CleanMinimalPrimary,
                                    inactiveTrackColor = CleanMinimalBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("playback_slider")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val currentSec = if (isDraggingSlider) {
                                    (dragSliderValue * totalDuration / 1000).toInt()
                                } else {
                                    (currentPositionMs / 1000).toInt()
                                }
                                val totalSec = (totalDuration / 1000).toInt()

                                Text(
                                    text = formatTime(currentSec),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanMinimalTextSecondary
                                )
                                Text(
                                    text = formatTime(totalSec),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanMinimalTextSecondary
                                )
                            }
                        }

                        // Controls: Shuffle, Prev, Play/Pause, Next, Repeat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Shuffle
                            IconButton(
                                onClick = onToggleShuffle,
                                modifier = Modifier.testTag("shuffle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shuffle,
                                    contentDescription = "Shuffle",
                                    tint = if (isShuffle) CleanMinimalPrimary else CleanMinimalTextSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Previous
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipPrevious,
                                    contentDescription = "Previous Song",
                                    tint = CleanMinimalTextPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Big Play / Pause Button
                            Surface(
                                onClick = onPlayPause,
                                shape = CircleShape,
                                color = CleanMinimalPrimary,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .size(68.dp)
                                    .testTag("now_playing_play_pause")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (playbackStatus) {
                                        PlaybackStatus.BUFFERING -> {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = Color.White,
                                                strokeWidth = 3.dp
                                            )
                                        }
                                        PlaybackStatus.PLAYING -> {
                                            Icon(
                                                imageVector = Icons.Filled.Pause,
                                                contentDescription = "Pause",
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Next
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next Song",
                                    tint = CleanMinimalTextPrimary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            // Repeat Mode
                            IconButton(
                                onClick = onCycleRepeat,
                                modifier = Modifier.testTag("repeat_button")
                            ) {
                                val (icon, tint) = when (repeatMode) {
                                    RepeatMode.OFF -> Icons.Filled.Repeat to CleanMinimalTextSecondary.copy(alpha = 0.5f)
                                    RepeatMode.ALL -> Icons.Filled.Repeat to CleanMinimalPrimary
                                    RepeatMode.ONE -> Icons.Filled.RepeatOne to CleanMinimalPrimary
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Repeat: $repeatMode",
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                1 -> {
                    // LYRICS VIEW
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingLyrics) {
                            CircularProgressIndicator(color = CleanMinimalPrimary)
                        } else if (!lyrics.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = lyrics,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 32.sp,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = CleanMinimalTextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(
                                text = "Lyrics unavailable for this track.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CleanMinimalTextSecondary
                            )
                        }
                    }
                }
                2 -> {
                    // UP NEXT QUEUE VIEW
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(queue) { index, queueSong ->
                            val isCurrent = queueSong.id == song.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onQueueSongClick(queueSong) },
                                color = if (isCurrent) CleanMinimalSecondaryContainer else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isCurrent) CleanMinimalPrimary else CleanMinimalTextSecondary,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    AsyncImage(
                                        model = queueSong.imageUrl,
                                        placeholder = painterResource(id = R.drawable.app_logo),
                                        error = painterResource(id = R.drawable.app_logo),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = queueSong.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = if (isCurrent) CleanMinimalPrimary else CleanMinimalTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = queueSong.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CleanMinimalTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Audio Quality Bottom Sheet
    if (showQualityDialog) {
        ModalBottomSheet(
            onDismissRequest = { showQualityDialog = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CleanMinimalBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Audio Streaming Quality",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CleanMinimalTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Unofficial Saavn API serves direct pristine lossless audio up to 320 kbps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CleanMinimalTextSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))

                AudioQuality.values().forEach { quality ->
                    val isSelected = audioQuality == quality
                    Surface(
                        onClick = {
                            onSelectQuality(quality)
                            showQualityDialog = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CleanMinimalSecondaryContainer else CleanMinimalSurfaceContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CleanMinimalPrimary else CleanMinimalBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = quality.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) CleanMinimalPrimary else CleanMinimalTextPrimary
                                )
                                Text(
                                    text = when (quality) {
                                        AudioQuality.KBPS_320 -> "Highest quality available • Pure HD"
                                        AudioQuality.KBPS_160 -> "Standard streaming quality"
                                        AudioQuality.KBPS_96 -> "Low data usage mode"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CleanMinimalTextSecondary
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CleanMinimalPrimary
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
