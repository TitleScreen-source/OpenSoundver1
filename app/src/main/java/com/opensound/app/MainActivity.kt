package com.opensound.app

import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.atmospherePresets
import com.opensound.app.navigation.BottomNavigation
import com.opensound.app.player.FullPlayer
import com.opensound.app.player.MiniPlayer
import com.opensound.app.screens.ArtistProfileScreen
import com.opensound.app.screens.HomeScreen
import com.opensound.app.screens.LibraryScreen
import com.opensound.app.screens.SearchScreen
import com.opensound.app.screens.TrackStudioScreen
import com.opensound.app.screens.UserProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = AndroidColor.rgb(8, 7, 13)
        window.navigationBarColor = AndroidColor.rgb(8, 7, 13)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            OpenSoundApp()
        }
    }
}
@Composable
fun OpenSoundApp() {
    val context = LocalContext.current

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.track1)
    }

    val tracks = listOf(
        Track("Night Drive", "Synth Waves"),
        Track("Lost Signal", "OpenSound Artist"),
        Track("Echo Dreams", "Cyber Pulse"),
        Track("Midnight City", "Neon Empire")
    )

    var selectedTrack by remember {
        mutableStateOf(tracks[0])
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }

    var isFullPlayerOpen by remember {
        mutableStateOf(false)
    }
    var currentScreen by remember {
        mutableStateOf("home")
    }
    var atmosphereConfigs by remember {
        mutableStateOf(
            mapOf(
                "Night Drive" to atmospherePresets[0],
                "Lost Signal" to atmospherePresets[1],
                "Echo Dreams" to atmospherePresets[2],
                "Midnight City" to atmospherePresets[3]
            )
        )
    }

    val selectedAtmosphereConfig = atmosphereConfigs[selectedTrack.title] ?: AtmosphereConfig()
    val showPersistentPlayer = currentScreen != "studio"

    fun togglePlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        } else {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101014)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    tracks = tracks,
                    selectedTrack = selectedTrack,
                    onTrackClick = { track ->
                        selectedTrack = track
                        if (!mediaPlayer.isPlaying) {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    }
                )

                "profile" -> ArtistProfileScreen(
                    tracks = tracks,
                    onTrackClick = { track ->
                        selectedTrack = track
                        if (!mediaPlayer.isPlaying) {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    },
                    onAddTrackClick = {
                        currentScreen = "studio"
                    }
                )
                "studio" -> TrackStudioScreen(
                    track = selectedTrack,
                    initialConfig = selectedAtmosphereConfig,
                    onSave = { newConfig ->
                        atmosphereConfigs = atmosphereConfigs + (selectedTrack.title to newConfig)
                        currentScreen = "profile"
                    },
                    onClose = {
                        currentScreen = "profile"
                    }
                )
                "search" -> SearchScreen(
                    tracks = tracks,
                    onTrackClick = { track ->
                        selectedTrack = track
                        if (!mediaPlayer.isPlaying) {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    }
                )

                "library" -> LibraryScreen(
                    tracks = tracks,
                    selectedTrack = selectedTrack,
                    onTrackClick = { track ->
                        selectedTrack = track
                        if (!mediaPlayer.isPlaying) {
                            mediaPlayer.start()
                            isPlaying = true
                        }
                    }
                )

                "userProfile" -> UserProfileScreen(
                    tracks = tracks,
                    onTrackClick = { track ->
                        selectedTrack = track
                    }
                )
            }

            if (showPersistentPlayer) {
                MiniPlayer(
                    atmosphereConfig = selectedAtmosphereConfig,
                    track = selectedTrack,
                    isPlaying = isPlaying,
                    onPlayPauseClick = {
                        togglePlay()
                    },
                    onOpenFullPlayer = {
                        isFullPlayerOpen = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(2f)
                )

                BottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { screen ->
                        currentScreen = screen
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f)
                )
            }

            AnimatedVisibility(
                visible = isFullPlayerOpen,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight }
                ) + fadeOut(),
                modifier = Modifier.zIndex(10f)
            ) {
                FullPlayer(
                    track = selectedTrack,
                    isPlaying = isPlaying,
                    atmosphereConfig = selectedAtmosphereConfig,
                    onPlayPauseClick = {
                        togglePlay()
                    },
                    onClose = {
                        isFullPlayerOpen = false
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
