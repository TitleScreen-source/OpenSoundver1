package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TrackStudioEditorAction
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.editor.timelineLayersFor
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.Track

@Composable
fun TrackStudioScreen(
    track: Track,
    editorState: TrackStudioEditorState,
    onEditorAction: (TrackStudioEditorAction) -> Unit,
    onSave: () -> Unit,
    onDiscardChangesAndClose: () -> Unit,
    onDismissCloseConfirmation: () -> Unit,
    onClose: () -> Unit
) {
    val draftConfig = editorState.draftConfig
    val selectedSection = editorState.selectedSection
    val previewTimeSeconds = editorState.previewTimeSeconds
    val selectedLayerId = editorState.selectedLayerId
    val timelineLayers = timelineLayersFor(draftConfig)

    fun dispatch(action: TrackStudioEditorAction) {
        onEditorAction(action)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF08070D))
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            TrackStudioHeader(
                track = track,
                isDirty = editorState.isDirty,
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Live mini-player preview",
                color = Color(0xFFC8BED8)
            )

            Spacer(modifier = Modifier.height(14.dp))

            TrackStudioPreviewCard(
                track = track,
                draftConfig = draftConfig,
                previewTimeSeconds = previewTimeSeconds,
                activeDragLayer = selectedSection,
                onDragCharacter = { dx, dy ->
                    dispatch(
                        TrackStudioEditorAction.LayerDragged(
                            type = AtmosphereLayerType.Character,
                            dx = dx,
                            dy = dy
                        )
                    )
                },
                onDragText = { dx, dy ->
                    dispatch(
                        TrackStudioEditorAction.LayerDragged(
                            type = AtmosphereLayerType.Text,
                            dx = dx,
                            dy = dy
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            TimelinePanel(
                layers = timelineLayers,
                previewTimeSeconds = previewTimeSeconds,
                onPreviewTimeChange = {
                    dispatch(TrackStudioEditorAction.PreviewTimeChanged(it))
                },
                selectedLayerId = selectedLayerId,
                onLayerTimelineEdit = { updatedLayer ->
                    dispatch(TrackStudioEditorAction.TimelineLayerChanged(updatedLayer))
                },
                onAddLayer = { type ->
                    dispatch(TrackStudioEditorAction.LayerAdded(type))
                },
                onDuplicateLayer = {
                    dispatch(TrackStudioEditorAction.SelectedLayerDuplicated)
                },
                onDeleteLayer = {
                    dispatch(TrackStudioEditorAction.SelectedLayerDeleted)
                },
                onToggleLayerVisibility = { layer ->
                    dispatch(TrackStudioEditorAction.LayerVisibilityToggled(layer.id))
                },
                onLayerSelected = { layer ->
                    dispatch(TrackStudioEditorAction.LayerSelected(layer))
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            TrackStudioSectionTabs(
                selectedSection = selectedSection,
                onSectionSelected = {
                    dispatch(TrackStudioEditorAction.SectionSelected(it))
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            TrackStudioSectionHost(
                editorState = editorState,
                timelineLayers = timelineLayers,
                onEditorAction = { action ->
                    dispatch(action)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TrackStudioSaveBar(
                isDirty = editorState.isDirty,
                onReset = {
                    dispatch(TrackStudioEditorAction.DraftReset)
                },
                onSave = onSave
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (editorState.closeConfirmationVisible) {
            TrackStudioUnsavedChangesDialog(
                onKeepEditing = onDismissCloseConfirmation,
                onDiscard = onDiscardChangesAndClose
            )
        }
    }
}
