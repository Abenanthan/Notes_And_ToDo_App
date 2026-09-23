package com.example.notestodo.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notestodo.ui.calendar.CalendarScreen
import com.example.notestodo.ui.notes.NoteEditScreen
import com.example.notestodo.ui.notes.NoteListScreen
import com.example.notestodo.ui.settings.SettingsScreen
import com.example.notestodo.ui.tasks.TaskEditScreen
import com.example.notestodo.ui.tasks.TaskListScreen
import com.example.notestodo.ui.trash.TrashScreen
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NEW_NOTE_ID
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NOTE_ID_ARG
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.DUE_DATE_ARG
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.NEW_TASK_ID
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.NO_DUE_DATE
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.TASK_ID_ARG

private const val NOTE_LIST_ROUTE = "notes"
private const val NOTE_EDIT_ROUTE = "note" // full route: "note/{noteId}"
private const val TASK_LIST_ROUTE = "tasks"
private const val TASK_EDIT_ROUTE = "task" // full route: "task/{taskId}?dueDate={dueDate}"
private const val CALENDAR_ROUTE = "calendar"
private const val SETTINGS_ROUTE = "settings"
private const val TRASH_ROUTE = "trash"

// The tabs in the bottom bar.
private enum class TopLevelTab(val route: String, val label: String, val icon: ImageVector) {
    Notes(NOTE_LIST_ROUTE, "Notes", Icons.Default.Edit),
    Tasks(TASK_LIST_ROUTE, "Tasks", Icons.Default.CheckCircle),
    Calendar(CALENDAR_ROUTE, "Calendar", Icons.Default.DateRange),
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    // Only the top-level lists show the bottom bar; the editors get the full screen.
    val showBottomBar = TopLevelTab.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(currentRoute = currentRoute, onTabClick = navController::navigateToTab)
            }
        },
        // Each screen's own Scaffold handles the status bar, so this one adds no insets itself.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NOTE_LIST_ROUTE,
            // consumeWindowInsets tells the inner Scaffolds the bottom bar already covers the
            // system navigation bar, so they don't leave that gap a second time.
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable(NOTE_LIST_ROUTE) {
                NoteListScreen(
                    onNoteClick = { id -> navController.navigate("$NOTE_EDIT_ROUTE/$id") },
                    onAddNote = { navController.navigate("$NOTE_EDIT_ROUTE/$NEW_NOTE_ID") },
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            }
            composable(
                route = "$NOTE_EDIT_ROUTE/{$NOTE_ID_ARG}",
                arguments = listOf(navArgument(NOTE_ID_ARG) { type = NavType.LongType }),
            ) {
                NoteEditScreen(onBack = { navController.popBackStack() })
            }

            composable(TASK_LIST_ROUTE) {
                TaskListScreen(
                    onTaskClick = { id -> navController.navigate("$TASK_EDIT_ROUTE/$id") },
                    onAddTask = { navController.navigate("$TASK_EDIT_ROUTE/$NEW_TASK_ID") },
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            }
            // "?dueDate=..." is optional: the task list opens this screen without it,
            // the calendar adds the day that was tapped.
            composable(
                route = "$TASK_EDIT_ROUTE/{$TASK_ID_ARG}?$DUE_DATE_ARG={$DUE_DATE_ARG}",
                arguments = listOf(
                    navArgument(TASK_ID_ARG) { type = NavType.LongType },
                    navArgument(DUE_DATE_ARG) {
                        type = NavType.LongType
                        defaultValue = NO_DUE_DATE
                    },
                ),
            ) {
                TaskEditScreen(onBack = { navController.popBackStack() })
            }

            composable(CALENDAR_ROUTE) {
                CalendarScreen(
                    onTaskClick = { id -> navController.navigate("$TASK_EDIT_ROUTE/$id") },
                    onAddTask = { date ->
                        navController.navigate(
                            "$TASK_EDIT_ROUTE/$NEW_TASK_ID?$DUE_DATE_ARG=${date.toEpochDay()}"
                        )
                    },
                    onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                )
            }

            composable(SETTINGS_ROUTE) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTrash = { navController.navigate(TRASH_ROUTE) },
                )
            }

            composable(TRASH_ROUTE) {
                TrashScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(currentRoute: String?, onTabClick: (String) -> Unit) {
    NavigationBar {
        TopLevelTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
        }
    }
}

// Standard bottom-bar navigation: keep a single copy of each tab on the back stack and
// restore its state (e.g. the notes search text) when you come back to it.
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
