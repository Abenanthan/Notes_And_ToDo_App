package com.example.notestodo.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

// Lets TaskRepository refresh the home-screen widget after a change without knowing
// anything about Glance. Does nothing when no widget has been added.
class TaskWidgetUpdater(private val context: Context) {

    suspend fun updateAll() {
        TodayTasksWidget().updateAll(context)
    }
}
