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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

@Composable
fun FullPlayer(
    track: Track,
    isPlaying: Boolean,
    atmosphereConfig: AtmosphereConfig,
    onPlayPauseClick: () -> Unit,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(accentColor.copy(alpha = 0.82f), accentColor)
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1:42", color = Color(0xFFC8BED8))
                Text("3:45", color = Color(0xFFC8BED8))
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerIconButton("SH", 46.dp, onClick = {})
                PlayerIconButton("<<", 54.dp, onClick = {})
                PlayButton(
                    isPlaying = isPlaying,
                    accentColor = accentColor,
                    onClick = onPlayPauseClick
                )
                PlayerIconButton(">>", 54.dp, onClick = {})
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
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
