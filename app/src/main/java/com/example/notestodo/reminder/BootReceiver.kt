package com.example.notestodo.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notestodo.NotesApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Android forgets every alarm when the device restarts, so the pending ones are set again here.
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repository = (context.applicationContext as NotesApp).taskRepository
        // goAsync keeps this receiver alive while the tasks are read on a background thread.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAllReminders()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
