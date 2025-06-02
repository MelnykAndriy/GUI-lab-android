package com.msgtrik.msgtrik.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.msgtrik.msgtrik.R

@Composable
fun MsgtrikTheme(content: @Composable () -> Unit) {
    val colors = lightColors(
        primary = colorResource(id = R.color.purple_web),
        primaryVariant = colorResource(id = R.color.purple_accent),
        secondary = colorResource(id = R.color.purple_web),
        secondaryVariant = colorResource(id = R.color.purple_accent),
        background = Color.White,
        surface = Color.White,
        error = Color(0xFFB00020),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White
    )

    MaterialTheme(
        colors = colors,
        content = content
    )
} 