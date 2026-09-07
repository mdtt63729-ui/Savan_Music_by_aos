package com.example.data

import android.util.Log
import com.example.crypto.AudioQuality
import com.example.crypto.SaavnMediaDecryptor
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SaavnRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
) {
    companion object {
        private const val TAG = "SaavnRepository"
        private const val BASE_SAAVN_URL = "https://www.jiosaavn.com/api.php"
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    /**
     * Search songs by query with unofficial Saavn dev API protocol,
     * extracting 320kbps highest quality stream URLs.
     */
    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "$BASE_SAAVN_URL?__call=search.getResults&_format=json&_marker=0&api_version=4&ctx=web6dot0&n=40&p=1&q=$encoded"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: return@withContext Result.success(emptyList())
            val songs = parseSongsFromJson(body)
            Result.success(songs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to search songs for query: $query", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch trending / category songs.
     */
    suspend fun getCategorySongs(category: String): Result<List<Song>> {
        val query = when (category.lowercase()) {
            "trending" -> "Trending Hits 2025"
            "bollywood" -> "Bollywood Top 50"
            "global" -> "Global Top Hits"
            "punjabi" -> "Punjabi Hits"
            "romantic" -> "Arijit Singh Romantic"
            "lo-fi" -> "Bollywood Lofi Chill"
            "indie" -> "Indian Indie Hits"
            "dance" -> "Party Dance Hits"
            else -> category
        }
        return searchSongs(query)
    }

    /**
     * Fetches lyrics for a song if available.
     */
    suspend fun getLyrics(lyricsIdOrSongId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$BASE_SAAVN_URL?__call=lyrics.getLyrics&_format=json&_marker=0&api_version=4&ctx=web6dot0&lyrics_id=$lyricsIdOrSongId"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            val json = JSONObject(body)
            val rawLyrics = json.optString("lyrics", "")
            if (rawLyrics.isBlank()) {
                return@withContext Result.failure(Exception("No lyrics found"))
            }

            // Clean <br> and HTML entities in lyrics
            val cleanLyrics = rawLyrics
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n")
            Result.success(SaavnMediaDecryptor.cleanHtmlText(cleanLyrics))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch lyrics for id: $lyricsIdOrSongId", e)
            Result.failure(e)
        }
    }

    private fun parseSongsFromJson(jsonStr: String): List<Song> {
        val songsList = mutableListOf<Song>()
        try {
            val root = JSONObject(jsonStr)
            val resultsArray = root.optJSONArray("results") ?: return emptyList()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                if (id.isBlank()) continue

                val rawTitle = item.optString("title", item.optString("song", "Unknown Song"))
                val title = SaavnMediaDecryptor.cleanHtmlText(rawTitle)

                val rawSubtitle = item.optString("subtitle", "")
                val rawHeaderDesc = item.optString("header_desc", "")
                val artistCandidate = if (rawSubtitle.isNotBlank()) rawSubtitle else rawHeaderDesc
                val artist = SaavnMediaDecryptor.cleanHtmlText(
                    artistCandidate.split("-").firstOrNull()?.trim() ?: "Various Artists"
                )

                val rawImage = item.optString("image", "")
                val imageUrl = SaavnMediaDecryptor.upgradeImageUrl(rawImage)

                val moreInfo = item.optJSONObject("more_info")
                val album = SaavnMediaDecryptor.cleanHtmlText(moreInfo?.optString("album", "") ?: "")
                val durationStr = moreInfo?.optString("duration", "0") ?: "0"
                val durationSeconds = durationStr.toIntOrNull() ?: 0
                val has320 = moreInfo?.optString("320kbps", "true") == "true"
                val lyricsId = moreInfo?.optString("lyrics_id", id).takeIf { !it.isNullOrBlank() }
                val encryptedMediaUrl = moreInfo?.optString("encrypted_media_url", "") ?: ""

                // Decrypt 320kbps highest quality stream URL and fallback 160kbps stream URL
                val streamUrl320 = SaavnMediaDecryptor.decryptUrl(encryptedMediaUrl, AudioQuality.KBPS_320)
                val streamUrl160 = SaavnMediaDecryptor.decryptUrl(encryptedMediaUrl, AudioQuality.KBPS_160)

                val year = item.optString("year", "")
                val language = item.optString("language", "")

                // Only include if a playable stream URL was successfully decrypted
                if (streamUrl320.isNotBlank() || streamUrl160.isNotBlank()) {
                    songsList.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            imageUrl = imageUrl,
                            streamUrl = if (streamUrl320.isNotBlank()) streamUrl320 else streamUrl160,
                            streamUrl160 = streamUrl160,
                            durationSeconds = durationSeconds,
                            has320kbps = has320 && streamUrl320.isNotBlank(),
                            lyricsId = lyricsId,
                            year = year,
                            language = language,
                            encryptedMediaUrl = encryptedMediaUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JioSaavn JSON", e)
        }
        return songsList
    }
}
