package com.opensound.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.ArtistProfile
import com.opensound.app.models.Track
import com.opensound.app.showcase.AudmoraShowcaseProfileScreen

@Composable
fun ArtistProfileScreen(
    profile: ArtistProfile,
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
                        text = profile.displayName,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = profile.genreLine,
                        color = Color(0xFFC8BED8)
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = profile.bio,
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

