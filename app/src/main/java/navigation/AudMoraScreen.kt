package com.opensound.app.navigation

enum class AudMoraScreen(val route: String) {
    Home("home"),
    Search("search"),
    Library("library"),
    ArtistProfile("profile"),
    TrackStudio("studio"),
    UserProfile("userProfile");

    companion object {
        fun fromRoute(route: String): AudMoraScreen {
            return entries.firstOrNull { screen -> screen.route == route } ?: ArtistProfile
        }
    }
}
