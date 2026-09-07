package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.crypto.AudioQuality
import com.example.crypto.SaavnMediaDecryptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Saavn Music", appName)
  }

  @Test
  fun `test 320kbps url decryption`() {
    val sampleRawUrl = "https://aac.saavncdn.com/123/sample_song_96.mp4"
    val encrypted = SaavnMediaDecryptor.encryptUrl(sampleRawUrl)
    val decrypted320 = SaavnMediaDecryptor.decryptUrl(encrypted, AudioQuality.KBPS_320)
    assertTrue("Should contain 320.mp4 and be decrypted", decrypted320.contains("_320.mp4"))
    assertEquals("https://aac.saavncdn.com/123/sample_song_320.mp4", decrypted320)
  }

  @Test
  fun `test 500x500 high res image upgrade`() {
    val lowRes = "https://c.saavncdn.com/450/Song-150x150.jpg"
    val highRes = SaavnMediaDecryptor.upgradeImageUrl(lowRes)
    assertEquals("https://c.saavncdn.com/450/Song-500x500.jpg", highRes)
  }
}

