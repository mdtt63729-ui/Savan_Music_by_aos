package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
import com.example.crypto.AudioQuality
import com.example.crypto.SaavnMediaDecryptor
import com.example.model.PlaybackStatus
import com.example.model.RepeatMode
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerManager(private val context: Context) {
    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    private val scope = CoroutineScope(Dispatchers.Main)
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _playbackStatus = MutableStateFlow(PlaybackStatus.IDLE)
    val playbackStatus: StateFlow<PlaybackStatus> = _playbackStatus.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _bufferedPercent = MutableStateFlow(0)
    val bufferedPercent: StateFlow<Int> = _bufferedPercent.asStateFlow()

    private val _audioQuality = MutableStateFlow(AudioQuality.KBPS_320)
    val audioQuality: StateFlow<AudioQuality> = _audioQuality.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    init {
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        releasePlayer()
        mediaPlayer = MediaPlayer().apply {
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )

            setOnPreparedListener { mp ->
                _playbackStatus.value = PlaybackStatus.PLAYING
                _durationMs.value = mp.duration.toLong().coerceAtLeast(0L)
                mp.start()
                startProgressTicker()
            }

            setOnBufferingUpdateListener { _, percent ->
                _bufferedPercent.value = percent
            }

            setOnCompletionListener {
                handlePlaybackCompletion()
            }

            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                _playbackStatus.value = PlaybackStatus.ERROR
                // Try fallback stream if currently attempting 320kbps
                val song = _currentSong.value
                if (song != null && song.streamUrl160.isNotBlank() && _audioQuality.value == AudioQuality.KBPS_320) {
                    Log.d(TAG, "Attempting fallback to 160kbps stream...")
                    _audioQuality.value = AudioQuality.KBPS_160
                    playUrl(song.streamUrl160, _currentPositionMs.value)
                }
                true
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song>? = null) {
        if (newQueue != null) {
            _queue.value = newQueue
            _currentIndex.value = newQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        } else if (!_queue.value.any { it.id == song.id }) {
            _queue.value = _queue.value + song
            _currentIndex.value = _queue.value.lastIndex
        } else {
            _currentIndex.value = _queue.value.indexOfFirst { it.id == song.id }
        }

        _currentSong.value = song
        _currentPositionMs.value = 0L
        _durationMs.value = (song.durationSeconds * 1000L).coerceAtLeast(0L)
        _playbackStatus.value = PlaybackStatus.BUFFERING

        val streamUrl = when (_audioQuality.value) {
            AudioQuality.KBPS_320 -> {
                if (song.encryptedMediaUrl.isNotBlank()) {
                    SaavnMediaDecryptor.decryptUrl(song.encryptedMediaUrl, AudioQuality.KBPS_320)
                } else song.streamUrl
            }
            AudioQuality.KBPS_160 -> {
                if (song.encryptedMediaUrl.isNotBlank()) {
                    SaavnMediaDecryptor.decryptUrl(song.encryptedMediaUrl, AudioQuality.KBPS_160)
                } else if (song.streamUrl160.isNotBlank()) song.streamUrl160 else song.streamUrl
            }
            AudioQuality.KBPS_96 -> {
                if (song.encryptedMediaUrl.isNotBlank()) {
                    SaavnMediaDecryptor.decryptUrl(song.encryptedMediaUrl, AudioQuality.KBPS_96)
                } else song.streamUrl
            }
        }

        playUrl(streamUrl, 0L)
    }

    private fun playUrl(url: String, seekPositionMs: Long = 0L) {
        if (url.isBlank()) {
            _playbackStatus.value = PlaybackStatus.ERROR
            return
        }

        try {
            stopProgressTicker()
            initMediaPlayer()
            mediaPlayer?.apply {
                reset()
                setDataSource(url)
                _playbackStatus.value = PlaybackStatus.BUFFERING
                prepareAsync()
                if (seekPositionMs > 0) {
                    setOnPreparedListener { mp ->
                        _playbackStatus.value = PlaybackStatus.PLAYING
                        _durationMs.value = mp.duration.toLong().coerceAtLeast(0L)
                        mp.seekTo(seekPositionMs.toInt())
                        mp.start()
                        startProgressTicker()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting playback for url: $url", e)
            _playbackStatus.value = PlaybackStatus.ERROR
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        when (_playbackStatus.value) {
            PlaybackStatus.PLAYING -> {
                player.pause()
                _playbackStatus.value = PlaybackStatus.PAUSED
                stopProgressTicker()
            }
            PlaybackStatus.PAUSED -> {
                player.start()
                _playbackStatus.value = PlaybackStatus.PLAYING
                startProgressTicker()
            }
            PlaybackStatus.IDLE, PlaybackStatus.ERROR -> {
                _currentSong.value?.let { playSong(it) }
            }
            PlaybackStatus.BUFFERING -> {
                // Currently buffering, do nothing
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        try {
            val target = positionMs.coerceIn(0L, _durationMs.value.coerceAtLeast(1L))
            player.seekTo(target.toInt())
            _currentPositionMs.value = target
        } catch (e: Exception) {
            Log.e(TAG, "Seek failed", e)
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffle.value) {
            (q.indices).random()
        } else {
            val next = _currentIndex.value + 1
            if (next >= q.size) {
                if (_repeatMode.value == RepeatMode.ALL) 0 else return
            } else next
        }

        _currentIndex.value = nextIndex
        playSong(q[nextIndex])
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If played more than 3 seconds, replay current song
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = if (_isShuffle.value) {
            (q.indices).random()
        } else {
            val prev = _currentIndex.value - 1
            if (prev < 0) {
                if (_repeatMode.value == RepeatMode.ALL) q.size - 1 else 0
            } else prev
        }

        _currentIndex.value = prevIndex
        playSong(q[prevIndex])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setAudioQuality(quality: AudioQuality) {
        if (_audioQuality.value == quality) return
        _audioQuality.value = quality
        val song = _currentSong.value ?: return
        val currentPos = _currentPositionMs.value
        // Reload current song with new audio quality at current timestamp
        val newUrl = SaavnMediaDecryptor.decryptUrl(song.encryptedMediaUrl, quality)
        playUrl(if (newUrl.isNotBlank()) newUrl else song.streamUrl, currentPos)
    }

    private fun handlePlaybackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _playbackStatus.value = PlaybackStatus.PLAYING
                startProgressTicker()
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (_currentIndex.value < _queue.value.size - 1) {
                    playNext()
                } else {
                    _playbackStatus.value = PlaybackStatus.PAUSED
                    seekTo(0)
                    stopProgressTicker()
                }
            }
        }
    }

    private fun startProgressTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition.toLong()
                            if (_durationMs.value <= 0 && player.duration > 0) {
                                _durationMs.value = player.duration.toLong()
                            }
                        }
                    } catch (e: Exception) {
                        // ignore state errors during async transitions
                    }
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun release() {
        stopProgressTicker()
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore release exceptions
        } finally {
            mediaPlayer = null
        }
    }
}
