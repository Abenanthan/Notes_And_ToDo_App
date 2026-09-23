package com.example.notestodo.ui.notes

import android.text.format.DateUtils
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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.ui.common.plainTextFieldColors
import com.example.notestodo.viewmodel.NoteEditViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults
import kotlinx.coroutines.flow.drop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    onBack: () -> Unit,
    viewModel: NoteEditViewModel = viewModel(factory = NoteEditViewModel.Factory),
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    // The chosen colour washes the whole editor, not just the card in the list.
    val background = noteColor(viewModel.colorIndex).takeOrElse { MaterialTheme.colorScheme.surface }

    // The editor holds the formatted body; the ViewModel only supplies the starting text
    // and takes the finished Markdown back when the note is saved.
    val richTextState = rememberRichTextState()
    val history = remember { EditorHistory() }
    var isEditorReady by remember { mutableStateOf(false) }

    // Load the note into the editor once it has arrived from the database.
    LaunchedEffect(viewModel.isLoaded) {
        if (!viewModel.isLoaded) return@LaunchedEffect
        richTextState.setMarkdown(viewModel.content)
        val loaded = richTextState.toMarkdown()
        viewModel.onEditorLoaded(loaded)
        history.reset(loaded)
        isEditorReady = true
    }

    // Record edits for undo. drop(1) skips the text that was just loaded above.
    LaunchedEffect(isEditorReady) {
        if (!isEditorReady) return@LaunchedEffect
        snapshotFlow { richTextState.annotatedString }
            .drop(1)
            .collect { history.record(richTextState.toMarkdown()) }
    }

    fun save() = viewModel.saveAndClose(richTextState.toMarkdown())

    // Navigate away only after the ViewModel has finished writing to the database.
    LaunchedEffect(viewModel.isFinished) {
        if (viewModel.isFinished) onBack()
    }
    // The system back button/gesture saves too, not just the arrow in the top bar.
    BackHandler(onBack = ::save)

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {},
                // Transparent so the note's colour runs behind the bar as well.
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = ::save) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Save and go back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { history.undo()?.let { richTextState.setMarkdown(it) } },
                        enabled = history.canUndo,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = { history.redo()?.let { richTextState.setMarkdown(it) } },
                        enabled = history.canRedo,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = viewModel::onFavouriteToggle) {
                        Icon(
                            imageVector = if (viewModel.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (viewModel.isFavourite) "Remove from favourites" else "Add to favourites",
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Move note to trash")
                    }
                },
            )
        },
        bottomBar = {
            Column(
                // Keeps the bars clear of the navigation bar, and above the keyboard while typing.
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            ) {
                FormattingBar(state = richTextState, containerColor = background)
                NoteColorBar(
                    selectedIndex = viewModel.colorIndex,
                    onColorSelected = viewModel::onColorSelected,
                    containerColor = background,
                )
            }
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

            MetaLine(
                updatedAt = viewModel.updatedAt,
                characterCount = richTextState.annotatedString.text.length,
            )

            RichTextEditor(
                state = richTextState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Note") },
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
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
            title = { Text("Move note to trash?") },
            text = { Text("You can restore it from Settings → Trash for 30 days.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Move to trash") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun MetaLine(updatedAt: Long, characterCount: Int) {
    Text(
        text = "${DateUtils.getRelativeTimeSpanString(updatedAt)}  |  $characterCount characters",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
    )
}

// Bold, italic and the rest, applied to the selection or to whatever is typed next.
// Only styles that survive a round trip through Markdown are offered: underline, for
// instance, has no Markdown equivalent and would be lost on reopening.
@Composable
private fun FormattingBar(state: RichTextState, containerColor: Color) {
    val currentSpan = state.currentSpanStyle

    Surface(color = containerColor) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FormatToggle(
                    icon = Icons.Default.FormatBold,
                    label = "Bold",
                    checked = currentSpan.fontWeight == FontWeight.Bold,
                    onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                )
                FormatToggle(
                    icon = Icons.Default.FormatItalic,
                    label = "Italic",
                    checked = currentSpan.fontStyle == FontStyle.Italic,
                    onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                )
                FormatToggle(
                    icon = Icons.Default.FormatStrikethrough,
                    label = "Strikethrough",
                    checked = currentSpan.textDecoration == TextDecoration.LineThrough,
                    onClick = {
                        state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
                    },
                )
                FormatToggle(
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    label = "Bullet list",
                    checked = state.isUnorderedList,
                    onClick = { state.toggleUnorderedList() },
                )
                FormatToggle(
                    icon = Icons.Default.FormatListNumbered,
                    label = "Numbered list",
                    checked = state.isOrderedList,
                    onClick = { state.toggleOrderedList() },
                )
            }
        }
    }
}

@Composable
private fun FormatToggle(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    FilledIconToggleButton(checked = checked, onCheckedChange = { onClick() }) {
        Icon(icon, contentDescription = label)
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
