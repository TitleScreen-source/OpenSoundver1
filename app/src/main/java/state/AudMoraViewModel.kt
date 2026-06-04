package com.opensound.app.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.AudMoraCatalogRepository
import com.opensound.app.data.InMemoryAtmosphereRepository
import com.opensound.app.data.InMemoryTrackStudioDraftRepository
import com.opensound.app.data.ProfileRepository
import com.opensound.app.data.SeedTrackFeedRepository
import com.opensound.app.data.SeedProfileRepository
import com.opensound.app.data.SeedUserLibraryRepository
import com.opensound.app.data.TrackFeedRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.data.TrackStudioDraftRepository
import com.opensound.app.data.UserLibraryRepository
import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.editor.TrackStudioSessionStateHolder
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.UserLibrarySnapshot
import com.opensound.app.navigation.AudMoraScreen
import com.opensound.app.playback.PlaybackEvent
import com.opensound.app.playback.PlaybackMediaItem
import com.opensound.app.playback.toPlaybackMediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AudMoraViewModel(
    private val trackRepository: TrackRepository,
    private val atmosphereRepository: AtmosphereRepository,
    private val trackFeedRepository: TrackFeedRepository = SeedTrackFeedRepository(trackRepository),
    private val profileRepository: ProfileRepository = SeedProfileRepository(),
    private val userLibraryRepository: UserLibraryRepository = SeedUserLibraryRepository(),
    private val trackStudioDraftRepository: TrackStudioDraftRepository = InMemoryTrackStudioDraftRepository()
) : ViewModel() {
    constructor(
        catalogRepository: AudMoraCatalogRepository = AudMoraCatalogRepository()
    ) : this(
        trackRepository = catalogRepository,
        atmosphereRepository = InMemoryAtmosphereRepository(
            initialConfigs = catalogRepository.initialAtmosphereConfigs()
        ),
        trackFeedRepository = SeedTrackFeedRepository(catalogRepository),
        profileRepository = SeedProfileRepository(),
        userLibraryRepository = SeedUserLibraryRepository(),
        trackStudioDraftRepository = InMemoryTrackStudioDraftRepository()
    )

    private val _uiState = MutableStateFlow(
        initialState(
            trackRepository = trackRepository,
            atmosphereRepository = atmosphereRepository,
            trackFeedRepository = trackFeedRepository,
            profileRepository = profileRepository,
            userLibraryRepository = userLibraryRepository
        )
    )
    val uiState: StateFlow<AudMoraUiState> = _uiState.asStateFlow()

    private val trackStudioStateHolder = TrackStudioSessionStateHolder()
    val trackStudioEditorState: StateFlow<TrackStudioEditorState> = trackStudioStateHolder.state

    val selectedPlaybackMediaItem: PlaybackMediaItem
        get() {
            val selectedTrack = _uiState.value.selectedTrack

            return selectedTrack.toPlaybackMediaItem(
                audioSource = trackRepository.audioSourceFor(selectedTrack)
            )
        }

    fun selectScreen(screen: AudMoraScreen) {
        _uiState.update { state ->
            state.copy(currentScreen = screen)
        }
    }

    fun openTrackStudio() {
        val state = _uiState.value
        val savedConfig = state.selectedAtmosphereConfig
        val draftConfig = trackStudioDraftRepository.draftConfigFor(state.selectedTrack.id)
            ?: savedConfig

        trackStudioStateHolder.startEditing(
            trackId = state.selectedTrack.id,
            savedConfig = savedConfig,
            draftConfig = draftConfig
        )
        selectScreen(AudMoraScreen.TrackStudio)
    }

    fun closeTrackStudio() {
        if (trackStudioStateHolder.requestClose()) {
            selectScreen(AudMoraScreen.ArtistProfile)
        }
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

    fun dispatchTrackStudioAction(action: TrackStudioEditorAction) {
        trackStudioStateHolder.dispatch(action)
        syncTrackStudioDraft()
    }

    fun saveTrackStudioAtmosphere() {
        val savedConfig = trackStudioStateHolder.saveConfig()
        trackStudioStateHolder.markSaved(savedConfig)
        trackStudioStateHolder.currentTrackId?.let { trackId ->
            trackStudioDraftRepository.clearDraftConfig(trackId)
        }
        saveAtmosphere(savedConfig)
    }

    fun discardTrackStudioChangesAndClose() {
        trackStudioStateHolder.currentTrackId?.let { trackId ->
            trackStudioDraftRepository.clearDraftConfig(trackId)
        }
        trackStudioStateHolder.discardChanges()
        selectScreen(AudMoraScreen.ArtistProfile)
    }

    fun dismissTrackStudioCloseConfirmation() {
        trackStudioStateHolder.dismissCloseConfirmation()
    }

    fun saveTrackToLibrary(track: Track) {
        userLibraryRepository.saveTrack(track.id)
        refreshUserLibraryState()
    }

    fun removeTrackFromLibrary(track: Track) {
        userLibraryRepository.removeSavedTrack(track.id)
        refreshUserLibraryState()
    }

    fun toggleTrackSaved(track: Track) {
        if (userLibraryRepository.isTrackSaved(track.id)) {
            userLibraryRepository.removeSavedTrack(track.id)
        } else {
            userLibraryRepository.saveTrack(track.id)
        }

        refreshUserLibraryState()
    }

    fun selectTrackAndPlay(track: Track) {
        playTrackFromQueueContext(
            track = track,
            queueTracks = _uiState.value.tracks,
            queueSource = PlaybackQueueSource.Catalog
        )
    }

    fun playTrackFromQueueContext(
        track: Track,
        queueTracks: List<Track>,
        queueSource: PlaybackQueueSource
    ) {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.TrackSelectedForPlayback(
                    track = track,
                    queueTracks = queueTracks,
                    queueSource = queueSource
                )
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

    fun toggleShuffle() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackShuffleToggled
            )
        }
    }

    fun cycleRepeatMode() {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackRepeatModeCycled
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

    fun handlePlaybackEvent(event: PlaybackEvent) {
        _uiState.update { state ->
            reduceAudMoraPlaybackState(
                state = state,
                action = AudMoraPlaybackAction.PlaybackEventReceived(event)
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

    private fun refreshUserLibraryState() {
        val userLibrary = userLibraryRepository.librarySnapshot()

        _uiState.update { state ->
            state.copy(
                userLibrary = userLibrary,
                libraryTracks = libraryTracksFor(
                    tracks = state.tracks,
                    userLibrary = userLibrary
                )
            )
        }
    }

    private fun syncTrackStudioDraft() {
        val trackId = trackStudioStateHolder.currentTrackId ?: return
        val editorState = trackStudioStateHolder.state.value

        if (editorState.isDirty) {
            trackStudioDraftRepository.saveDraftConfig(
                trackId = trackId,
                config = editorState.draftConfig
            )
        } else {
            trackStudioDraftRepository.clearDraftConfig(trackId)
        }
    }

    companion object {
        fun factory(
            catalogRepository: AudMoraCatalogRepository = AudMoraCatalogRepository(),
            atmosphereRepository: AtmosphereRepository = InMemoryAtmosphereRepository(
                initialConfigs = catalogRepository.initialAtmosphereConfigs()
            ),
            profileRepository: ProfileRepository = SeedProfileRepository(),
            userLibraryRepository: UserLibraryRepository = SeedUserLibraryRepository(),
            trackStudioDraftRepository: TrackStudioDraftRepository = InMemoryTrackStudioDraftRepository()
        ): ViewModelProvider.Factory {
            return factory(
                trackRepository = catalogRepository,
                atmosphereRepository = atmosphereRepository,
                trackFeedRepository = SeedTrackFeedRepository(catalogRepository),
                profileRepository = profileRepository,
                userLibraryRepository = userLibraryRepository,
                trackStudioDraftRepository = trackStudioDraftRepository
            )
        }

        fun factory(
            trackRepository: TrackRepository,
            atmosphereRepository: AtmosphereRepository,
            trackFeedRepository: TrackFeedRepository = SeedTrackFeedRepository(trackRepository),
            profileRepository: ProfileRepository = SeedProfileRepository(),
            userLibraryRepository: UserLibraryRepository = SeedUserLibraryRepository(),
            trackStudioDraftRepository: TrackStudioDraftRepository = InMemoryTrackStudioDraftRepository()
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AudMoraViewModel::class.java)) {
                        return AudMoraViewModel(
                            trackRepository = trackRepository,
                            atmosphereRepository = atmosphereRepository,
                            trackFeedRepository = trackFeedRepository,
                            profileRepository = profileRepository,
                            userLibraryRepository = userLibraryRepository,
                            trackStudioDraftRepository = trackStudioDraftRepository
                        ) as T
                    }

                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        private fun initialState(
            trackRepository: TrackRepository,
            atmosphereRepository: AtmosphereRepository,
            trackFeedRepository: TrackFeedRepository,
            profileRepository: ProfileRepository,
            userLibraryRepository: UserLibraryRepository
        ): AudMoraUiState {
            val tracks = trackRepository.tracks()
            val userLibrary = userLibraryRepository.librarySnapshot()

            return AudMoraUiState(
                tracks = tracks,
                homeTracks = trackFeedRepository.homeTracks(),
                artistProfileTracks = trackFeedRepository.artistProfileTracks(),
                searchTracks = trackFeedRepository.searchTracks(),
                libraryTracks = libraryTracksFor(
                    tracks = tracks,
                    userLibrary = userLibrary
                ),
                userProfileTracks = trackFeedRepository.userProfileTracks(),
                currentUserProfile = profileRepository.currentUserProfile(),
                featuredArtistProfile = profileRepository.featuredArtistProfile(),
                userLibrary = userLibrary,
                playbackQueue = PlaybackQueue(tracks = tracks),
                atmosphereConfigs = atmosphereRepository.atmosphereConfigs()
            )
        }

        private fun libraryTracksFor(
            tracks: List<Track>,
            userLibrary: UserLibrarySnapshot
        ): List<Track> {
            val tracksById = tracks.associateBy { track -> track.id }

            return userLibrary.savedTrackIds.mapNotNull { trackId ->
                tracksById[trackId]
            }
        }
    }
}
