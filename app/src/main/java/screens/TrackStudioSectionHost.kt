package com.opensound.app.screens

import androidx.compose.runtime.Composable
import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.editor.TrackStudioSection
import com.opensound.app.models.AtmosphereLayer

@Composable
internal fun TrackStudioSectionHost(
    editorState: TrackStudioEditorState,
    timelineLayers: List<AtmosphereLayer>,
    onEditorAction: (TrackStudioEditorAction) -> Unit
) {
    val draftConfig = editorState.draftConfig
    val selectedLayerId = editorState.selectedLayerId

    when (editorState.selectedSection) {
        TrackStudioSection.Scene -> SceneSection(
            draftConfig = draftConfig,
            onConfigChange = {
                onEditorAction(TrackStudioEditorAction.DraftConfigChanged(it))
            }
        )

        TrackStudioSection.Character -> CharacterSection(
            draftConfig = draftConfig,
            selectedLayerId = selectedLayerId,
            onConfigChange = {
                onEditorAction(TrackStudioEditorAction.DraftConfigChanged(it))
            }
        )

        TrackStudioSection.Text -> TextSection(
            draftConfig = draftConfig,
            selectedLayerId = selectedLayerId,
            onConfigChange = {
                onEditorAction(TrackStudioEditorAction.DraftConfigChanged(it))
            }
        )

        TrackStudioSection.Timing -> TimingSection(
            draftConfig = draftConfig,
            selectedLayer = timelineLayers.firstOrNull { layer -> layer.id == selectedLayerId },
            onLayerChange = { updatedLayer ->
                onEditorAction(TrackStudioEditorAction.TimelineLayerChanged(updatedLayer))
            }
        )

        TrackStudioSection.Assets -> AssetsSection(
            draftConfig = draftConfig
        )
    }
}
