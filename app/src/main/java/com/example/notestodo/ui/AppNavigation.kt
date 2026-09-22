package com.example.notestodo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notestodo.ui.notes.NoteEditScreen
import com.example.notestodo.ui.notes.NoteListScreen
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NEW_NOTE_ID
import com.example.notestodo.viewmodel.NoteEditViewModel.Companion.NOTE_ID_ARG

private const val NOTE_LIST_ROUTE = "notes"
private const val NOTE_EDIT_ROUTE = "note" // full route: "note/{noteId}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = NOTE_LIST_ROUTE) {
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
    }
}
