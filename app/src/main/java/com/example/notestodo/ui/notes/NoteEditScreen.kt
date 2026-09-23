package com.example.notestodo.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.ui.common.plainTextFieldColors
import com.example.notestodo.viewmodel.NoteEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    onBack: () -> Unit,
    viewModel: NoteEditViewModel = viewModel(factory = NoteEditViewModel.Factory),
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    // The chosen colour washes the whole editor, not just the card in the list.
    val background = noteColor(viewModel.colorIndex).takeOrElse { MaterialTheme.colorScheme.surface }

    // Navigate away only after the ViewModel has finished writing to the database.
    LaunchedEffect(viewModel.isFinished) {
        if (viewModel.isFinished) onBack()
    }
    // The system back button/gesture saves too, not just the arrow in the top bar.
    BackHandler(onBack = viewModel::saveAndClose)

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {},
                // Transparent so the note's colour runs behind the bar as well.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = viewModel::saveAndClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save and go back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onFavouriteToggle) {
                        Icon(
                            imageVector = if (viewModel.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (viewModel.isFavourite) "Remove from favourites" else "Add to favourites",
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete note")
                    }
                },
            )
        },
        bottomBar = {
            NoteColorBar(
                selectedIndex = viewModel.colorIndex,
                onColorSelected = viewModel::onColorSelected,
                containerColor = background,
                // Keeps the row clear of the navigation bar, and above the keyboard while typing.
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            )
        },
    ) { padding ->
        val titleFocus = remember { FocusRequester() }

        Column(Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge,
                colors = plainTextFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            TextField(
                value = viewModel.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Note") },
                colors = plainTextFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }

        // Open the keyboard straight away when writing a new note.
        LaunchedEffect(Unit) {
            if (viewModel.isNewNote) titleFocus.requestFocus()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete note?") },
            text = { Text("This note will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun NoteColorBar(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = containerColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()) // narrow screens can still reach every colour
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(NOTE_COLOR_COUNT) { index ->
                ColorCircle(
                    color = noteColor(index),
                    name = noteColorName(index),
                    isSelected = index == selectedIndex,
                    onClick = { onColorSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(color: Color, name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color.takeOrElse { MaterialTheme.colorScheme.surface })
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
            )
            .clickable(onClickLabel = name, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
