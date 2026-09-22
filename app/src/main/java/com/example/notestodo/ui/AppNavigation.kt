package com.example.notestodo.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.notestodo.ui.notes.NoteEditScreen
import com.example.notestodo.ui.notes.NoteListScreen
import com.example.notestodo.ui.tasks.TaskEditScreen
import com.example.notestodo.ui.tasks.TaskListScreen
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NEW_NOTE_ID
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NOTE_ID_ARG
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.NEW_TASK_ID
import com.example.notestodo.viewmodel.TaskEditViewModel.Companion.TASK_ID_ARG

private const val NOTE_LIST_ROUTE = "notes"
private const val NOTE_EDIT_ROUTE = "note" // full route: "note/{noteId}"
private const val TASK_LIST_ROUTE = "tasks"
private const val TASK_EDIT_ROUTE = "task" // full route: "task/{taskId}"

// The tabs in the bottom bar. Phase 3 adds Calendar here.
private enum class TopLevelTab(val route: String, val label: String, val icon: ImageVector) {
    Notes(NOTE_LIST_ROUTE, "Notes", Icons.Default.Edit),
    Tasks(TASK_LIST_ROUTE, "Tasks", Icons.Default.CheckCircle),
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
                )
            }
            composable(
                route = "$TASK_EDIT_ROUTE/{$TASK_ID_ARG}",
                arguments = listOf(navArgument(TASK_ID_ARG) { type = NavType.LongType }),
            ) {
                TaskEditScreen(onBack = { navController.popBackStack() })
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
