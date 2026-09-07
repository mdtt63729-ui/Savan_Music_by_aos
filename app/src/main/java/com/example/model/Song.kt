package com.example.model

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val imageUrl: String,
    val streamUrl: String,
    val streamUrl160: String = "",
    val durationSeconds: Int = 0,
    val has320kbps: Boolean = true,
    val lyricsId: String? = null,
    val year: String = "",
    val language: String = "",
    val encryptedMediaUrl: String = ""
) {
    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "--:--"
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

enum class PlaybackStatus {
    IDLE,
    BUFFERING,
    PLAYING,
    PAUSED,
    ERROR
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}
