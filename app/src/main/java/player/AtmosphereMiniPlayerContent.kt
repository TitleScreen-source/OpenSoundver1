package com.opensound.app.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
    ) {
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xDD1A1028)
            )
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
                    Text(track.title, color = Color.White, maxLines = 1)
                    Text(track.artist, color = Color.LightGray, maxLines = 1)
                }

                TextButton(onClick = { onPlayPauseClick() }) {
                    Text(
                        text = if (isPlaying) "Pause" else "Play",
                        color = Color.White
                    )
                }
            }
        }
    }
}