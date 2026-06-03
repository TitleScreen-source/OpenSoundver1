package com.opensound.app.state

import com.opensound.app.data.AtmosphereRepository
import com.opensound.app.data.TrackRepository
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
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
        val viewModel = AudMoraViewModel()

        viewModel.togglePlay()
        viewModel.updatePlaybackSeconds(24f)
        viewModel.completePlayback()

        val state = viewModel.uiState.value
        assertFalse(state.isPlaying)
        assertEquals(0f, state.playbackSeconds, 0.001f)
    }

    @Test
    fun initialState_usesInjectedRepositoryContracts() {
        val track = Track(
            id = TrackId("fake-track"),
            title = "Injected Track",
            artist = "Repository Artist",
            audioResId = 7
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
        assertEquals(700, viewModel.selectedAudioRes)
    }

    @Test
    fun saveAtmosphere_writesSelectedTrackConfigToRepository() {
        val track = Track(
            id = TrackId("save-target"),
            title = "Save Target",
            artist = "Repository Artist",
            audioResId = 9
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
        private val track: Track
    ) : TrackRepository {
        override fun tracks(): List<Track> {
            return listOf(track)
        }

        override fun audioResFor(track: Track): Int {
            return track.audioResId * 100
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
