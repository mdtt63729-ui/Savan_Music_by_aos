package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Premium Animated Logo Splash Screen adhering strictly to the PRD:
 * - App Launch -> Pure Black (#000000) background
 * - Stage 1 (0-100ms): Logo centered, initial state (Alpha = 0, Scale = 0.75, TranslationY = 8dp, Rotation = -1°)
 * - Stage 2 (100-500ms): Smooth reveal (Alpha: 0 -> 1, Scale: 0.75 -> 1.0, FastOutSlowInEasing)
 * - Stage 3 (500-700ms): Subtle premium bounce (Scale: 1.00 -> 1.04 -> 1.00)
 * - Stage 4 (700-1000ms): Splash hold (Centered, stable background)
 * - Stage 5 (1000-1240ms): Smooth exit (Alpha: 1 -> 0, Scale: 1.00 -> 1.03) -> Main UI enters
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val translationYPx = with(density) { 8.dp.toPx() }

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.75f) }
    val logoTranslationY = remember { Animatable(translationYPx) }
    val logoRotation = remember { Animatable(-1f) }

    LaunchedEffect(Unit) {
        // Stage 1: Initial state hold (0ms - 100ms)
        delay(100)

        // Stage 2: Logo Reveal (100ms - 500ms, duration = 400ms)
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoTranslationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // Stage 3: Premium Bounce (~180-220ms: 1.00 -> 1.04 -> 1.00)
        logoScale.animateTo(
            targetValue = 1.04f,
            animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1.00f,
            animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing)
        )

        // Stage 4: Splash Hold (~250-350ms)
        delay(300)

        // Stage 5: Splash Exit Animation (200-240ms)
        launch {
            logoScale.animateTo(
                targetValue = 1.03f,
                animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
            )
        }
        logoAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
        )

        // Transition to Main UI
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Pristine uploaded logo rendered centered without stretching, cropping, or distortion
        Image(
            painter = painterResource(id = R.drawable.app_logo_white),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(136.dp)
                .graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    translationY = logoTranslationY.value
                    rotationZ = logoRotation.value
                }
                .testTag("splash_logo")
        )
    }
}
