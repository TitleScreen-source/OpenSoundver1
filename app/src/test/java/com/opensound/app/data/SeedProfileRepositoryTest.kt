package com.opensound.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedProfileRepositoryTest {
    private val repository = SeedProfileRepository()

    @Test
    fun currentUserProfile_containsSeedIdentityAndMetrics() {
        val profile = repository.currentUserProfile()

        assertEquals("Open Listener", profile.displayName)
        assertEquals("@audmora", profile.handle)
        assertTrue(profile.metrics.isNotEmpty())
    }

    @Test
    fun featuredArtistProfile_containsSeedArtistCopy() {
        val profile = repository.featuredArtistProfile()

        assertEquals("Synth Waves", profile.displayName)
        assertTrue(profile.genreLine.isNotBlank())
        assertTrue(profile.bio.isNotBlank())
    }
}
