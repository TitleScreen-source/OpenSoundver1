package com.opensound.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.opensound.app.R
import kotlin.math.roundToInt
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.limitAtmosphereConfig
import com.opensound.app.player.AtmosphereMiniPlayerContent
import com.opensound.app.models.Track

@Composable
fun TrackStudioScreen(
    initialConfig: AtmosphereConfig,
    onSave: (AtmosphereConfig) -> Unit,
    onClose: () -> Unit
) {
    var characterOffset by remember {
        mutableStateOf(Offset(initialConfig.characterX, initialConfig.characterY))
    }

    var characterSize by remember {
        mutableStateOf(initialConfig.characterSize)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101014))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Track Studio",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = { onClose() }
            ) {
                Text("Закрыть", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Предпросмотр мини-плеера",
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            AtmosphereMiniPlayerContent(
                track = Track("Preview Track", "OpenSound Artist"),
                isPlaying = false,
                atmosphereConfig = AtmosphereConfig(
                    characterX = characterOffset.x,
                    characterY = characterOffset.y,
                    characterSize = characterSize
                ),
                onPlayPauseClick = {},
                onOpenFullPlayer = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            val limitedConfig = limitAtmosphereConfig(
                                AtmosphereConfig(
                                    characterX = characterOffset.x + dragAmount.x,
                                    characterY = characterOffset.y + dragAmount.y,
                                    characterSize = characterSize
                                )
                            )

                            characterOffset = Offset(
                                limitedConfig.characterX,
                                limitedConfig.characterY
                            )
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Размер персонажа",
            color = Color.White
        )

        Slider(
            value = characterSize,
            onValueChange = { characterSize = it },
            valueRange = 70f..150f
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Позиция: x=${characterOffset.x.roundToInt()}, y=${characterOffset.y.roundToInt()}",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    limitAtmosphereConfig(
                        AtmosphereConfig(
                            characterX = characterOffset.x,
                            characterY = characterOffset.y,
                            characterSize = characterSize
                        )
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить атмосферу")
        }
    }
}