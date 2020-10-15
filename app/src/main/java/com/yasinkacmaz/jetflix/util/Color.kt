package com.yasinkacmaz.jetflix.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedTask
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ContextAmbient
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

private val colorRange = 0..256

fun Color.Companion.randomColor() = Color(colorRange.random(), colorRange.random(), colorRange.random())

@Composable
fun fetchDominantColorFromPoster(
    posterUrl: String,
    colorState: MutableState<Color>,
    defaultColor: Color = Color.randomColor()
) {
    val context = ContextAmbient.current
    LaunchedTask {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(posterUrl)
            .allowHardware(false)
            .build()

        val bitmap = (loader.execute(request) as? SuccessResult)?.drawable?.toBitmap(128, 128) ?: return@LaunchedTask
        val dominantColor = Palette.from(bitmap).generate().getVibrantColor(defaultColor.toArgb())
        colorState.value = Color(dominantColor)
    }
}