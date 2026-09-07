package com.example.data

import android.content.Context
import com.example.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences("saavn_favorites_prefs", Context.MODE_PRIVATE)
    private val _favorites = MutableStateFlow<List<Song>>(emptyList())
    val favorites: StateFlow<List<Song>> = _favorites.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        val jsonStr = prefs.getString("favorite_songs_json", null) ?: return
        try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<Song>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Song(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        artist = obj.getString("artist"),
                        album = obj.optString("album", ""),
                        imageUrl = obj.getString("imageUrl"),
                        streamUrl = obj.getString("streamUrl"),
                        streamUrl160 = obj.optString("streamUrl160", ""),
                        durationSeconds = obj.optInt("durationSeconds", 0),
                        has320kbps = obj.optBoolean("has320kbps", true),
                        lyricsId = if (obj.has("lyricsId")) obj.getString("lyricsId") else null,
                        year = obj.optString("year", ""),
                        language = obj.optString("language", ""),
                        encryptedMediaUrl = obj.optString("encryptedMediaUrl", "")
                    )
                )
            }
            _favorites.value = list
        } catch (e: Exception) {
            // Ignore parse issues
        }
    }

    private fun saveFavorites() {
        try {
            val jsonArray = JSONArray()
            _favorites.value.forEach { song ->
                val obj = JSONObject().apply {
                    put("id", song.id)
                    put("title", song.title)
                    put("artist", song.artist)
                    put("album", song.album)
                    put("imageUrl", song.imageUrl)
                    put("streamUrl", song.streamUrl)
                    put("streamUrl160", song.streamUrl160)
                    put("durationSeconds", song.durationSeconds)
                    put("has320kbps", song.has320kbps)
                    put("lyricsId", song.lyricsId)
                    put("year", song.year)
                    put("language", song.language)
                    put("encryptedMediaUrl", song.encryptedMediaUrl)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("favorite_songs_json", jsonArray.toString()).apply()
        } catch (e: Exception) {
            // Ignore save issues
        }
    }

    fun isFavorite(songId: String): Boolean {
        return _favorites.value.any { it.id == songId }
    }

    fun toggleFavorite(song: Song) {
        val current = _favorites.value.toMutableList()
        val index = current.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            current.removeAt(index)
        } else {
            current.add(0, song)
        }
        _favorites.value = current
        saveFavorites()
    }
}
