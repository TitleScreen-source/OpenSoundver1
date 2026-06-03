package com.opensound.app.data

import com.opensound.app.models.UserLibrarySummary

class SeedUserLibraryRepository : UserLibraryRepository {
    override fun librarySummary(): UserLibrarySummary {
        return UserLibrarySummary(
            description = "Треки, плейлисты и атмосферы"
        )
    }
}
