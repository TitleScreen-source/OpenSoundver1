package com.opensound.app.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.Track
import kotlin.math.roundToInt

@Composable
fun AtmosphereMiniPlayerContent(
    track: Track,
    isPlaying: Boolean,
    atmosphereConfig: AtmosphereConfig,
    onPlayPauseClick: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    currentTimeSeconds: Float? = null,
    editorDragMode: String? = null,
    onCharacterDrag: ((Float, Float) -> Unit)? = null,
    onTextDrag: ((Float, Float) -> Unit)? = null
) {
    val panelShape = RoundedCornerShape(22.dp)
    val accentColor = Color(atmosphereConfig.accentColor)
    val panelAlpha = atmosphereConfig.panelOpacity
    val activeLayers = atmosphereConfig.layers.filter { layer ->
        layer.isVisible && (currentTimeSeconds == null || currentTimeSeconds in layer.startTime..layer.endTime)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .then(
                when (editorDragMode) {
                    "Character" -> Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onCharacterDrag?.invoke(dragAmount.x, dragAmount.y)
                        }
                    }

                    "Text" -> Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onTextDrag?.invoke(dragAmount.x, dragAmount.y)
                        }
                    }

                    else -> Modifier
                }
            )
    ) {
        activeLayers
            .filter { it.type == AtmosphereLayerType.Text }
            .forEachIndexed { index, layer ->
                val text = textForLayer(layer, atmosphereConfig)
                if (text.isNotBlank()) {
                    val layerAccent = Color(layer.accentColor)

                    Text(
                        text = text,
                        color = Color.White.copy(alpha = layer.opacity),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset {
                                IntOffset(
                                    xForLayer(layer, atmosphereConfig).roundToInt(),
                                    yForLayer(layer, atmosphereConfig).roundToInt()
                                )
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .background(layerAccent.copy(alpha = 0.28f * layer.opacity))
                            .border(
                                width = 1.dp,
                                color = layerAccent.copy(alpha = 0.72f * layer.opacity),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .zIndex(5f + index * 0.01f)
                    )
                }
            }

        activeLayers
            .filter { it.type == AtmosphereLayerType.Character }
            .forEachIndexed { index, layer ->
                val layerAccent = Color(layer.accentColor)
                val characterSize = (scaleForLayer(layer, atmosphereConfig) * 100f)
                    .coerceIn(70f, 150f)
                val characterX = xForLayer(layer, atmosphereConfig)
                val characterY = yForLayer(layer, atmosphereConfig)

                CharacterGlow(
                    modifier = Modifier
                        .size((characterSize + 42).dp)
                        .align(Alignment.TopCenter)
                        .offset {
                            IntOffset(
                                characterX.roundToInt(),
                                characterY.roundToInt() + 12
                            )
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    layerAccent.copy(alpha = 0.68f * atmosphereConfig.glowIntensity * layer.opacity),
                                    layerAccent.copy(alpha = 0.28f * atmosphereConfig.glowIntensity * layer.opacity),
                                    Color.Transparent
                                )
                            )
                        )
                        .zIndex(1f + index * 0.01f)
                )

                Image(
                    painter = painterResource(id = R.drawable.character),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alpha = layer.opacity,
                    modifier = Modifier
                        .size(characterSize.dp)
                        .align(Alignment.TopCenter)
                        .offset {
                            IntOffset(
                                characterX.roundToInt(),
                                characterY.roundToInt()
                            )
                        }
                        .zIndex(2f + index * 0.01f)
                )
            }

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
                .zIndex(1f)
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
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter)
                .zIndex(7f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
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
            )
        }
    }
}

@Composable
private fun CharacterGlow(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}

private fun textForLayer(
    layer: AtmosphereLayer,
    config: AtmosphereConfig
): String {
    return if (layer.id == "text-main") {
        layer.text.ifBlank { config.overlayText }
    } else {
        layer.text
    }
}

private fun xForLayer(
    layer: AtmosphereLayer,
    config: AtmosphereConfig
): Float {
    return when {
        layer.id == "character-main" -> config.characterX
        layer.id == "text-main" -> config.overlayTextX
        else -> layer.x
    }
}

private fun yForLayer(
    layer: AtmosphereLayer,
    config: AtmosphereConfig
): Float {
    return when {
        layer.id == "character-main" -> config.characterY
        layer.id == "text-main" -> config.overlayTextY
        else -> layer.y
    }
}

private fun scaleForLayer(
    layer: AtmosphereLayer,
    config: AtmosphereConfig
): Float {
    return if (layer.id == "character-main") {
        config.characterSize / 100f
    } else {
        layer.scale
    }
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
