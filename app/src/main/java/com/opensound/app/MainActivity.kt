package com.opensound.app

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.opensound.app.navigation.AudMoraScreen
import com.opensound.app.navigation.BottomNavigation
import com.opensound.app.playback.MediaPlayerPlaybackEffect
import com.opensound.app.player.FullPlayer
import com.opensound.app.player.MiniPlayer
import com.opensound.app.screens.ArtistProfileScreen
import com.opensound.app.screens.HomeScreen
import com.opensound.app.screens.LibraryScreen
import com.opensound.app.screens.SearchScreen
import com.opensound.app.screens.TrackStudioScreen
import com.opensound.app.screens.UserProfileScreen
import com.opensound.app.state.AudMoraViewModel
import com.opensound.app.ui.theme.AudMoraTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: AudMoraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            AudMoraViewModel.factory()
        )[AudMoraViewModel::class.java]

        window.statusBarColor = AndroidColor.rgb(8, 7, 13)
        window.navigationBarColor = AndroidColor.rgb(8, 7, 13)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            AudMoraTheme {
                AudMoraApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AudMoraApp(viewModel: AudMoraViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsState()

    MediaPlayerPlaybackEffect(
        audioResId = viewModel.selectedAudioRes,
        isPlaying = uiState.isPlaying,
        onPlaybackSecondsChanged = viewModel::updatePlaybackSeconds,
        onPlaybackCompleted = viewModel::completePlayback
    )

    DisposableEffect(uiState.isFullPlayerOpen, uiState.isShowcaseProfile, activity) {
        val controller = activity?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView)
        }

        if (uiState.isFullPlayerOpen) {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else if (uiState.isShowcaseProfile) {
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101014)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (uiState.currentScreen) {
                AudMoraScreen.Home -> HomeScreen(
                    tracks = uiState.tracks,
                    selectedTrack = uiState.selectedTrack,
                    onTrackClick = viewModel::selectTrackAndPlay
                )

                AudMoraScreen.ArtistProfile -> ArtistProfileScreen(
                    tracks = uiState.tracks,
                    showcaseMode = uiState.selectedTrack.usesShowcaseVisuals,
                    playbackSeconds = uiState.playbackSeconds,
                    onTrackClick = viewModel::selectTrackAndPlay,
                    onAddTrackClick = viewModel::openTrackStudio
                )

                AudMoraScreen.TrackStudio -> TrackStudioScreen(
                    track = uiState.selectedTrack,
                    initialConfig = uiState.selectedAtmosphereConfig,
                    onSave = viewModel::saveAtmosphere,
                    onClose = viewModel::closeTrackStudio
                )

                AudMoraScreen.Search -> SearchScreen(
                    tracks = uiState.tracks,
                    onTrackClick = viewModel::selectTrackAndPlay
                )

                AudMoraScreen.Library -> LibraryScreen(
                    tracks = uiState.tracks,
                    selectedTrack = uiState.selectedTrack,
                    onTrackClick = viewModel::selectTrackAndPlay
                )

                AudMoraScreen.UserProfile -> UserProfileScreen(
                    tracks = uiState.tracks,
                    onTrackClick = viewModel::selectTrackAndPlay
                )
            }

            if (uiState.showPersistentPlayer) {
                MiniPlayer(
                    atmosphereConfig = uiState.selectedAtmosphereConfig,
                    track = uiState.selectedTrack,
                    isPlaying = uiState.isPlaying,
                    playbackSeconds = uiState.playbackSeconds,
                    onPlayPauseClick = viewModel::togglePlay,
                    onOpenFullPlayer = viewModel::openFullPlayer,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(2f)
                )

                BottomNavigation(
                    currentScreen = uiState.currentScreen,
                    onScreenSelected = viewModel::selectScreen,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .zIndex(1f)
                )
            }

            AnimatedVisibility(
                visible = uiState.isFullPlayerOpen,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight }
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight }
                ) + fadeOut(),
                modifier = Modifier.zIndex(10f)
            ) {
                FullPlayer(
                    track = uiState.selectedTrack,
                    isPlaying = uiState.isPlaying,
                    atmosphereConfig = uiState.selectedAtmosphereConfig,
                    onPlayPauseClick = viewModel::togglePlay,
                    onClose = viewModel::closeFullPlayer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
