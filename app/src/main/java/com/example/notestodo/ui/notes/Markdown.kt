package com.example.notestodo.ui.notes

private val headings = Regex("""^#{1,6}\s+""", RegexOption.MULTILINE)
private val bullets = Regex("""^\s*[-*+]\s+""", RegexOption.MULTILINE)
private val numbers = Regex("""^\s*\d+\.\s+""", RegexOption.MULTILINE)
private val emphasis = Regex("""\*\*|__|~~|`""")

// Note bodies are stored as Markdown. Cards in the list show a plain preview, so the
// marks are stripped and list markers become bullets.
fun markdownToPlainText(markdown: String): String = markdown
    .replace(headings, "")
    .replace(bullets, "• ")
    .replace(numbers, "• ")
    .replace(emphasis, "")
    .trim()
