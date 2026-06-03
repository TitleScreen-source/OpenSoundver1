package com.opensound.app.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.AudMoraCatalogRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.navigation.AudMoraScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudMoraViewModel(
    private val trackRepository: TrackRepository,
    atmosphereRepository: AtmosphereRepository
) : ViewModel() {
    constructor(
        catalogRepository: AudMoraCatalogRepository = AudMoraCatalogRepository()
    ) : this(
        trackRepository = catalogRepository,
        atmosphereRepository = catalogRepository
    )

    private val _uiState = MutableStateFlow(
        initialState(
            trackRepository = trackRepository,
            atmosphereRepository = atmosphereRepository
        )
    )
    val uiState: StateFlow<AudMoraUiState> = _uiState.asStateFlow()

    val selectedAudioRes: Int
        get() = trackRepository.audioResFor(_uiState.value.selectedTrack)

    fun selectScreen(screen: AudMoraScreen) {
        _uiState.update { state ->
            state.copy(currentScreen = screen)
        }
    }

    fun openTrackStudio() {
        selectScreen(AudMoraScreen.TrackStudio)
    }

    fun closeTrackStudio() {
        selectScreen(AudMoraScreen.ArtistProfile)
    }

    fun saveAtmosphere(config: AtmosphereConfig) {
        _uiState.update { state ->
            state.copy(
                atmosphereConfigs = state.atmosphereConfigs + (state.selectedTrack.id to config),
                currentScreen = AudMoraScreen.ArtistProfile
            )
        }
    }

    fun selectTrackAndPlay(track: Track) {
        _uiState.update { state ->
            if (track == state.selectedTrack) {
                state.copy(isPlaying = true)
            } else {
                state.copy(
                    selectedTrack = track,
                    isPlaying = true,
                    playbackSeconds = 0f
                )
            }
        }
    }

    fun togglePlay() {
        _uiState.update { state ->
            state.copy(isPlaying = !state.isPlaying)
        }
    }

    fun updatePlaybackSeconds(seconds: Float) {
        _uiState.update { state ->
            state.copy(playbackSeconds = seconds.coerceAtLeast(0f))
        }
    }

    fun completePlayback() {
        _uiState.update { state ->
            state.copy(isPlaying = false, playbackSeconds = 0f)
        }
    }

    fun openFullPlayer() {
        _uiState.update { state ->
            state.copy(isFullPlayerOpen = true)
        }
    }

    fun closeFullPlayer() {
        _uiState.update { state ->
            state.copy(isFullPlayerOpen = false)
        }
    }

    companion object {
        fun factory(
            catalogRepository: AudMoraCatalogRepository = AudMoraCatalogRepository()
        ): ViewModelProvider.Factory {
            return factory(
                trackRepository = catalogRepository,
                atmosphereRepository = catalogRepository
            )
        }

        fun factory(
            trackRepository: TrackRepository,
            atmosphereRepository: AtmosphereRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AudMoraViewModel::class.java)) {
                        return AudMoraViewModel(
                            trackRepository = trackRepository,
                            atmosphereRepository = atmosphereRepository
                        ) as T
                    }

                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        private fun initialState(
            trackRepository: TrackRepository,
            atmosphereRepository: AtmosphereRepository
        ): AudMoraUiState {
            val tracks = trackRepository.tracks()

            return AudMoraUiState(
                tracks = tracks,
                selectedTrack = tracks.first(),
                atmosphereConfigs = atmosphereRepository.initialAtmosphereConfigs()
            )
        }
    }
}
