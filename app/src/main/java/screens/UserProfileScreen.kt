package com.opensound.app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.R
import com.opensound.app.models.ProfileMetric
import com.opensound.app.models.Track
import com.opensound.app.models.UserProfile

@Composable
fun UserProfileScreen(
    profile: UserProfile,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08070D))
            .padding(horizontal = 20.dp)
            .padding(top = 44.dp, bottom = 168.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.cover),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.displayName,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(profile.handle, color = Color(0xFFA9A1B6))

        Spacer(modifier = Modifier.height(22.dp))

        Row {
            profile.metrics.forEach { metric ->
                ProfileStat(metric = metric)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Column(modifier = Modifier.align(Alignment.Start)) {
            Text(
                text = "Избранные треки",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            tracks.forEach { track ->
                TrackRow(track = track, onClick = { onTrackClick(track) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ProfileStat(
    metric: ProfileMetric
) {
    Column(
        modifier = Modifier.padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(metric.value, color = Color.White, fontWeight = FontWeight.Bold)
        Text(metric.label, color = Color(0xFFA9A1B6))
    }
}
