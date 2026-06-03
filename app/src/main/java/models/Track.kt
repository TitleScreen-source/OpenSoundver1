package com.opensound.app.models

data class TrackId(
    val value: String
)

enum class TrackVisualMode {
    Atmosphere,
    ShowcaseReels
}

sealed class TrackAudioSource {
    data class LocalRawResource(val resId: Int) : TrackAudioSource()
}

data class Track(
    val id: TrackId,
    val title: String,
    val artist: String,
    val audioSource: TrackAudioSource,
    val durationSeconds: Float = 100f,
    val visualMode: TrackVisualMode = TrackVisualMode.Atmosphere
) {
    val usesShowcaseVisuals: Boolean
        get() = visualMode == TrackVisualMode.ShowcaseReels
}


