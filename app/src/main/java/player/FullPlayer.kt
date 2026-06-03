package com.opensound.app.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import kotlin.math.roundToInt

@Composable
fun FullPlayer(
    track: Track,
    isPlaying: Boolean,
    atmosphereConfig: AtmosphereConfig,
    playbackSeconds: Float,
    canSkipPrevious: Boolean,
    canSkipNext: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(atmosphereConfig.accentColor)

    Box(
        modifier = modifier
            .background(Color(0xFF08070D))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 35) {
                            onClose()
                        }
                    }
                )
            }
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
                        listOf(
                            Color(0xD908070D),
                            Color(0xCC120B1C),
                            Color(0xFF08070D)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentColor.copy(alpha = 0.52f * atmosphereConfig.glowIntensity),
                            accentColor.copy(alpha = 0.18f * atmosphereConfig.glowIntensity),
                            Color.Transparent
                        )
                    )
                )
        )

        Image(
            painter = painterResource(id = R.drawable.character),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height((atmosphereConfig.characterSize * 4.1f).dp)
                .align(Alignment.TopCenter)
                .padding(top = 82.dp)
                .offset(x = (atmosphereConfig.characterX * 0.35f).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 26.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton(
                    label = "v",
                    size = 42.dp,
                    onClick = onClose
                )

                PlayerIconButton(
                    label = "...",
                    size = 42.dp,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (atmosphereConfig.overlayText.isNotBlank()) {
                Text(
                    text = atmosphereConfig.overlayText,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .offset(
                            x = (atmosphereConfig.overlayTextX * 0.55f).dp,
                            y = (atmosphereConfig.overlayTextY * 0.55f).dp
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(accentColor.copy(alpha = 0.24f))
                        .border(
                            width = 1.dp,
                            color = accentColor.copy(alpha = 0.68f),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = track.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = track.artist,
                color = Color(0xFFD8D0E6),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(22.dp))

            PlaybackSeekBar(
                playbackSeconds = playbackSeconds,
                durationSeconds = track.durationSeconds,
                accentColor = accentColor,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton("SH", 46.dp, onClick = {})
                PlayerIconButton(
                    label = "<<",
                    size = 54.dp,
                    enabled = canSkipPrevious,
                    onClick = onPreviousClick
                )
                PlayButton(
                    isPlaying = isPlaying,
                    accentColor = accentColor,
                    onClick = onPlayPauseClick
                )
                PlayerIconButton(
                    label = ">>",
                    size = 54.dp,
                    enabled = canSkipNext,
                    onClick = onNextClick
                )
                PlayerIconButton("RE", 46.dp, onClick = {})
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PlayerIconButton("FX", 42.dp, onClick = {})
                PlayerIconButton("LY", 42.dp, onClick = {})
                PlayerIconButton("AT", 42.dp, onClick = {})
                PlayerIconButton("EQ", 42.dp, onClick = {})
            }
        }
    }
}

@Composable
private fun PlaybackSeekBar(
    playbackSeconds: Float,
    durationSeconds: Float,
    accentColor: Color,
    onSeek: (Float) -> Unit
) {
    val safeDuration = durationSeconds.coerceAtLeast(1f)
    val safePlaybackSeconds = playbackSeconds.coerceIn(0f, safeDuration)

    Slider(
        value = safePlaybackSeconds,
        onValueChange = { seconds -> onSeek(seconds) },
        valueRange = 0f..safeDuration,
        colors = SliderDefaults.colors(
            thumbColor = accentColor,
            activeTrackColor = accentColor,
            inactiveTrackColor = Color.White.copy(alpha = 0.14f)
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatPlaybackTime(safePlaybackSeconds),
            color = Color(0xFFC8BED8)
        )
        Text(
            text = formatPlaybackTime(safeDuration),
            color = Color(0xFFC8BED8)
        )
    }
}

private fun formatPlaybackTime(seconds: Float): String {
    val safeSeconds = seconds.coerceAtLeast(0f).roundToInt()
    val minutes = safeSeconds / 60
    val remainingSeconds = safeSeconds % 60

    return "$minutes:${remainingSeconds.toString().padStart(2, '0')}"
}

@Composable
private fun PlayButton(
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(84.dp)
            .shadow(22.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.88f), accentColor)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isPlaying) "II" else ">",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayerIconButton(
    label: String,
    size: Dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.08f else 0.03f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (enabled) 0.12f else 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = if (enabled) 1f else 0.35f),
            fontWeight = FontWeight.SemiBold
        )
    }
}
