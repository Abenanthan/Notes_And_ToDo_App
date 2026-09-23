package com.example.notestodo.ui.notes

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Index 0 means "leave it to the theme"; the rest are pastels with a darker twin so a
// coloured note stays readable in dark mode.
private val lightNoteColors = listOf(
    Color.Unspecified,
    Color(0xFFFFF475), // yellow
    Color(0xFFFBBC04), // orange
    Color(0xFFF28B82), // red
    Color(0xFFCCFF90), // green
    Color(0xFFCBF0F8), // blue
    Color(0xFFD7AEFB), // purple
)

private val darkNoteColors = listOf(
    Color.Unspecified,
    Color(0xFF635D19),
    Color(0xFF614A19),
    Color(0xFF5C2B29),
    Color(0xFF345920),
    Color(0xFF2D555E),
    Color(0xFF42275E),
)

private val noteColorNames = listOf("Default", "Yellow", "Orange", "Red", "Green", "Blue", "Purple")

val NOTE_COLOR_COUNT = lightNoteColors.size

// Color.Unspecified is Material's "use the default" value, so it can be handed straight
// to CardDefaults.outlinedCardColors().
@Composable
fun noteColor(index: Int): Color {
    // Read the applied theme rather than the system setting, so this still follows the
    // app's own light/dark choice once there is a theme toggle.
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val colors = if (isDark) darkNoteColors else lightNoteColors
    return colors.getOrElse(index) { Color.Unspecified }
}

fun noteColorName(index: Int): String = noteColorNames.getOrElse(index) { "Default" }
