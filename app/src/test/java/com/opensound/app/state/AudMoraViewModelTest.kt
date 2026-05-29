package com.opensound.app.state

import com.opensound.app.models.AtmosphereConfig
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
}
