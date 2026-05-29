package com.opensound.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AudMoraScreenTest {
    @Test
    fun fromRoute_mapsKnownRoutes() {
        assertEquals(AudMoraScreen.Home, AudMoraScreen.fromRoute("home"))
        assertEquals(AudMoraScreen.Search, AudMoraScreen.fromRoute("search"))
        assertEquals(AudMoraScreen.Library, AudMoraScreen.fromRoute("library"))
        assertEquals(AudMoraScreen.TrackStudio, AudMoraScreen.fromRoute("studio"))
    }

    @Test
    fun fromRoute_fallsBackToArtistProfile() {
        assertEquals(AudMoraScreen.ArtistProfile, AudMoraScreen.fromRoute("unknown"))
    }
}
