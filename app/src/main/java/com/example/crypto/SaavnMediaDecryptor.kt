package com.example.crypto

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SaavnMediaDecryptor {
    private const val TAG = "SaavnMediaDecryptor"
    private const val CIPHER_KEY = "38346591"

    /**
     * Decrypts the DES-ECB encrypted media URL provided by JioSaavn
     * and upgrades it to the highest quality (320kbps MP4/AAC stream).
     */
    fun decryptUrl(encryptedUrl: String, quality: AudioQuality = AudioQuality.KBPS_320): String {
        if (encryptedUrl.isBlank()) return ""
        val trimmed = encryptedUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return adjustQuality(trimmed, quality)
        }
        return try {
            val keySpec = SecretKeySpec(CIPHER_KEY.toByteArray(Charsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decoded = Base64.decode(trimmed, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decoded)
            val rawUrl = String(decryptedBytes, Charsets.UTF_8).trim()
            adjustQuality(rawUrl, quality)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt media url", e)
            ""
        }
    }

    /**
     * Encrypts a raw media URL with DES-ECB for testing or round-tripping.
     */
    fun encryptUrl(rawUrl: String): String {
        return try {
            val keySpec = SecretKeySpec(CIPHER_KEY.toByteArray(Charsets.UTF_8), "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(rawUrl.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt media url", e)
            ""
        }
    }

    private fun adjustQuality(url: String, quality: AudioQuality): String {
        return when (quality) {
            AudioQuality.KBPS_320 -> {
                url.replace("_96.mp4", "_320.mp4")
                    .replace("_160.mp4", "_320.mp4")
            }
            AudioQuality.KBPS_160 -> {
                url.replace("_96.mp4", "_160.mp4")
                    .replace("_320.mp4", "_160.mp4")
            }
            AudioQuality.KBPS_96 -> {
                url.replace("_320.mp4", "_96.mp4")
                    .replace("_160.mp4", "_96.mp4")
            }
        }
    }

    /**
     * Upgrades low resolution thumbnails (50x50 or 150x150) to crystal-clear 500x500 high-res album art.
     */
    fun upgradeImageUrl(imageUrl: String): String {
        if (imageUrl.isBlank()) return ""
        return imageUrl
            .replace("50x50", "500x500")
            .replace("150x150", "500x500")
    }

    /**
     * Cleans up HTML entities commonly returned in song titles and artist descriptions.
     */
    fun cleanHtmlText(text: String): String {
        if (text.isBlank()) return ""
        return text
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }
}

enum class AudioQuality(val label: String, val bitrate: String) {
    KBPS_320("320 kbps (Ultra HD)", "320"),
    KBPS_160("160 kbps (High Quality)", "160"),
    KBPS_96("96 kbps (Data Saver)", "96")
}
