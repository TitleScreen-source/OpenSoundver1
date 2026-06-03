package com.opensound.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TrackStudioEditorState
import com.opensound.app.editor.TrackStudioSection
import com.opensound.app.editor.addTimelineLayer
import com.opensound.app.editor.deleteSelectedLayer
import com.opensound.app.editor.duplicateSelectedLayer
import com.opensound.app.editor.moveSelectedLayer
import com.opensound.app.editor.sectionForLayer
import com.opensound.app.editor.sectionForLayerType
import com.opensound.app.editor.syncPrimaryLayers
import com.opensound.app.editor.timelineLayersFor
import com.opensound.app.editor.toggleLayerVisibility
import com.opensound.app.editor.updateTimelineLayer
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.AtmosphereLayerType
import com.opensound.app.models.Track
import com.opensound.app.models.limitAtmosphereConfig
import com.opensound.app.player.AtmosphereMiniPlayerContent

@Composable
fun TrackStudioScreen(
    track: Track,
    initialConfig: AtmosphereConfig,
    onSave: (AtmosphereConfig) -> Unit,
    onClose: () -> Unit
) {
    var editorState by remember(track.id) {
        mutableStateOf(TrackStudioEditorState(draftConfig = initialConfig))
    }
    val draftConfig = editorState.draftConfig
    val selectedSection = editorState.selectedSection
    val previewTimeSeconds = editorState.previewTimeSeconds
    val selectedLayerId = editorState.selectedLayerId

    fun updateDraftConfig(config: AtmosphereConfig) {
        editorState = editorState.copy(draftConfig = config)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF08070D))
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        StudioHeader(track = track, onClose = onClose)

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Live mini-player preview",
            color = Color(0xFFC8BED8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PreviewCard(
            track = track,
            draftConfig = draftConfig,
            previewTimeSeconds = previewTimeSeconds,
            activeDragLayer = selectedSection,
            onDragCharacter = { dx, dy ->
                updateDraftConfig(
                    limitAtmosphereConfig(
                        moveSelectedLayer(
                            config = draftConfig,
                            selectedLayerId = selectedLayerId,
                            type = AtmosphereLayerType.Character,
                            dx = dx,
                            dy = dy
                        )
                    )
                )
            },
            onDragText = { dx, dy ->
                updateDraftConfig(
                    limitAtmosphereConfig(
                        moveSelectedLayer(
                            config = draftConfig,
                            selectedLayerId = selectedLayerId,
                            type = AtmosphereLayerType.Text,
                            dx = dx,
                            dy = dy
                        )
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        TimelinePanel(
            layers = timelineLayersFor(draftConfig),
            previewTimeSeconds = previewTimeSeconds,
            onPreviewTimeChange = { editorState = editorState.copy(previewTimeSeconds = it) },
            selectedLayerId = selectedLayerId,
            onLayerTimelineEdit = { updatedLayer ->
                updateDraftConfig(
                    limitAtmosphereConfig(
                        updateTimelineLayer(
                            config = draftConfig,
                            updatedLayer = updatedLayer
                        )
                    )
                )
            },
            onAddLayer = { type ->
                addTimelineLayer(
                    config = draftConfig,
                    type = type,
                    playheadSeconds = previewTimeSeconds
                ).let { result ->
                    editorState = editorState.copy(
                        draftConfig = result.first,
                        selectedLayerId = result.second,
                        selectedSection = sectionForLayerType(type)
                    )
                }
            },
            onDuplicateLayer = {
                duplicateSelectedLayer(
                    config = draftConfig,
                    selectedLayerId = selectedLayerId
                )?.let { result ->
                    editorState = editorState.copy(
                        draftConfig = result.first,
                        selectedLayerId = result.second,
                        selectedSection = TrackStudioSection.Timing
                    )
                }
            },
            onDeleteLayer = {
                deleteSelectedLayer(
                    config = draftConfig,
                    selectedLayerId = selectedLayerId
                )?.let { result ->
                    editorState = editorState.copy(
                        draftConfig = result.first,
                        selectedLayerId = result.second,
                        selectedSection = timelineLayersFor(result.first)
                            .firstOrNull { it.id == result.second }
                            ?.let { sectionForLayer(it) }
                            ?: TrackStudioSection.Timing
                    )
                }
            },
            onToggleLayerVisibility = { layer ->
                updateDraftConfig(
                    syncPrimaryLayers(
                        toggleLayerVisibility(
                            config = draftConfig,
                            layerId = layer.id
                        )
                    )
                )
            },
            onLayerSelected = { layer ->
                editorState = editorState.copy(
                    selectedLayerId = layer.id,
                    selectedSection = sectionForLayer(layer)
                )
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        StudioSections(
            selectedSection = selectedSection,
            onSectionSelected = { editorState = editorState.copy(selectedSection = it) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        when (selectedSection) {
            TrackStudioSection.Scene -> SceneSection(
                draftConfig = draftConfig,
                onConfigChange = { updateDraftConfig(limitAtmosphereConfig(syncPrimaryLayers(it))) }
            )

            TrackStudioSection.Character -> CharacterSection(
                draftConfig = draftConfig,
                selectedLayerId = selectedLayerId,
                onConfigChange = { updateDraftConfig(limitAtmosphereConfig(syncPrimaryLayers(it))) }
            )

            TrackStudioSection.Text -> TextSection(
                draftConfig = draftConfig,
                selectedLayerId = selectedLayerId,
                onConfigChange = { updateDraftConfig(limitAtmosphereConfig(syncPrimaryLayers(it))) }
            )

            TrackStudioSection.Timing -> TimingSection(
                draftConfig = draftConfig,
                selectedLayer = timelineLayersFor(draftConfig).firstOrNull { it.id == selectedLayerId },
                onLayerChange = { updatedLayer ->
                    updateDraftConfig(
                        limitAtmosphereConfig(
                            updateTimelineLayer(
                                config = draftConfig,
                                updatedLayer = updatedLayer
                            )
                        )
                    )
                }
            )

            TrackStudioSection.Assets -> AssetsSection(
                draftConfig = draftConfig
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(
                onClick = { updateDraftConfig(AtmosphereConfig()) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset", color = Color.White)
            }

            Button(
                onClick = { onSave(limitAtmosphereConfig(draftConfig)) },
                modifier = Modifier.weight(1.4f)
            ) {
                Text("Save atmosphere")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StudioHeader(
    track: Track,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Track Studio",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = track.title,
                color = Color(0xFFC8BED8),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        TextButton(onClick = onClose) {
            Text("\u0417\u0430\u043A\u0440\u044B\u0442\u044C", color = Color.White)
        }
    }
}

@Composable
private fun PreviewCard(
    track: Track,
    draftConfig: AtmosphereConfig,
    previewTimeSeconds: Float,
    activeDragLayer: TrackStudioSection,
    onDragCharacter: (Float, Float) -> Unit,
    onDragText: (Float, Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11101A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            AtmosphereMiniPlayerContent(
                track = track,
                isPlaying = true,
                atmosphereConfig = draftConfig,
                onPlayPauseClick = {},
                onOpenFullPlayer = {},
                currentTimeSeconds = previewTimeSeconds,
                editorDragMode = activeDragLayer.editorDragMode,
                onCharacterDrag = onDragCharacter,
                onTextDrag = onDragText,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun StudioSections(
    selectedSection: TrackStudioSection,
    onSectionSelected: (TrackStudioSection) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(TrackStudioSection.ordered) { section ->
            SectionChip(
                name = section.label,
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) }
            )
        }
    }
}
