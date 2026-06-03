package com.opensound.app.data

import com.opensound.app.models.UserLibrarySummary

object UserLibrarySeedData {
    val summary = UserLibrarySummary(
        description = "Треки, плейлисты и атмосферы"
    )

    val defaultSavedTrackIds = listOf(
        AudMoraSeedTrackIds.NightDrive,
        AudMoraSeedTrackIds.LostSignal,
        AudMoraSeedTrackIds.EchoDreams
    )
}
