package com.example.notestodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.notestodo.MainActivity
import com.example.notestodo.NotesApp
import com.example.notestodo.data.local.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val headerDateFormat = DateTimeFormatter.ofPattern("d MMM")

// Home-screen widget listing what is due today. Glance builds the widget from composables
// rather than RemoteViews and XML layouts.
class TodayTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as NotesApp).taskRepository

        provideContent {
            val today = LocalDate.now()
            // remember keyed on the day: without it every recomposition would build a new
            // Flow, restart the collection and recompose again, round and round.
            val tasksFlow = remember(today) { repository.getTasksBetween(today, today) }
            val tasks by tasksFlow.collectAsState(initial = emptyList())

            GlanceTheme {
                WidgetBody(today = today, tasks = tasks.filterNot { it.isDone })
            }
        }
    }
}

class TodayTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidget()
}

@Composable
private fun WidgetBody(today: LocalDate, tasks: List<Task>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            // appWidgetBackground + cornerRadius give the rounded shape Android 12 expects.
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                text = "Today",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = GlanceModifier.defaultWeight(),
            )
            Text(
                text = today.format(headerDateFormat),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 13.sp),
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "Nothing due today",
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 14.sp),
            )
        } else {
            LazyColumn {
                items(tasks, itemId = { it.id }) { task ->
                    // Ticking the box completes the task without opening the app.
                    CheckBox(
                        checked = false,
                        onCheckedChange = actionRunCallback<CompleteTaskAction>(
                            actionParametersOf(TaskIdParameter to task.id)
                        ),
                        text = task.title,
                        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp),
                        maxLines = 2,
                        modifier = GlanceModifier.padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

val TaskIdParameter = ActionParameters.Key<Long>("taskId")

// Runs in the background when a checkbox on the widget is ticked.
class CompleteTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[TaskIdParameter] ?: return
        val repository = (context.applicationContext as NotesApp).taskRepository
        repository.getTask(taskId)?.let { repository.saveTask(it.copy(isDone = true)) }
        // Redraw straight away rather than waiting for the next update.
        TodayTasksWidget().update(context, glanceId)
    }
}
