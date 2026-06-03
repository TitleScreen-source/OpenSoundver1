package com.opensound.app.state

import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackId
import com.opensound.app.navigation.AudMoraScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(
            TrackAudioSource.LocalRawResource(700),
            viewModel.selectedAudioSource
        )
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
