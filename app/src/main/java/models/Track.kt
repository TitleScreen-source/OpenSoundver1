package com.opensound.app.models

data class TrackId(
    val value: String
)

enum class TrackVisualMode {
    Atmosphere,
    ShowcaseReels
}

data class Track(
    val id: TrackId,
    val title: String,
    val artist: String,
    val audioResId: Int,
    val visualMode: TrackVisualMode = TrackVisualMode.Atmosphere
) {
    val usesShowcaseVisuals: Boolean
        get() = visualMode == TrackVisualMode.ShowcaseReels
}


