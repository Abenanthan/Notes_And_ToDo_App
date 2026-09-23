package com.example.notestodo.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notestodo.BuildConfig
import com.example.notestodo.NotesApp
import com.example.notestodo.data.repository.ThemeMode
import com.example.notestodo.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTrash: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val settings = viewModel.settings.collectAsStateWithLifecycle().value
    // Wallpaper colours only exist from Android 12 onwards.
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader("Appearance")
            ThemeMode.entries.forEach { mode ->
                val isSelected = settings.themeMode == mode
                ListItem(
                    headlineContent = { Text(themeModeLabel(mode)) },
                    leadingContent = {
                        // onClick = null: the whole row handles the click, and the radio
                        // button just shows the state.
                        RadioButton(selected = isSelected, onClick = null)
                    },
                    modifier = Modifier.selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { viewModel.onThemeModeChange(mode) },
                    ),
                )
            }

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Material You colours") },
                supportingContent = {
                    Text(
                        if (dynamicColorSupported) "Take colours from your wallpaper"
                        else "Needs Android 12 or newer"
                    )
                },
                trailingContent = {
                    Switch(
                        checked = settings.dynamicColor && dynamicColorSupported,
                        onCheckedChange = viewModel::onDynamicColorChange,
                        enabled = dynamicColorSupported,
                    )
                },
            )

            HorizontalDivider()

            SectionHeader("Data")
            ListItem(
                headlineContent = { Text("Trash") },
                supportingContent = {
                    Text("Deleted notes and tasks, kept for ${NotesApp.TRASH_RETENTION.inWholeDays} days")
                },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                modifier = Modifier.clickable(onClick = onOpenTrash),
            )

            HorizontalDivider()

            SectionHeader("About")
            ListItem(
                headlineContent = { Text("Notes & To-Do") },
                supportingContent = { Text("Version ${BuildConfig.VERSION_NAME}") },
            )
        }
    }
}

private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "Follow system"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
