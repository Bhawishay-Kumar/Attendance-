package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@Composable
fun StudentAvatar(
    photoUri: String?,
    studentName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showBorder: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clickableModifier = if (onClick != null) modifier.clickable { onClick() } else modifier

    val gradientRing = Brush.sweepGradient(
        listOf(
            Color(0xFF38BDF8),
            Color(0xFF2563EB),
            Color(0xFF7C3AED),
            Color(0xFF06B6D4),
            Color(0xFF38BDF8)
        )
    )

    Box(
        modifier = clickableModifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Outer Luminous Ring Frame
        if (showBorder) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(gradientRing)
            )
        }

        // Inner Image Surface with 2dp inset for halo frame
        val innerPadding = if (showBorder) 2.dp else 0.dp
        Box(
            modifier = Modifier
                .size(size - (innerPadding * 2))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val hasLocalPhoto = !photoUri.isNullOrBlank() && File(photoUri).exists()
            val hasNetworkPhoto = !photoUri.isNullOrBlank() && (photoUri.startsWith("http://") || photoUri.startsWith("https://") || photoUri.startsWith("content://"))

            if (hasLocalPhoto || hasNetworkPhoto) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (hasLocalPhoto) File(photoUri!!) else photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo of $studentName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                val initial = studentName.trim().firstOrNull()?.uppercase() ?: "S"
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                    Color(0xFF0F172A).copy(alpha = 0.95f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Black,
                        fontSize = (size.value * 0.40f).sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }
    }
}
