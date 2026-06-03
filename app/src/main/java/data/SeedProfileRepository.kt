package com.opensound.app.data

import com.opensound.app.models.ArtistProfile
import com.opensound.app.models.ProfileMetric
import com.opensound.app.models.UserProfile

class SeedProfileRepository : ProfileRepository {
    override fun currentUserProfile(): UserProfile {
        return UserProfile(
            displayName = "Open Listener",
            handle = "@audmora",
            metrics = listOf(
                ProfileMetric(value = "23", label = "Плейлисты"),
                ProfileMetric(value = "156", label = "Подписки"),
                ProfileMetric(value = "2.4K", label = "Прослушивания")
            )
        )
    }

    override fun featuredArtistProfile(): ArtistProfile {
        return ArtistProfile(
            displayName = "Synth Waves",
            genreLine = "Electronic • Ambient • Indie",
            bio = "Автор создаёт атмосферную электронную музыку с визуальными сценами для каждого релиза."
        )
    }
}
