package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.example.ui.splash.SplashScreen(
        onSplashFinished = onSplashFinished,
        modifier = modifier
    )
}

