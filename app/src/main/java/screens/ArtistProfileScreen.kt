package com.opensound.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.Track
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun ArtistProfileScreen(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onAddTrackClick: () -> Unit,
    showcaseMode: Boolean = false,
    playbackSeconds: Float = 0f
) {
    if (showcaseMode) {
        AudmoraShowcaseProfileScreen(
            tracks = tracks,
            playbackSeconds = playbackSeconds,
            onTrackClick = onTrackClick,
            onAddTrackClick = onAddTrackClick
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08070D))
            .padding(bottom = 168.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cover),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x6608070D), Color(0xFF08070D))
                            )
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.character),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(218.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 18.dp, y = 18.dp)
                        .zIndex(2f)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .zIndex(3f)
                ) {
                    Text(
                        text = "Synth Waves",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Electronic • Ambient • Indie",
                        color = Color(0xFFC8BED8)
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Автор создаёт атмосферную электронную музыку с визуальными сценами для каждого релиза.",
                    color = Color(0xFFC8BED8),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onAddTrackClick) {
                        Text("Edit Atmosphere")
                    }

                    Card(
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = "Поддержать",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = "Популярные треки",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        items(tracks) { track ->
            ArtistTrackRow(
                track = track,
                onClick = { onTrackClick(track) }
            )
        }
    }
}

@Composable
private fun AudmoraShowcaseProfileScreen(
    tracks: List<Track>,
    playbackSeconds: Float,
    onTrackClick: (Track) -> Unit,
    onAddTrackClick: () -> Unit
) {
    val pulse = profilePulse(playbackSeconds)
    val madness = profileWindow(playbackSeconds, start = 21.5f, end = 34.5f)
    val collapse = profileSustain(playbackSeconds, start = 29.5f, full = 35.8f)
    val crimson = Color(0xFFFF1740)
    val violet = Color(0xFF9B5CFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030306))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 210.dp)
        ) {
            item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(214.dp)
                    .background(Color.Black)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.audmora_showcase_header),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.06f),
                                Color.Black.copy(alpha = 0.12f + collapse * 0.18f),
                                Color(0xFF030306)
                            )
                        )
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                crimson.copy(alpha = (0.08f + pulse * 0.16f + madness * 0.1f)),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.76f, size.height * 0.5f),
                            radius = size.width * 0.46f
                        ),
                        radius = size.width * 0.46f,
                        center = Offset(size.width * 0.76f, size.height * 0.5f)
                    )

                    for (i in 0 until 34) {
                        val x = size.width * (((i * 31) % 100) / 100f)
                        val baseY = size.height * (((i * 47) % 100) / 100f)
                        val drift = (playbackSeconds * (5f + i % 4) + i * 9f) % size.height
                        val y = (baseY + drift) % size.height
                        val twinkle = 0.35f + 0.5f * abs(sin((playbackSeconds * 1.1f + i).toDouble()).toFloat())

                        drawCircle(
                            color = Color.White.copy(alpha = (0.025f + madness * 0.03f) * twinkle),
                            radius = 0.8f + (i % 3) * 0.55f,
                            center = Offset(x, y)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(78.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF030306))
                            )
                        )
                )

            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF030306))
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 42.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(22.dp, CircleShape, clip = false)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            crimson.copy(alpha = 0.92f + pulse * 0.08f),
                                            violet.copy(alpha = 0.82f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.audmora_showcase_avatar),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Subaru Natsuki",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(crimson.copy(alpha = 0.96f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "@subaru_returns",
                                color = Color(0xFFC6BBD2),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        ShowcaseStat(value = "23", label = "Atmospheres")
                        ShowcaseStat(value = "1.2K", label = "Followers")
                        ShowcaseStat(value = "56", label = "Following")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Creating atmosphere-driven music worlds shaped by emotion, darkness, and light.",
                        color = Color(0xFFD6CEDF),
                        style = MaterialTheme.typography.bodyLarge
                    )

                }

            }
        }
        }

        FrostedGlassOverlay(
            playbackSeconds = playbackSeconds,
            intensity = 0.24f + madness * 0.14f + collapse * 0.2f,
            modifier = Modifier
                .matchParentSize()
                .zIndex(4f)
        )
    }
}

@Composable
private fun FrostedGlassOverlay(
    playbackSeconds: Float,
    intensity: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val glass = intensity.coerceIn(0f, 0.9f)

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = glass * 0.26f)
                ),
                center = Offset(size.width * 0.5f, size.height * 0.46f),
                radius = size.maxDimension * 0.74f
            )
        )

        for (i in 0 until 30) {
            val x = size.width * (0.04f + (((i * 37) % 92) / 100f))
            val rawY = size.height * (((i * 19) % 100) / 100f)
            val fall = (playbackSeconds * (10f + (i % 5) * 2.8f) + i * 13f) % (size.height * 0.34f)
            val y = (rawY + fall) % size.height
            val radius = 2.4f + (i % 5) * 1.6f
            val stretch = 1.0f + (i % 4) * 0.62f
            val alpha = glass * (0.18f + (i % 4) * 0.028f)

            drawOval(
                color = Color.Black.copy(alpha = alpha * 0.62f),
                topLeft = Offset(x - radius * 0.72f + 1.2f, y - radius * stretch + 1.6f),
                size = Size(radius * 1.6f, radius * 2f * stretch)
            )

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * 0.95f),
                        Color(0xFFEAF6FF).copy(alpha = alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(x - radius * 0.24f, y - radius * 0.54f),
                    radius = radius * 2.4f
                ),
                topLeft = Offset(x - radius * 0.72f, y - radius * stretch),
                size = Size(radius * 1.6f, radius * 2f * stretch)
            )

            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.78f),
                radius = radius * 0.22f,
                center = Offset(x - radius * 0.24f, y - radius * 0.54f)
            )
        }

        for (i in 0 until 13) {
            val x = size.width * (0.06f + (((i * 29) % 88) / 100f))
            val startY = size.height * (((i * 13) % 42) / 100f)
            val fall = (playbackSeconds * (8f + i % 3) + i * 17f) % (size.height * 0.22f)
            val y = (startY + fall).coerceIn(0f, size.height)
            val length = size.height * (0.16f + (i % 4) * 0.046f)
            val sway = sin((playbackSeconds * 0.42f + i).toDouble()).toFloat() * 3.2f
            val alpha = glass * (0.14f + (i % 3) * 0.036f)

            drawLine(
                color = Color.Black.copy(alpha = alpha * 0.6f),
                start = Offset(x + 1.1f, y + 1.4f),
                end = Offset(x + sway + 1.1f, y + length + 1.4f),
                strokeWidth = 2.3f + (i % 3) * 0.4f,
                cap = StrokeCap.Round
            )

            drawLine(
                color = Color.White.copy(alpha = alpha),
                start = Offset(x, y),
                end = Offset(x + sway, y + length),
                strokeWidth = 1.25f + (i % 3) * 0.35f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.9f),
                radius = 2.8f + (i % 3) * 0.9f,
                center = Offset(x + sway, y + length)
            )
        }

        val edgeAlpha = glass * 0.22f
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Black.copy(alpha = edgeAlpha),
                    Color.Transparent,
                    Color.Transparent,
                    Color.Black.copy(alpha = edgeAlpha)
                )
            )
        )
    }
}

@Composable
private fun ShowcaseStat(
    value: String,
    label: String
) {
    Column {
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFF9F94AE),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ShowcaseActionButton(
    text: String,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.72f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun profilePulse(time: Float): Float {
    val phase = (time * 1.72f) % 1f
    val first = (1f - abs(phase - 0.08f) / 0.08f).coerceIn(0f, 1f)
    val second = (1f - abs(phase - 0.24f) / 0.05f).coerceIn(0f, 1f) * 0.62f
    return maxOf(first, second)
}

private fun profileWindow(
    time: Float,
    start: Float,
    end: Float
): Float {
    if (time <= start || time >= end) return 0f
    val progress = ((time - start) / (end - start)).coerceIn(0f, 1f)
    return sin(progress.toDouble() * PI).toFloat().coerceIn(0f, 1f)
}

private fun profileSustain(
    time: Float,
    start: Float,
    full: Float
): Float {
    if (time <= start) return 0f
    val progress = ((time - start) / (full - start)).coerceIn(0f, 1f)
    return profileSmoothStep(progress)
}

private fun profileSmoothStep(value: Float): Float {
    val t = value.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

@Composable
fun ArtistTrackRow(
    track: Track,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(track.title, color = Color.White)
                Text(track.artist, color = Color(0xFFA9A1B6))
            }
        }
    }
}
