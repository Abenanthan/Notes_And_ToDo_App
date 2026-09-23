package com.example.notestodo.ui.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private const val MAX_STEPS = 50

// A burst of typing becomes one undo step rather than one per letter.
private const val STEP_GAP_MS = 700L

// Undo/redo for the note body, as snapshots of its Markdown. The editor library has no
// history of its own, so the screen keeps one.
class EditorHistory {

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private var current = ""
    private var lastPushAt = 0L

    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set

    // Called once the note has been loaded into the editor: that text is the starting
    // point, not something to undo back to.
    fun reset(markdown: String) {
        current = markdown
        undoStack.clear()
        redoStack.clear()
        refresh()
    }

    fun record(markdown: String) {
        // Also skips the text that undo() and redo() just wrote into the editor,
        // since those set `current` before the editor reports back.
        if (markdown == current) return

        val now = System.currentTimeMillis()
        if (undoStack.isEmpty() || now - lastPushAt > STEP_GAP_MS) {
            undoStack.addLast(current)
            if (undoStack.size > MAX_STEPS) undoStack.removeFirst()
            lastPushAt = now
            redoStack.clear()
        }
        current = markdown
        refresh()
    }

    /** Returns the text to put back in the editor, or null when there is nothing to undo. */
    fun undo(): String? {
        val previous = undoStack.removeLastOrNull() ?: return null
        redoStack.addLast(current)
        current = previous
        refresh()
        return previous
    }

    fun redo(): String? {
        val next = redoStack.removeLastOrNull() ?: return null
        undoStack.addLast(current)
        current = next
        refresh()
        return next
    }

    private fun refresh() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }
}
