package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.models.Track
import com.opensound.app.models.UserLibrarySummary

@Composable
fun LibraryScreen(
    summary: UserLibrarySummary,
    tracks: List<Track>,
    selectedTrack: Track,
    onTrackClick: (Track) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08070D))
            .padding(horizontal = 20.dp)
            .padding(top = 40.dp, bottom = 168.dp)
    ) {
        Text(
            text = "Библиотека",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = summary.description,
            color = Color(0xFFC8BED8),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(26.dp))

        tracks.forEach { track ->
            TrackRow(
                track = track,
                onClick = { onTrackClick(track) }
            )
            if (track == selectedTrack) {
                Text(
                    text = "Сейчас играет",
                    color = Color(0xFF9B5CFF),
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
