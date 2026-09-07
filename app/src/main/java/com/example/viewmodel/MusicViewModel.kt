package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.AudioQuality
import com.example.data.FavoritesStore
import com.example.data.SaavnRepository
import com.example.model.PlaybackStatus
import com.example.model.RepeatMode
import com.example.model.Song
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SaavnRepository()
    val playerManager = AudioPlayerManager(application.applicationContext)
    val favoritesStore = FavoritesStore(application.applicationContext)

    val currentSong: StateFlow<Song?> = playerManager.currentSong
    val playbackStatus: StateFlow<PlaybackStatus> = playerManager.playbackStatus
    val currentPositionMs: StateFlow<Long> = playerManager.currentPositionMs
    val durationMs: StateFlow<Long> = playerManager.durationMs
    val audioQuality: StateFlow<AudioQuality> = playerManager.audioQuality
    val isShuffle: StateFlow<Boolean> = playerManager.isShuffle
    val repeatMode: StateFlow<RepeatMode> = playerManager.repeatMode
    val queue: StateFlow<List<Song>> = playerManager.queue
    val favorites: StateFlow<List<Song>> = favoritesStore.favorites

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Trending")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _lyrics = MutableStateFlow<String?>(null)
    val lyrics: StateFlow<String?> = _lyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private val _showFavoritesTab = MutableStateFlow(false)
    val showFavoritesTab: StateFlow<Boolean> = _showFavoritesTab.asStateFlow()

    private var searchDebounceJob: Job? = null

    val categories = listOf(
        "Trending",
        "Bollywood",
        "Global",
        "Punjabi",
        "Romantic",
        "Lo-Fi",
        "Indie",
        "Dance"
    )

    init {
        loadCategory("Trending")
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        _showFavoritesTab.value = false
        _searchQuery.value = ""
        loadCategory(category)
    }

    fun toggleFavoritesTab() {
        _showFavoritesTab.value = !_showFavoritesTab.value
        if (!_showFavoritesTab.value && _songs.value.isEmpty()) {
            loadCategory(_selectedCategory.value)
        }
    }

    fun setShowFavoritesTab(show: Boolean) {
        _showFavoritesTab.value = show
        if (!show && _songs.value.isEmpty()) {
            loadCategory(_selectedCategory.value)
        }
    }

    private fun loadCategory(category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.getCategorySongs(category)
            result.onSuccess { songList ->
                _songs.value = songList
                _isLoading.value = false
            }.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Failed to fetch songs"
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        _showFavoritesTab.value = false

        searchDebounceJob?.cancel()
        if (newQuery.isBlank()) {
            loadCategory(_selectedCategory.value)
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(500) // debounce typing
            searchSongs(newQuery)
        }
    }

    fun performSearch(query: String = _searchQuery.value) {
        searchDebounceJob?.cancel()
        if (query.isBlank()) {
            loadCategory(_selectedCategory.value)
            return
        }
        viewModelScope.launch {
            searchSongs(query)
        }
    }

    private suspend fun searchSongs(query: String) {
        _isLoading.value = true
        _errorMessage.value = null
        val result = repository.searchSongs(query)
        result.onSuccess { songList ->
            _songs.value = songList
            _isLoading.value = false
            if (songList.isEmpty()) {
                _errorMessage.value = "No songs found for \"$query\""
            }
        }.onFailure { error ->
            _errorMessage.value = error.localizedMessage ?: "Search failed"
            _isLoading.value = false
        }
    }

    fun playSong(song: Song, queueList: List<Song>? = null) {
        val currentQueue = queueList ?: if (_showFavoritesTab.value) favorites.value else _songs.value
        playerManager.playSong(song, currentQueue)
        loadLyricsForSong(song)
    }

    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    fun playNext() {
        playerManager.playNext()
        playerManager.currentSong.value?.let { loadLyricsForSong(it) }
    }

    fun playPrevious() {
        playerManager.playPrevious()
        playerManager.currentSong.value?.let { loadLyricsForSong(it) }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerManager.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerManager.cycleRepeatMode()
    }

    fun setAudioQuality(quality: AudioQuality) {
        playerManager.setAudioQuality(quality)
    }

    fun toggleFavorite(song: Song) {
        favoritesStore.toggleFavorite(song)
    }

    fun isFavorite(songId: String): Boolean {
        return favoritesStore.isFavorite(songId)
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
        if (expanded && _lyrics.value == null) {
            currentSong.value?.let { loadLyricsForSong(it) }
        }
    }

    private fun loadLyricsForSong(song: Song) {
        _lyrics.value = null
        val lyricsId = song.lyricsId ?: song.id
        viewModelScope.launch {
            _isLoadingLyrics.value = true
            val result = repository.getLyrics(lyricsId)
            result.onSuccess { lyricText ->
                _lyrics.value = lyricText
                _isLoadingLyrics.value = false
            }.onFailure {
                _lyrics.value = "Lyrics not available for this track"
                _isLoadingLyrics.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
