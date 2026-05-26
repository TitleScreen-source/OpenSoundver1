package com.opensound.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.opensound.app.R
import com.opensound.app.models.Track

@Composable
fun ArtistProfileScreen(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onAddTrackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101014))
            .padding(bottom = 160.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
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
                        .background(Color(0xAA000000))
                )

                Image(
                    painter = painterResource(id = R.drawable.character),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(210.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 15.dp, y = 20.dp)
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
                        style = MaterialTheme.typography.headlineLarge
                    )

                    Text(
                        text = "Electronic • Ambient • Indie",
                        color = Color.LightGray
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Автор создаёт атмосферную электронную музыку с визуальными сценами для каждого релиза.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(onClick = { onAddTrackClick() }) {
                        Text("Добавить трек")
                    }

                    Card(
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF241A36)
                        )
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
                    style = MaterialTheme.typography.titleLarge
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(track.title, color = Color.White)
                Text(track.artist, color = Color.Gray)
            }
        }
    }
}