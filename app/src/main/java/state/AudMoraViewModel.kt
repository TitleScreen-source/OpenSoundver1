package com.opensound.app.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.AudMoraCatalogRepository
import com.opensound.app.data.InMemoryAtmosphereRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.navigation.AudMoraScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudMoraViewModel(
    private val trackRepository: TrackRepository,
    private val atmosphereRepository: AtmosphereRepository
) : ViewModel() {
    constructor(
        catalogRepository: AudMoraCatalogRepository = AudMoraCatalogRepository()
    ) : this(
        trackRepository = catalogRepository,
        atmosphereRepository = InMemoryAtmosphereRepository(
            initialConfigs = catalogRepository.initialAtmosphereConfigs()
        )
    )

    private val _uiState = MutableStateFlow(
        initialState(
            trackRepository = trackRepository,
            atmosphereRepository = atmosphereRepository
        )
    )
    val uiState: StateFlow<AudMoraUiState> = _uiState.asStateFlow()

    val selectedAudioSource: TrackAudioSource
        get() = trackRepository.audioSourceFor(_uiState.value.selectedTrack)

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
        val selectedTrackId = _uiState.value.selectedTrack.id
        atmosphereRepository.saveAtmosphereConfig(
            trackId = selectedTrackId,
            config = config
        )

        _uiState.update { state ->
            state.copy(
                atmosphereConfigs = atmosphereRepository.atmosphereConfigs(),
                currentScreen = AudMoraScreen.ArtistProfile
            )
        }
    }

    fun selectTrackAndPlay(track: Track) {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.TrackSelectedForPlayback(track)
            )
        }
    }

    fun togglePlay() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackToggled
            )
        }
    }

    fun updatePlaybackSeconds(seconds: Float) {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackProgressChanged(seconds)
            )
        }
    }

    fun seekPlaybackTo(seconds: Float) {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackSeekRequested(seconds)
            )
        }
    }

    fun skipToPreviousTrack() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackPreviousRequested
            )
        }
    }

    fun skipToNextTrack() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackNextRequested
            )
        }
    }

    fun completePlayback() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackCompleted
            )
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
                atmosphereRepository = InMemoryAtmosphereRepository(
                    initialConfigs = catalogRepository.initialAtmosphereConfigs()
                )
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
                playbackQueue = PlaybackQueue(tracks = tracks),
                atmosphereConfigs = atmosphereRepository.atmosphereConfigs()
            )
        }
    }
}
