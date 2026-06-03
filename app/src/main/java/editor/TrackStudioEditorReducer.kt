package com.opensound.app.editor

import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayer
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.limitAtmosphereConfig

sealed class TrackStudioEditorAction {
    data class PreviewTimeChanged(val seconds: Float) : TrackStudioEditorAction()
    data class SectionSelected(val section: TrackStudioSection) : TrackStudioEditorAction()
    data class DraftConfigChanged(val config: AtmosphereConfig) : TrackStudioEditorAction()
    data class LayerDragged(
        val type: AtmosphereLayerType,
        val dx: Float,
        val dy: Float
    ) : TrackStudioEditorAction()

    data class TimelineLayerChanged(val layer: AtmosphereLayer) : TrackStudioEditorAction()
    data class LayerAdded(val type: AtmosphereLayerType) : TrackStudioEditorAction()
    data object SelectedLayerDuplicated : TrackStudioEditorAction()
    data object SelectedLayerDeleted : TrackStudioEditorAction()
    data class LayerVisibilityToggled(val layerId: String) : TrackStudioEditorAction()
    data class LayerSelected(val layer: AtmosphereLayer) : TrackStudioEditorAction()
    data object DraftReset : TrackStudioEditorAction()
}

fun reduceTrackStudioEditorState(
    state: TrackStudioEditorState,
    action: TrackStudioEditorAction
): TrackStudioEditorState {
    return when (action) {
        is TrackStudioEditorAction.PreviewTimeChanged -> state.copy(
            previewTimeSeconds = action.seconds
        )

        is TrackStudioEditorAction.SectionSelected -> state.copy(
            selectedSection = action.section
        )

        is TrackStudioEditorAction.DraftConfigChanged -> state.copy(
            draftConfig = limitAndSyncPrimaryLayers(action.config)
        )

        is TrackStudioEditorAction.LayerDragged -> state.copy(
            draftConfig = limitAndSyncPrimaryLayers(
                moveSelectedLayer(
                    config = state.draftConfig,
                    selectedLayerId = state.selectedLayerId,
                    type = action.type,
                    dx = action.dx,
                    dy = action.dy
                )
            )
        )

        is TrackStudioEditorAction.TimelineLayerChanged -> state.copy(
            draftConfig = limitAndSyncPrimaryLayers(
                updateTimelineLayer(
                    config = state.draftConfig,
                    updatedLayer = action.layer
                )
            )
        )

        is TrackStudioEditorAction.LayerAdded -> {
            val result = addTimelineLayer(
                config = state.draftConfig,
                type = action.type,
                playheadSeconds = state.previewTimeSeconds
            )

            state.copy(
                draftConfig = result.first,
                selectedLayerId = result.second,
                selectedSection = sectionForLayerType(action.type)
            )
        }

        TrackStudioEditorAction.SelectedLayerDuplicated -> {
            duplicateSelectedLayer(
                config = state.draftConfig,
                selectedLayerId = state.selectedLayerId
            )?.let { result ->
                state.copy(
                    draftConfig = result.first,
                    selectedLayerId = result.second,
                    selectedSection = TrackStudioSection.Timing
                )
            } ?: state
        }

        TrackStudioEditorAction.SelectedLayerDeleted -> {
            deleteSelectedLayer(
                config = state.draftConfig,
                selectedLayerId = state.selectedLayerId
            )?.let { result ->
                state.copy(
                    draftConfig = result.first,
                    selectedLayerId = result.second,
                    selectedSection = timelineLayersFor(result.first)
                        .firstOrNull { layer -> layer.id == result.second }
                        ?.let { layer -> sectionForLayer(layer) }
                        ?: TrackStudioSection.Timing
                )
            } ?: state
        }

        is TrackStudioEditorAction.LayerVisibilityToggled -> state.copy(
            draftConfig = syncPrimaryLayers(
                toggleLayerVisibility(
                    config = state.draftConfig,
                    layerId = action.layerId
                )
            )
        )

        is TrackStudioEditorAction.LayerSelected -> state.copy(
            selectedLayerId = action.layer.id,
            selectedSection = sectionForLayer(action.layer)
        )

        TrackStudioEditorAction.DraftReset -> state.copy(
            draftConfig = AtmosphereConfig()
        )
    }
}

private fun limitAndSyncPrimaryLayers(config: AtmosphereConfig): AtmosphereConfig {
    return syncPrimaryLayers(limitAtmosphereConfig(config))
}
