package com.msgtrik.msgtrik.ui.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.msgtrik.msgtrik.R
import com.msgtrik.msgtrik.models.auth.UserProfile
import com.msgtrik.msgtrik.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Singleton object for ImageLoader
object ImageLoaderSingleton {
    private var imageLoader: ImageLoader? = null

    fun getInstance(context: Context): ImageLoader {
        if (imageLoader == null) {
            val networkLogger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(networkLogger)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            imageLoader = ImageLoader.Builder(context.applicationContext)
                .okHttpClient(client)
                .respectCacheHeaders(false)
                .crossfade(true)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()
        }
        return imageLoader!!
    }
}

@Composable
fun UserAvatar(
    userProfile: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    email: String? = null
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoaderSingleton.getInstance(context) }

    val colors = listOf(
        "bg-purple-500",
        "bg-blue-500",
        "bg-green-500",
        "bg-yellow-500",
        "bg-pink-500",
        "bg-indigo-500"
    )

    fun mapTailwindColorToRes(colorClass: String): Int {
        return when (colorClass) {
            "bg-purple-500" -> R.color.bg_purple_500
            "bg-blue-500" -> R.color.bg_blue_500
            "bg-green-500" -> R.color.bg_green_500
            "bg-yellow-500" -> R.color.bg_yellow_500
            "bg-pink-500" -> R.color.bg_pink_500
            "bg-indigo-500" -> R.color.bg_indigo_500
            else -> R.color.bg_purple_500 // default fallback
        }
    }

    fun getBackgroundColor(): String {
        if (userProfile.avatarColor != null) return userProfile.avatarColor
        if (email != null) {
            val hash = email.toCharArray().fold(0) { acc, char ->
                acc + char.code
            }
            return colors[hash % colors.size]
        }
        return "bg-purple-500"
    }

    fun getInitials(name: String?): String {
        if (name.isNullOrBlank()) return "?"
        return name.split(" ")
            .take(2)  // Take first two parts of the name
            .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
            .joinToString("")
            .take(2)  // Ensure we only take 2 characters max
    }

    Box(
        modifier = modifier
            .size(size)
            .background(
                Color(
                    context.getColor(
                        mapTailwindColorToRes(getBackgroundColor())
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (userProfile.avatarUrl?.isNotBlank() == true) {
            val devAvatarUrl = userProfile.avatarUrl.replace(
                "http://localhost:8000",
                Constants.BASE_URL
            )

            val imageRequest = ImageRequest.Builder(context)
                .data(devAvatarUrl)
                .size(size.value.toInt() * 2)
                .crossfade(true)
                .build()

            val painter = rememberAsyncImagePainter(
                model = imageRequest,
                imageLoader = imageLoader
            )

            when (painter.state) {
                is AsyncImagePainter.State.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size / 2),
                        color = Color.White
                    )
                }

                is AsyncImagePainter.State.Error -> {
                    Text(
                        text = getInitials(userProfile.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                is AsyncImagePainter.State.Success -> {
                    Image(
                        painter = painter,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    Text(
                        text = getInitials(userProfile.name),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = getInitials(userProfile.name),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 