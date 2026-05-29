package com.opensound.app.state

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackId
import com.opensound.app.navigation.AudMoraScreen

data class AudMoraUiState(
    val tracks: List<Track>,
    val selectedTrack: Track,
    val currentScreen: AudMoraScreen = AudMoraScreen.ArtistProfile,
    val atmosphereConfigs: Map<TrackId, AtmosphereConfig> = emptyMap(),
    val isPlaying: Boolean = false,
    val playbackSeconds: Float = 0f,
    val isFullPlayerOpen: Boolean = false
) {
    val selectedAtmosphereConfig: AtmosphereConfig
        get() = atmosphereConfigs[selectedTrack.id] ?: AtmosphereConfig()

    val showPersistentPlayer: Boolean
        get() = currentScreen != AudMoraScreen.TrackStudio

    val isShowcaseProfile: Boolean
        get() = selectedTrack.usesShowcaseVisuals && currentScreen == AudMoraScreen.ArtistProfile
}
