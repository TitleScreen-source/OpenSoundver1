package com.opensound.app.editor

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType

object TrackStudioLayerIds {
    const val CHARACTER_MAIN_LAYER_ID = "character-main"
    const val TEXT_MAIN_LAYER_ID = "text-main"
    const val EFFECT_MAIN_LAYER_ID = "effect-glow"
    const val BACKGROUND_MAIN_LAYER_ID = "background-main"
    const val WAVE_MAIN_LAYER_ID = "wave-main"
}

enum class TrackStudioSection(
    val label: String,
    val editorDragMode: String? = null
) {
    Scene(label = "Scene"),
    Character(label = "Character", editorDragMode = "Character"),
    Text(label = "Text", editorDragMode = "Text"),
    Timing(label = "Timing"),
    Assets(label = "Assets");

    companion object {
        val ordered: List<TrackStudioSection> = listOf(
            Scene,
            Character,
            Text,
            Timing,
            Assets
        )

        fun fromLabel(label: String): TrackStudioSection {
            return ordered.firstOrNull { section -> section.label == label } ?: Scene
        }
    }
}

data class TrackStudioEditorState(
    val draftConfig: AtmosphereConfig,
    val savedConfig: AtmosphereConfig = draftConfig,
    val selectedSection: TrackStudioSection = TrackStudioSection.Scene,
    val previewTimeSeconds: Float = 24f,
    val selectedLayerId: String = TrackStudioLayerIds.TEXT_MAIN_LAYER_ID,
    val closeConfirmationVisible: Boolean = false
) {
    val isDirty: Boolean
        get() = draftConfig != savedConfig
}

fun sectionForLayer(layer: AtmosphereLayer): TrackStudioSection {
    return sectionForLayerType(layer.type)
}

fun sectionForLayerType(type: AtmosphereLayerType): TrackStudioSection {
    return when (type) {
        AtmosphereLayerType.Character -> TrackStudioSection.Character
        AtmosphereLayerType.Text -> TrackStudioSection.Text
        AtmosphereLayerType.Effect -> TrackStudioSection.Scene
        AtmosphereLayerType.Background -> TrackStudioSection.Assets
        AtmosphereLayerType.Wave -> TrackStudioSection.Timing
    }
}
