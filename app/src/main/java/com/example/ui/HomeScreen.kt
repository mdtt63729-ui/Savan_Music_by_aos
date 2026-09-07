package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.PlaybackStatus
import com.example.model.Song
import com.example.ui.components.MiniPlayer
import com.example.ui.components.NowPlayingSheet
import com.example.ui.components.SongItemCard
import com.example.ui.theme.ActiveRed
import com.example.ui.theme.CleanMinimalActiveRed
import com.example.ui.theme.CleanMinimalBackground
import com.example.ui.theme.CleanMinimalBorder
import com.example.ui.theme.CleanMinimalOnPrimaryContainer
import com.example.ui.theme.CleanMinimalOnSecondaryContainer
import com.example.ui.theme.CleanMinimalPrimary
import com.example.ui.theme.CleanMinimalPrimaryContainer
import com.example.ui.theme.CleanMinimalSecondaryContainer
import com.example.ui.theme.CleanMinimalSurfaceContainer
import com.example.ui.theme.CleanMinimalTextPrimary
import com.example.ui.theme.CleanMinimalTextSecondary
import com.example.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val playbackStatus by viewModel.playbackStatus.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val lyrics by viewModel.lyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFavoritesTab by viewModel.showFavoritesTab.collectAsState()

    val focusManager = LocalFocusManager.current

    val displayedSongs = if (showFavoritesTab) favorites else songs

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = CleanMinimalBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                MiniPlayer(
                    song = currentSong,
                    playbackStatus = playbackStatus,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    audioQuality = audioQuality,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextClick = { viewModel.playNext() },
                    onClick = { viewModel.setNowPlayingExpanded(true) }
                )

                // Clean Minimalism Bottom Navigation Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    color = CleanMinimalSurfaceContainer,
                    border = BorderStroke(width = 1.dp, color = CleanMinimalBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isHomeSelected = !showFavoritesTab && (selectedCategory.equals("Trending", ignoreCase = true) && searchQuery.isBlank())
                        val isSongsSelected = !showFavoritesTab && (!selectedCategory.equals("Trending", ignoreCase = true) || searchQuery.isNotBlank())
                        val isSavedSelected = showFavoritesTab

                        // Home Tab
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    viewModel.setShowFavoritesTab(false)
                                    if (searchQuery.isNotBlank() || !selectedCategory.equals("Trending", ignoreCase = true)) {
                                        viewModel.onCategorySelected("Trending")
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isHomeSelected) CleanMinimalSecondaryContainer else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isHomeSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home",
                                    tint = if (isHomeSelected) CleanMinimalOnSecondaryContainer else CleanMinimalTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHomeSelected) CleanMinimalTextPrimary else CleanMinimalTextSecondary
                            )
                        }

                        // Songs Tab
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    viewModel.setShowFavoritesTab(false)
                                    if (selectedCategory.equals("Trending", ignoreCase = true) && searchQuery.isBlank()) {
                                        viewModel.onCategorySelected("Bollywood")
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSongsSelected) CleanMinimalSecondaryContainer else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSongsSelected) Icons.Filled.MusicNote else Icons.Outlined.MusicNote,
                                    contentDescription = "Songs",
                                    tint = if (isSongsSelected) CleanMinimalOnSecondaryContainer else CleanMinimalTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "Songs",
                                fontSize = 11.sp,
                                fontWeight = if (isSongsSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSongsSelected) CleanMinimalTextPrimary else CleanMinimalTextSecondary
                            )
                        }

                        // Saved Tab
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    focusManager.clearFocus()
                                    viewModel.setShowFavoritesTab(true)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSavedSelected) CleanMinimalSecondaryContainer else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (favorites.isNotEmpty()) {
                                            Badge(
                                                containerColor = CleanMinimalActiveRed,
                                                contentColor = Color.White
                                            ) {
                                                Text("${favorites.size}", fontSize = 9.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSavedSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Saved",
                                        tint = if (isSavedSelected) CleanMinimalOnSecondaryContainer else CleanMinimalTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Saved",
                                fontSize = 11.sp,
                                fontWeight = if (isSavedSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSavedSelected) CleanMinimalTextPrimary else CleanMinimalTextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            // App Header Bar (Design HTML pattern)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CleanMinimalPrimary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "App Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Savan Music",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.2).sp
                            ),
                            color = CleanMinimalTextPrimary
                        )
                        Text(
                            text = "LOSSLESS HI-FI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = CleanMinimalPrimary
                        )
                    }
                }

                // Header Action: Favorites Heart
                IconButton(
                    onClick = { viewModel.toggleFavoritesTab() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .testTag("favorites_toggle")
                ) {
                    BadgedBox(
                        badge = {
                            if (favorites.isNotEmpty()) {
                                Badge(
                                    containerColor = CleanMinimalActiveRed,
                                    contentColor = Color.White
                                ) {
                                    Text("${favorites.size}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (showFavoritesTab) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Saved Favorites",
                            tint = if (showFavoritesTab) CleanMinimalActiveRed else CleanMinimalTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = {
                    Text(
                        text = "Search songs, artists, 320kbps lossless...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CleanMinimalTextSecondary.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = CleanMinimalPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear search",
                                tint = CleanMinimalTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    viewModel.performSearch()
                }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CleanMinimalPrimary,
                    unfocusedBorderColor = CleanMinimalBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = CleanMinimalSurfaceContainer,
                    focusedTextColor = CleanMinimalTextPrimary,
                    unfocusedTextColor = CleanMinimalTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_input")
            )

            // Category Chips Row (hidden in favorites mode)
            if (!showFavoritesTab) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.categories) { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true) && searchQuery.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.onCategorySelected(category)
                            },
                            label = {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CleanMinimalPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = CleanMinimalSurfaceContainer,
                                labelColor = CleanMinimalTextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) CleanMinimalPrimary else CleanMinimalBorder,
                                selectedBorderColor = CleanMinimalPrimary,
                                borderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("category_chip_$category")
                        )
                    }
                }
            }

            // Featured Hero Card (from Design HTML: Now Trending card)
            if (!showFavoritesTab && searchQuery.isBlank() && displayedSongs.isNotEmpty() && !isLoading) {
                val featuredSong = displayedSongs.first()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { viewModel.playSong(featuredSong, displayedSongs) }
                        .testTag("featured_song_card"),
                    shape = RoundedCornerShape(28.dp),
                    color = CleanMinimalPrimaryContainer,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NOW TRENDING",
                                    color = CleanMinimalOnPrimaryContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = featuredSong.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = CleanMinimalOnPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = featuredSong.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CleanMinimalTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                            ) {
                                AsyncImage(
                                    model = featuredSong.imageUrl,
                                    placeholder = painterResource(id = R.drawable.app_logo),
                                    error = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "Featured Cover",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "320KBPS",
                                        color = CleanMinimalOnPrimaryContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SAAVN-HQ",
                                        color = CleanMinimalOnPrimaryContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = CleanMinimalPrimary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Play Featured",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section Header (from Design HTML: Your Library / See all)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showFavoritesTab) "Your Library (${favorites.size})" else "Your Library",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = CleanMinimalTextPrimary
                )
                if (!showFavoritesTab) {
                    Text(
                        text = if (selectedCategory.isNotEmpty()) selectedCategory else "Top Tracks",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = CleanMinimalPrimary
                    )
                }
            }

            // Songs List Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = CleanMinimalPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading highest quality tracks...",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanMinimalTextSecondary
                        )
                    }
                } else if (errorMessage != null && displayedSongs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "Unable to fetch tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CleanMinimalActiveRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.performSearch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanMinimalPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (displayedSongs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = CleanMinimalTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showFavoritesTab) "No favorite songs yet" else "No songs found",
                            style = MaterialTheme.typography.titleMedium,
                            color = CleanMinimalTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (showFavoritesTab) "Tap the heart icon on any song to save it here" else "Try searching for a song or artist name",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanMinimalTextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp)
                    ) {
                        items(displayedSongs, key = { it.id }) { song ->
                            val isPlaying = currentSong?.id == song.id && playbackStatus == PlaybackStatus.PLAYING
                            val isFav = viewModel.isFavorite(song.id)
                            SongItemCard(
                                song = song,
                                isPlaying = isPlaying,
                                isFavorite = isFav,
                                onSongClick = { viewModel.playSong(song, displayedSongs) },
                                onFavoriteClick = { viewModel.toggleFavorite(song) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Now Playing Sheet
    AnimatedVisibility(
        visible = isNowPlayingExpanded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        NowPlayingSheet(
            song = currentSong,
            playbackStatus = playbackStatus,
            currentPositionMs = currentPositionMs,
            durationMs = durationMs,
            audioQuality = audioQuality,
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            isFavorite = currentSong?.let { viewModel.isFavorite(it.id) } ?: false,
            queue = queue,
            lyrics = lyrics,
            isLoadingLyrics = isLoadingLyrics,
            onDismiss = { viewModel.setNowPlayingExpanded(false) },
            onPlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() },
            onSeek = { viewModel.seekTo(it) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onCycleRepeat = { viewModel.cycleRepeatMode() },
            onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
            onSelectQuality = { viewModel.setAudioQuality(it) },
            onQueueSongClick = { viewModel.playSong(it, queue) }
        )
    }
}
