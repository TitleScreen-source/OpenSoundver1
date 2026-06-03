package com.opensound.app.data

import com.opensound.app.R
import com.opensound.app.models.TrackAudioSource
import com.opensound.app.models.TrackVisualMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AudMoraCatalogRepositoryTest {
    private val repository = AudMoraCatalogRepository()

    @Test
    fun tracks_includeShowcaseReferenceFirst() {
        val tracks = repository.tracks()

        assertEquals(AudMoraSeedTrackIds.RezeroShowcase, tracks.first().id)
        assertEquals(TrackVisualMode.ShowcaseReels, tracks.first().visualMode)
        assertTrue(tracks.first().usesShowcaseVisuals)
        assertEquals("I Feel Sick", tracks.first().title)
    }

    @Test
    fun audioSourceFor_usesTrackAudioMetadata() {
        val showcaseTrack = repository.tracks().first { track ->
            track.visualMode == TrackVisualMode.ShowcaseReels
        }
        val regularTrack = repository.tracks().first { track ->
            track.visualMode == TrackVisualMode.Atmosphere
        }

        assertEquals(
            TrackAudioSource.LocalRawResource(R.raw.rezero_showcase),
            repository.audioSourceFor(showcaseTrack)
        )
        assertEquals(35f, showcaseTrack.durationSeconds, 0.001f)
        assertEquals(
            TrackAudioSource.LocalRawResource(R.raw.track1),
            repository.audioSourceFor(regularTrack)
        )
        assertEquals(209f, regularTrack.durationSeconds, 0.001f)
    }

    @Test
    fun initialAtmosphereConfigs_useStableTrackIds() {
        val configTrackIds = repository.initialAtmosphereConfigs().keys

        assertTrue(AudMoraSeedTrackIds.NightDrive in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.LostSignal in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.EchoDreams in configTrackIds)
        assertTrue(AudMoraSeedTrackIds.MidnightCity in configTrackIds)
    }

    @Test
    fun tracks_havePositivePlaybackDurations() {
        assertTrue(repository.tracks().all { track -> track.durationSeconds > 0f })
    }

    @Test
    fun catalogRepository_implementsTrackContract() {
        val trackRepository: TrackRepository = repository

        assertTrue(trackRepository.tracks().isNotEmpty())
    }
}
