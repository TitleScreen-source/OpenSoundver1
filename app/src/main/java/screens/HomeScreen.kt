package com.opensound.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.opensound.app.R
import com.opensound.app.models.Track

@Composable
fun HomeScreen(
    tracks: List<Track>,
    selectedTrack: Track,
    onTrackClick: (Track) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 40.dp, bottom = 150.dp)
    ) {
        Text(
            text = "OpenSound",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Что будем слушать сегодня?",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeaturedTrackCard(
            track = selectedTrack,
            onClick = { onTrackClick(selectedTrack) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Рекомендуем",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Смотреть все",
                color = Color(0xFF9B5CFF),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tracks) { track ->
                SmallTrackCard(
                    track = track,
                    onClick = { onTrackClick(track) }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Недавно прослушано",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        tracks.take(2).forEach { track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(track) }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun FeaturedTrackCard(
    track: Track,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF201A2E)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
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
                    .background(Color(0x99000000))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = track.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = track.artist,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun SmallTrackCard(
    track: Track,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.cover),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.title,
            color = Color.White,
            maxLines = 1
        )

        Text(
            text = track.artist,
            color = Color.Gray,
            maxLines = 1
        )
    }
}

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B1B22)
        )
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = track.title,
                    color = Color.White
                )

                Text(
                    text = track.artist,
                    color = Color.Gray
                )
            }
        }
    }
}