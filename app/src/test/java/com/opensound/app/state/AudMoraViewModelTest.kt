package com.opensound.app.state

import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.InMemoryTrackStudioDraftRepository
import com.opensound.app.data.ProfileRepository
import com.opensound.app.data.TrackFeedRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.data.UserLibraryRepository
import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.ArtistProfile
import com.opensound.app.models.ProfileMetric
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import com.opensound.app.models.UserLibrarySnapshot
import com.opensound.app.models.UserLibrarySummary
import com.opensound.app.models.UserProfile
import com.opensound.app.navigation.AudMoraScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudMoraViewModelTest {
    @Test
    fun initialState_opensShowcaseProfileReference() {
        val viewModel = AudMoraViewModel()
        val state = viewModel.uiState.value

        assertEquals(AudMoraScreen.ArtistProfile, state.currentScreen)
        assertTrue(state.selectedTrack.usesShowcaseVisuals)
        assertTrue(state.isShowcaseProfile)
    }

    @Test
    fun selectTrackAndPlay_startsPlaybackAndResetsPositionForNewTrack() {
        val viewModel = AudMoraViewModel()
        val nextTrack = viewModel.uiState.value.tracks.first { track ->
            !track.usesShowcaseVisuals
        }

        viewModel.updatePlaybackSeconds(12f)
        viewModel.selectTrackAndPlay(nextTrack)

        val state = viewModel.uiState.value
        assertEquals(nextTrack, state.selectedTrack)
        assertTrue(state.isPlaying)
        assertEquals(0f, state.playbackSeconds, 0.001f)
        assertFalse(state.isShowcaseProfile)
    }

    @Test
    fun saveAtmosphere_persistsSelectedTrackConfigAndReturnsToProfile() {
        val viewModel = AudMoraViewModel()
        val config = AtmosphereConfig(presetName = "Test preset")

        viewModel.selectScreen(AudMoraScreen.TrackStudio)
        viewModel.saveAtmosphere(config)

        val state = viewModel.uiState.value
        assertEquals(AudMoraScreen.ArtistProfile, state.currentScreen)
        assertEquals(config, state.selectedAtmosphereConfig)
    }

    @Test
    fun completePlayback_stopsAndRewindsUiState() {
        val track = testTrack("single-track")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository()
        )

        viewModel.togglePlay()
        viewModel.updatePlaybackSeconds(24f)
        viewModel.completePlayback()

        val state = viewModel.uiState.value
        assertFalse(state.isPlaying)
        assertEquals(0f, state.playbackSeconds, 0.001f)
    }

    @Test
    fun seekPlaybackTo_updatesProgressAndCreatesSeekRequest() {
        val viewModel = AudMoraViewModel()

        viewModel.seekPlaybackTo(18f)

        val state = viewModel.uiState.value
        assertEquals(18f, state.playbackSeconds, 0.001f)
        assertEquals(1L, state.playbackSeekRequest?.id)
        assertEquals(18f, state.playbackSeekRequest?.seconds ?: -1f, 0.001f)
    }

    @Test
    fun skipToNextAndPreviousTrack_usePlaybackQueue() {
        val firstTrack = testTrack("first-track")
        val secondTrack = testTrack("second-track")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(listOf(firstTrack, secondTrack)),
            atmosphereRepository = FakeAtmosphereRepository()
        )

        viewModel.togglePlay()
        viewModel.updatePlaybackSeconds(12f)
        viewModel.skipToNextTrack()

        val nextState = viewModel.uiState.value
        assertEquals(secondTrack, nextState.selectedTrack)
        assertTrue(nextState.isPlaying)
        assertEquals(0f, nextState.playbackSeconds, 0.001f)

        viewModel.skipToPreviousTrack()

        val previousState = viewModel.uiState.value
        assertEquals(firstTrack, previousState.selectedTrack)
        assertTrue(previousState.isPlaying)
        assertEquals(0f, previousState.playbackSeconds, 0.001f)
    }

    @Test
    fun playTrackFromQueueContext_keepsCatalogTracksAndUsesContextQueue() {
        val firstTrack = testTrack("first-track")
        val secondTrack = testTrack("second-track")
        val thirdTrack = testTrack("third-track")
        val catalogTracks = listOf(firstTrack, secondTrack, thirdTrack)
        val userProfileTracks = listOf(secondTrack, thirdTrack)
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(catalogTracks),
            atmosphereRepository = FakeAtmosphereRepository()
        )

        viewModel.playTrackFromQueueContext(
            track = thirdTrack,
            queueTracks = userProfileTracks,
            queueSource = PlaybackQueueSource.UserProfile
        )

        val state = viewModel.uiState.value
        assertEquals(catalogTracks, state.tracks)
        assertEquals(userProfileTracks, state.playbackQueue.tracks)
        assertEquals(PlaybackQueueSource.UserProfile, state.playbackQueueSource)
        assertEquals(thirdTrack, state.selectedTrack)
        assertTrue(state.isPlaying)
    }

    @Test
    fun initialState_usesInjectedFeedRepositoryForNonLibraryScreenLists() {
        val catalogTracks = listOf(
            testTrack("catalog-first"),
            testTrack("catalog-second"),
            testTrack("catalog-third")
        )
        val homeTracks = listOf(catalogTracks[1], catalogTracks[2])
        val userProfileTracks = listOf(catalogTracks[2])
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(catalogTracks),
            atmosphereRepository = FakeAtmosphereRepository(),
            trackFeedRepository = FakeTrackFeedRepository(
                homeTracks = homeTracks,
                artistProfileTracks = catalogTracks,
                searchTracks = listOf(catalogTracks[0]),
                libraryTracks = catalogTracks.reversed(),
                userProfileTracks = userProfileTracks
            )
        )

        val state = viewModel.uiState.value
        assertEquals(catalogTracks, state.tracks)
        assertEquals(homeTracks, state.homeTracks)
        assertEquals(listOf(catalogTracks[0]), state.searchTracks)
        assertEquals(userProfileTracks, state.userProfileTracks)
        assertEquals(catalogTracks.first(), state.selectedTrack)
    }

    @Test
    fun initialState_usesInjectedProfileAndLibraryRepositories() {
        val userProfile = UserProfile(
            displayName = "Injected Listener",
            handle = "@injected",
            metrics = listOf(ProfileMetric(value = "7", label = "Playlists"))
        )
        val artistProfile = ArtistProfile(
            displayName = "Injected Artist",
            genreLine = "Dream / Noise",
            bio = "Injected artist bio"
        )
        val librarySummary = UserLibrarySummary(
            description = "Injected library summary"
        )
        val track = testTrack("track")
        val userLibrary = UserLibrarySnapshot(
            summary = librarySummary,
            savedTrackIds = listOf(track.id)
        )
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(),
            profileRepository = FakeProfileRepository(
                userProfile = userProfile,
                artistProfile = artistProfile
            ),
            userLibraryRepository = FakeUserLibraryRepository(userLibrary)
        )

        val state = viewModel.uiState.value
        assertEquals(userProfile, state.currentUserProfile)
        assertEquals(artistProfile, state.featuredArtistProfile)
        assertEquals(librarySummary, state.userLibrarySummary)
        assertEquals(userLibrary, state.userLibrary)
        assertEquals(listOf(track), state.libraryTracks)
        assertTrue(state.selectedTrackIsSaved)
    }

    @Test
    fun toggleShuffleAndCycleRepeatMode_updatePlaybackQueueModes() {
        val viewModel = AudMoraViewModel()

        viewModel.toggleShuffle()
        viewModel.cycleRepeatMode()

        val state = viewModel.uiState.value
        assertTrue(state.shuffleEnabled)
        assertEquals(PlaybackRepeatMode.All, state.repeatMode)
    }

    @Test
    fun initialState_usesInjectedRepositoryContracts() {
        val track = Track(
            id = TrackId("fake-track"),
            title = "Injected Track",
            artist = "Repository Artist",
            audioSource = TrackAudioSource.LocalRawResource(7)
        )
        val config = AtmosphereConfig(presetName = "Injected Atmosphere")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(track.id, config)
        )

        val state = viewModel.uiState.value

        assertEquals(listOf(track), state.tracks)
        assertEquals(track, state.selectedTrack)
        assertEquals(config, state.selectedAtmosphereConfig)
        val mediaItem = viewModel.selectedPlaybackMediaItem
        assertEquals(track.id, mediaItem.id)
        assertEquals(track.title, mediaItem.title)
        assertEquals(track.artist, mediaItem.artist)
        assertEquals(track.durationSeconds, mediaItem.durationSeconds, 0.001f)
        assertEquals(TrackAudioSource.LocalRawResource(700), mediaItem.audioSource)
    }

    @Test
    fun selectedPlaybackMediaItem_changesTrackIdentityEvenWhenAudioSourceMatches() {
        val sharedAudioSource = TrackAudioSource.LocalRawResource(9)
        val firstTrack = Track(
            id = TrackId("first-track"),
            title = "First Track",
            artist = "Repository Artist",
            audioSource = sharedAudioSource
        )
        val secondTrack = Track(
            id = TrackId("second-track"),
            title = "Second Track",
            artist = "Repository Artist",
            audioSource = sharedAudioSource
        )
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(listOf(firstTrack, secondTrack)),
            atmosphereRepository = FakeAtmosphereRepository()
        )

        val initialMediaItem = viewModel.selectedPlaybackMediaItem
        viewModel.selectTrackAndPlay(secondTrack)
        val nextMediaItem = viewModel.selectedPlaybackMediaItem

        assertEquals(firstTrack.id, initialMediaItem.id)
        assertEquals(secondTrack.id, nextMediaItem.id)
        assertEquals(initialMediaItem.audioSource, nextMediaItem.audioSource)
    }

    @Test
    fun saveAtmosphere_writesSelectedTrackConfigToRepository() {
        val track = Track(
            id = TrackId("save-target"),
            title = "Save Target",
            artist = "Repository Artist",
            audioSource = TrackAudioSource.LocalRawResource(9)
        )
        val atmosphereRepository = FakeAtmosphereRepository()
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = atmosphereRepository
        )
        val config = AtmosphereConfig(presetName = "Saved Through Repository")

        viewModel.saveAtmosphere(config)

        assertEquals(config, atmosphereRepository.atmosphereConfigFor(track.id))
        assertEquals(config, viewModel.uiState.value.selectedAtmosphereConfig)
        assertEquals(AudMoraScreen.ArtistProfile, viewModel.uiState.value.currentScreen)
    }

    @Test
    fun openTrackStudio_initializesEditorStateWithSelectedTrackAtmosphere() {
        val track = testTrack("studio-track")
        val config = AtmosphereConfig(presetName = "Studio Seed")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(track.id, config)
        )

        viewModel.openTrackStudio()

        assertEquals(AudMoraScreen.TrackStudio, viewModel.uiState.value.currentScreen)
        assertEquals(config, viewModel.trackStudioEditorState.value.draftConfig)
    }

    @Test
    fun openTrackStudio_restoresDraftFromDraftRepository() {
        val track = testTrack("studio-draft-track")
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val draftConfig = AtmosphereConfig(presetName = "Restored Draft")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(track.id, savedConfig),
            trackStudioDraftRepository = InMemoryTrackStudioDraftRepository(
                initialDrafts = mapOf(track.id to draftConfig)
            )
        )

        viewModel.openTrackStudio()

        val editorState = viewModel.trackStudioEditorState.value
        assertEquals(savedConfig, editorState.savedConfig)
        assertEquals(draftConfig, editorState.draftConfig)
        assertTrue(editorState.isDirty)
    }

    @Test
    fun dispatchTrackStudioAction_autosavesDirtyDraft() {
        val track = testTrack("studio-autosave-track")
        val draftRepository = InMemoryTrackStudioDraftRepository()
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(),
            trackStudioDraftRepository = draftRepository
        )
        val draftConfig = AtmosphereConfig(presetName = "Autosaved Draft")

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(draftConfig)
        )

        assertEquals(
            viewModel.trackStudioEditorState.value.draftConfig,
            draftRepository.draftConfigFor(track.id)
        )
    }

    @Test
    fun dispatchTrackStudioAction_clearsDraftWhenResetToSaved() {
        val track = testTrack("studio-reset-track")
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val draftRepository = InMemoryTrackStudioDraftRepository()
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(track.id, savedConfig),
            trackStudioDraftRepository = draftRepository
        )

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Dirty Draft")
            )
        )
        viewModel.dispatchTrackStudioAction(TrackStudioEditorAction.DraftReset)

        assertEquals(savedConfig, viewModel.trackStudioEditorState.value.draftConfig)
        assertFalse(viewModel.trackStudioEditorState.value.isDirty)
        assertNull(draftRepository.draftConfigFor(track.id))
    }

    @Test
    fun saveTrackStudioAtmosphere_persistsEditorDraftAndReturnsToProfile() {
        val track = testTrack("studio-save-target")
        val atmosphereRepository = FakeAtmosphereRepository()
        val draftRepository = InMemoryTrackStudioDraftRepository()
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = atmosphereRepository,
            trackStudioDraftRepository = draftRepository
        )
        val draftConfig = AtmosphereConfig(presetName = "Studio Draft")

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(draftConfig)
        )
        val expectedSavedConfig = viewModel.trackStudioEditorState.value.draftConfig

        viewModel.saveTrackStudioAtmosphere()

        assertEquals(expectedSavedConfig, atmosphereRepository.atmosphereConfigFor(track.id))
        assertEquals(expectedSavedConfig, viewModel.uiState.value.selectedAtmosphereConfig)
        assertEquals(AudMoraScreen.ArtistProfile, viewModel.uiState.value.currentScreen)
        assertNull(draftRepository.draftConfigFor(track.id))
    }

    @Test
    fun closeTrackStudio_closesImmediatelyWhenDraftIsClean() {
        val viewModel = AudMoraViewModel()

        viewModel.openTrackStudio()
        viewModel.closeTrackStudio()

        assertEquals(AudMoraScreen.ArtistProfile, viewModel.uiState.value.currentScreen)
        assertFalse(viewModel.trackStudioEditorState.value.closeConfirmationVisible)
    }

    @Test
    fun closeTrackStudio_showsConfirmationWhenDraftIsDirty() {
        val viewModel = AudMoraViewModel()

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Dirty Draft")
            )
        )
        viewModel.closeTrackStudio()

        assertEquals(AudMoraScreen.TrackStudio, viewModel.uiState.value.currentScreen)
        assertTrue(viewModel.trackStudioEditorState.value.closeConfirmationVisible)
    }

    @Test
    fun discardTrackStudioChangesAndClose_restoresSavedDraftAndClosesEditor() {
        val track = testTrack("discard-track")
        val savedConfig = AtmosphereConfig(presetName = "Saved")
        val draftRepository = InMemoryTrackStudioDraftRepository()
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(track.id, savedConfig),
            trackStudioDraftRepository = draftRepository
        )

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Dirty Draft")
            )
        )
        viewModel.closeTrackStudio()
        viewModel.discardTrackStudioChangesAndClose()

        assertEquals(AudMoraScreen.ArtistProfile, viewModel.uiState.value.currentScreen)
        assertEquals(savedConfig, viewModel.trackStudioEditorState.value.draftConfig)
        assertFalse(viewModel.trackStudioEditorState.value.isDirty)
        assertNull(draftRepository.draftConfigFor(track.id))
    }

    @Test
    fun dismissTrackStudioCloseConfirmation_keepsEditorOpenAndDraftDirty() {
        val viewModel = AudMoraViewModel()

        viewModel.openTrackStudio()
        viewModel.dispatchTrackStudioAction(
            TrackStudioEditorAction.DraftConfigChanged(
                AtmosphereConfig(presetName = "Dirty Draft")
            )
        )
        viewModel.closeTrackStudio()
        viewModel.dismissTrackStudioCloseConfirmation()

        assertEquals(AudMoraScreen.TrackStudio, viewModel.uiState.value.currentScreen)
        assertFalse(viewModel.trackStudioEditorState.value.closeConfirmationVisible)
        assertTrue(viewModel.trackStudioEditorState.value.isDirty)
    }

    @Test
    fun saveTrackToLibrary_writesTrackIdAndRefreshesLibraryTracks() {
        val firstTrack = testTrack("first-track")
        val secondTrack = testTrack("second-track")
        val userLibraryRepository = FakeUserLibraryRepository(
            UserLibrarySnapshot(
                summary = UserLibrarySummary("Test library"),
                savedTrackIds = emptyList()
            )
        )
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(listOf(firstTrack, secondTrack)),
            atmosphereRepository = FakeAtmosphereRepository(),
            userLibraryRepository = userLibraryRepository
        )

        viewModel.saveTrackToLibrary(secondTrack)

        val state = viewModel.uiState.value
        assertEquals(listOf(secondTrack.id), state.savedTrackIds)
        assertEquals(listOf(secondTrack), state.libraryTracks)
        assertTrue(userLibraryRepository.isTrackSaved(secondTrack.id))
    }

    @Test
    fun removeTrackFromLibrary_removesTrackIdAndRefreshesLibraryTracks() {
        val firstTrack = testTrack("first-track")
        val secondTrack = testTrack("second-track")
        val userLibraryRepository = FakeUserLibraryRepository(
            UserLibrarySnapshot(
                summary = UserLibrarySummary("Test library"),
                savedTrackIds = listOf(firstTrack.id, secondTrack.id)
            )
        )
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(listOf(firstTrack, secondTrack)),
            atmosphereRepository = FakeAtmosphereRepository(),
            userLibraryRepository = userLibraryRepository
        )

        viewModel.removeTrackFromLibrary(firstTrack)

        val state = viewModel.uiState.value
        assertEquals(listOf(secondTrack.id), state.savedTrackIds)
        assertEquals(listOf(secondTrack), state.libraryTracks)
        assertFalse(userLibraryRepository.isTrackSaved(firstTrack.id))
    }

    @Test
    fun toggleTrackSaved_addsOrRemovesTrackId() {
        val track = testTrack("toggle-track")
        val viewModel = AudMoraViewModel(
            trackRepository = FakeTrackRepository(track),
            atmosphereRepository = FakeAtmosphereRepository(),
            userLibraryRepository = FakeUserLibraryRepository(
                UserLibrarySnapshot(
                    summary = UserLibrarySummary("Test library"),
                    savedTrackIds = emptyList()
                )
            )
        )

        viewModel.toggleTrackSaved(track)
        assertTrue(viewModel.uiState.value.selectedTrackIsSaved)
        assertEquals(listOf(track), viewModel.uiState.value.libraryTracks)

        viewModel.toggleTrackSaved(track)
        assertFalse(viewModel.uiState.value.selectedTrackIsSaved)
        assertTrue(viewModel.uiState.value.libraryTracks.isEmpty())
    }

    private class FakeTrackRepository(
        private val queuedTracks: List<Track>
    ) : TrackRepository {
        constructor(track: Track) : this(listOf(track))

        override fun tracks(): List<Track> {
            return queuedTracks
        }

        override fun audioSourceFor(track: Track): TrackAudioSource {
            val source = track.audioSource
            return when (source) {
                is TrackAudioSource.LocalRawResource -> TrackAudioSource.LocalRawResource(source.resId * 100)
            }
        }
    }

    private fun testTrack(id: String): Track {
        return Track(
            id = TrackId(id),
            title = id,
            artist = "Repository Artist",
            audioSource = TrackAudioSource.LocalRawResource(id.hashCode())
        )
    }

    private class FakeTrackFeedRepository(
        private val homeTracks: List<Track>,
        private val artistProfileTracks: List<Track>,
        private val searchTracks: List<Track>,
        private val libraryTracks: List<Track>,
        private val userProfileTracks: List<Track>
    ) : TrackFeedRepository {
        override fun homeTracks(): List<Track> {
            return homeTracks
        }

        override fun artistProfileTracks(): List<Track> {
            return artistProfileTracks
        }

        override fun searchTracks(): List<Track> {
            return searchTracks
        }

        override fun libraryTracks(): List<Track> {
            return libraryTracks
        }

        override fun userProfileTracks(): List<Track> {
            return userProfileTracks
        }
    }

    private class FakeProfileRepository(
        private val userProfile: UserProfile,
        private val artistProfile: ArtistProfile
    ) : ProfileRepository {
        override fun currentUserProfile(): UserProfile {
            return userProfile
        }

        override fun featuredArtistProfile(): ArtistProfile {
            return artistProfile
        }
    }

    private class FakeUserLibraryRepository(
        snapshot: UserLibrarySnapshot
    ) : UserLibraryRepository {
        private val summary = snapshot.summary
        private val savedTrackIds = snapshot.savedTrackIds.toMutableList()

        override fun librarySnapshot(): UserLibrarySnapshot {
            return UserLibrarySnapshot(
                summary = summary,
                savedTrackIds = savedTrackIds.toList()
            )
        }

        override fun saveTrack(trackId: TrackId) {
            if (!isTrackSaved(trackId)) {
                savedTrackIds.add(trackId)
            }
        }

        override fun removeSavedTrack(trackId: TrackId) {
            savedTrackIds.remove(trackId)
        }

        override fun isTrackSaved(trackId: TrackId): Boolean {
            return savedTrackIds.contains(trackId)
        }
    }

    private class FakeAtmosphereRepository(
        initialConfigs: Map<TrackId, AtmosphereConfig> = emptyMap()
    ) : AtmosphereRepository {
        constructor(
            trackId: TrackId,
            config: AtmosphereConfig
        ) : this(mapOf(trackId to config))

        private val configs = initialConfigs.toMutableMap()

        override fun atmosphereConfigs(): Map<TrackId, AtmosphereConfig> {
            return configs.toMap()
        }

        override fun atmosphereConfigFor(trackId: TrackId): AtmosphereConfig? {
            return configs[trackId]
        }

        override fun saveAtmosphereConfig(
            trackId: TrackId,
            config: AtmosphereConfig
        ) {
            configs[trackId] = config
        }
    }
}
