package com.opensound.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.opensound.app.editor.TrackStudioSection
import com.opensound.app.models.AtmosphereConfig
import com.opensound.app.models.Track
import com.opensound.app.player.AtmosphereMiniPlayerContent

@Composable
internal fun TrackStudioHeader(
    track: Track,
    isDirty: Boolean,
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
            if (isDirty) {
                Text(
                    text = "Unsaved changes",
                    color = Color(0xFF9B5CFF),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        TextButton(onClick = onClose) {
            Text("\u0417\u0430\u043A\u0440\u044B\u0442\u044C", color = Color.White)
        }
    }
}

@Composable
internal fun TrackStudioPreviewCard(
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
internal fun TrackStudioSectionTabs(
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

@Composable
internal fun TrackStudioSaveBar(
    isDirty: Boolean,
    onReset: () -> Unit,
    onSave: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(
            onClick = onReset,
            enabled = isDirty,
            modifier = Modifier.weight(1f)
        ) {
            Text("Reset", color = Color.White)
        }

        Button(
            onClick = onSave,
            enabled = isDirty,
            modifier = Modifier.weight(1.4f)
        ) {
            Text("Save atmosphere")
        }
    }
}

@Composable
internal fun TrackStudioUnsavedChangesDialog(
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = {
            Text("\u041D\u0435\u0441\u043E\u0445\u0440\u0430\u043D\u0435\u043D\u043D\u044B\u0435 \u0438\u0437\u043C\u0435\u043D\u0435\u043D\u0438\u044F")
        },
        text = {
            Text("\u0417\u0430\u043A\u0440\u044B\u0442\u044C \u0440\u0435\u0434\u0430\u043A\u0442\u043E\u0440 \u0438 \u043F\u043E\u0442\u0435\u0440\u044F\u0442\u044C \u0442\u0435\u043A\u0443\u0449\u0438\u0439 \u0447\u0435\u0440\u043D\u043E\u0432\u0438\u043A?")
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text("\u0417\u0430\u043A\u0440\u044B\u0442\u044C")
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text("\u041E\u0441\u0442\u0430\u0442\u044C\u0441\u044F")
            }
        }
    )
}
