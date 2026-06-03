package com.opensound.app.state

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackId
import com.opensound.app.navigation.AudMoraScreen
import com.opensound.app.playback.PlaybackSeekRequest

data class AudMoraUiState(
    val playbackQueue: PlaybackQueue,
    val currentScreen: AudMoraScreen = AudMoraScreen.ArtistProfile,
    val atmosphereConfigs: Map<TrackId, AtmosphereConfig> = emptyMap(),
    val isPlaying: Boolean = false,
    val playbackSeconds: Float = 0f,
    val playbackSeekRequest: PlaybackSeekRequest? = null,
    val isFullPlayerOpen: Boolean = false
) {
    val tracks: List<Track>
        get() = playbackQueue.tracks

    val selectedTrack: Track
        get() = playbackQueue.currentTrack

    val selectedAtmosphereConfig: AtmosphereConfig
        get() = atmosphereConfigs[selectedTrack.id] ?: AtmosphereConfig()

    val canSkipToPreviousTrack: Boolean
        get() = playbackQueue.canSkipPrevious

    val canSkipToNextTrack: Boolean
        get() = playbackQueue.canSkipNext

    val showPersistentPlayer: Boolean
        get() = currentScreen != AudMoraScreen.TrackStudio

    val isShowcaseProfile: Boolean
        get() = selectedTrack.usesShowcaseVisuals && currentScreen == AudMoraScreen.ArtistProfile
}
