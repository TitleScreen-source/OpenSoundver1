package com.opensound.app

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.coroutines.delay

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
    val activity = context as? Activity

    val tracks = listOf(
        Track("I Feel Sick", "Subaru Natsuki", isShowcase = true),
        Track("Night Drive", "Synth Waves"),
        Track("Lost Signal", "AUDMORA Artist"),
        Track("Echo Dreams", "Cyber Pulse"),
        Track("Midnight City", "Neon Empire")
    )

    var selectedTrack by remember {
        mutableStateOf(tracks[0])
    }

    val selectedAudioRes = if (selectedTrack.isShowcase) {
        R.raw.rezero_showcase
    } else {
        R.raw.track1
    }

    val mediaPlayer = remember(selectedAudioRes) {
        MediaPlayer.create(context, selectedAudioRes)
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }

    var playbackSeconds by remember {
        mutableStateOf(0f)
    }

    var isFullPlayerOpen by remember {
        mutableStateOf(false)
    }
    var currentScreen by remember {
        mutableStateOf("profile")
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
    val isShowcaseProfile = selectedTrack.isShowcase && currentScreen == "profile"

    DisposableEffect(mediaPlayer) {
        mediaPlayer.setOnCompletionListener { player ->
            player.seekTo(0)
            isPlaying = false
            playbackSeconds = 0f
        }

        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(mediaPlayer, isPlaying) {
        if (isPlaying && !mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }

        while (isPlaying) {
            playbackSeconds = mediaPlayer.currentPosition / 1000f
            delay(33L)
        }
    }

    DisposableEffect(isFullPlayerOpen, isShowcaseProfile, activity) {
        val controller = activity?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView)
        }

        if (isFullPlayerOpen) {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else if (isShowcaseProfile) {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.statusBars())
            controller?.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    fun togglePlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        } else {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    fun selectTrackAndPlay(track: Track) {
        if (track != selectedTrack) {
            selectedTrack = track
            playbackSeconds = 0f
            isPlaying = true
        } else if (!mediaPlayer.isPlaying) {
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
                        selectTrackAndPlay(track)
                    }
                )

                "profile" -> ArtistProfileScreen(
                    tracks = tracks,
                    showcaseMode = selectedTrack.isShowcase,
                    playbackSeconds = playbackSeconds,
                    onTrackClick = { track ->
                        selectTrackAndPlay(track)
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
                        selectTrackAndPlay(track)
                    }
                )

                "library" -> LibraryScreen(
                    tracks = tracks,
                    selectedTrack = selectedTrack,
                    onTrackClick = { track ->
                        selectTrackAndPlay(track)
                    }
                )

                "userProfile" -> UserProfileScreen(
                    tracks = tracks,
                    onTrackClick = { track ->
                        selectTrackAndPlay(track)
                    }
                )
            }

            if (showPersistentPlayer) {
                MiniPlayer(
                    atmosphereConfig = selectedAtmosphereConfig,
                    track = selectedTrack,
                    isPlaying = isPlaying,
                    playbackSeconds = playbackSeconds,
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
