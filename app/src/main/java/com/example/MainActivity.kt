package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.HomeScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {
    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false) {
                var isSplashVisible by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = isSplashVisible,
                    transitionSpec = {
                        if (targetState) {
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        } else {
                            (fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)) +
                             scaleIn(initialScale = 0.98f, animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)))
                                .togetherWith(fadeOut(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)))
                        }
                    },
                    label = "splash_to_main_entrance"
                ) { showSplash ->
                    if (showSplash) {
                        SplashScreen(
                            onSplashFinished = { isSplashVisible = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        HomeScreen(
                            viewModel = musicViewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

