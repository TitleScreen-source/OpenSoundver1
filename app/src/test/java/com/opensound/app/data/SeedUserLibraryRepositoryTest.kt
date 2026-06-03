package com.opensound.app.data

import org.junit.Assert.assertTrue
import org.junit.Test

class SeedUserLibraryRepositoryTest {
    @Test
    fun librarySummary_containsDescription() {
        val summary = SeedUserLibraryRepository().librarySummary()

        assertTrue(summary.description.isNotBlank())
    }
}
