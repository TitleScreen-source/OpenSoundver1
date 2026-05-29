package com.opensound.app.data

import com.opensound.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudMoraCatalogRepositoryTest {
    private val repository = AudMoraCatalogRepository()

    @Test
    fun tracks_includeShowcaseReferenceFirst() {
        val tracks = repository.tracks()

        assertTrue(tracks.first().isShowcase)
        assertEquals("I Feel Sick", tracks.first().title)
    }

    @Test
    fun audioResFor_usesShowcaseAudioOnlyForShowcaseTracks() {
        val showcaseTrack = repository.tracks().first { track -> track.isShowcase }
        val regularTrack = repository.tracks().first { track -> !track.isShowcase }

        assertEquals(R.raw.rezero_showcase, repository.audioResFor(showcaseTrack))
        assertEquals(R.raw.track1, repository.audioResFor(regularTrack))
        assertFalse(regularTrack.isShowcase)
    }

    @Test
    fun initialAtmosphereConfigs_coverRegularSeedTracks() {
        val configTitles = repository.initialAtmosphereConfigs().keys

        assertTrue("Night Drive" in configTitles)
        assertTrue("Lost Signal" in configTitles)
        assertTrue("Echo Dreams" in configTitles)
        assertTrue("Midnight City" in configTitles)
    }
}
