package com.opensound.app.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import kotlin.math.roundToInt

@Composable
fun AtmosphereMiniPlayerContent(
    track: Track,
    isPlaying: Boolean,
    atmosphereConfig: AtmosphereConfig,
    onPlayPauseClick: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(22.dp)
    val accentColor = Color(atmosphereConfig.accentColor)
    val panelAlpha = atmosphereConfig.panelOpacity

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
    ) {
        CharacterGlow(
            modifier = Modifier
                .size((atmosphereConfig.characterSize + 42).dp)
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        atmosphereConfig.characterX.roundToInt(),
                        atmosphereConfig.characterY.roundToInt() + 12
                    )
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.68f * atmosphereConfig.glowIntensity),
                            accentColor.copy(alpha = 0.28f * atmosphereConfig.glowIntensity),
                            Color.Transparent
                        )
                    )
                )
                .zIndex(1f)
        )

        Image(
            painter = painterResource(id = R.drawable.character),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(atmosphereConfig.characterSize.dp)
                .align(Alignment.TopCenter)
                .offset {
                    IntOffset(
                        atmosphereConfig.characterX.roundToInt(),
                        atmosphereConfig.characterY.roundToInt()
                    )
                }
                .zIndex(2f)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .shadow(18.dp, panelShape, clip = false)
                .clip(panelShape)
                .background(Color(0xFF100D18).copy(alpha = panelAlpha))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.78f),
                            Color.White.copy(alpha = 0.12f),
                            accentColor.copy(alpha = 0.55f)
                        )
                    ),
                    shape = panelShape
                )
                .clickable { onOpenFullPlayer() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(0f)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF110D1B).copy(alpha = panelAlpha),
                                Color(0xFF1A1029).copy(alpha = panelAlpha * 0.86f),
                                Color(0xFF090811).copy(alpha = panelAlpha)
                            )
                        )
                    )
                    .zIndex(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .zIndex(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cover),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist,
                        color = Color(0xFFC8BED8),
                        maxLines = 1
                    )
                }

                MiniControlButton(
                    label = if (isPlaying) "II" else ">",
                    size = 42.dp,
                    onClick = onPlayPauseClick
                )

                Spacer(modifier = Modifier.width(8.dp))

                MiniControlButton(
                    label = "EQ",
                    size = 38.dp,
                    onClick = onOpenFullPlayer
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.38f)
                    .height(3.dp)
                    .background(accentColor)
                    .zIndex(3f)
            )
        }
    }
}

@Composable
private fun CharacterGlow(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}

@Composable
private fun MiniControlButton(
    label: String,
    size: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
