package com.opensound.app.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    modifier: Modifier = Modifier,
    atmosphereConfig: AtmosphereConfig,
    playbackSeconds: Float = 0f,
) {
    val playerModifier = modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp)
        .padding(bottom = if (track.isShowcase) 96.dp else 120.dp)

    if (track.isShowcase) {
        ReelsShowcaseMiniPlayer(
            track = track,
            isPlaying = isPlaying,
            playbackSeconds = playbackSeconds,
            onPlayPauseClick = onPlayPauseClick,
            onOpenFullPlayer = onOpenFullPlayer,
            modifier = playerModifier
        )
        return
    }

    AtmosphereMiniPlayerContent(
        track = track,
        isPlaying = isPlaying,
        atmosphereConfig = atmosphereConfig,
        onPlayPauseClick = onPlayPauseClick,
        onOpenFullPlayer = onOpenFullPlayer,
        modifier = playerModifier
    )
}
