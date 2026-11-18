package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.SystemClock
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.model.ImeActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.max
import kotlin.math.min

interface InputTarget {
    fun commitText(text: String)
    fun deleteBackward(count: Int = 1)
    fun deleteWordBackward(): String?
    fun replaceTextBeforeCursor(deleteCount: Int, newText: String)
    fun replaceCurrentWord(newText: String)
    fun moveCursor(delta: Int, extendSelection: Boolean = false)
    fun moveCursorVertical(up: Boolean, extendSelection: Boolean = false)
    fun moveCursorLineEdge(toEnd: Boolean, extendSelection: Boolean = false)
    fun selectAll()
    fun cut(): String?
    fun copy(): String?
    fun paste(fallbackText: String? = null)
    fun performAction(action: ImeActionType)
    fun getTextBeforeCursor(maxChars: Int = 32): String
    fun openInputMethodPicker()
    fun openSettingsApp()
}

class InAppPlaygroundInputTarget(
    private val context: Context,
    initialText: String = "Tap anywhere or try Liquid Glass typing ✨\nDrag spacebar to glide cursor ↔  Swipe ← on backspace to delete words.",
    private val onActionFeedback: (String) -> Unit = {},
    private val onOpenSettings: () -> Unit = {},
) : InputTarget {

    private val _textFieldValue = MutableStateFlow(
        TextFieldValue(text = initialText, selection = TextRange(initialText.length))
    )
    val textFieldValue: StateFlow<TextFieldValue> = _textFieldValue.asStateFlow()

    fun updateFromExternalField(value: TextFieldValue) {
        _textFieldValue.value = value
    }

    override fun commitText(text: String) {
        _textFieldValue.update { current ->
            val start = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            val end = max(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            val nextText = buildString {
                append(current.text.substring(0, start))
                append(text)
                append(current.text.substring(end))
            }
            val nextCursor = start + text.length
            TextFieldValue(text = nextText, selection = TextRange(nextCursor))
        }
    }

    override fun deleteBackward(count: Int) {
        _textFieldValue.update { current ->
            val start = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            val end = max(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            if (start != end) {
                val nextText = current.text.removeRange(start, end)
                TextFieldValue(nextText, TextRange(start))
            } else if (start > 0) {
                // Handle surrogate pairs / emoji cleanly
                var deleteStart = start
                var remaining = count
                while (remaining > 0 && deleteStart > 0) {
                    val prevCodePoint = Character.codePointBefore(current.text, deleteStart)
                    deleteStart -= Character.charCount(prevCodePoint)
                    remaining--
                }
                val nextText = current.text.removeRange(deleteStart, start)
                TextFieldValue(nextText, TextRange(deleteStart))
            } else {
                current
            }
        }
    }

    override fun deleteWordBackward(): String? {
        var deleted: String? = null
        _textFieldValue.update { current ->
            val start = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            val end = max(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            if (start != end) {
                deleted = current.text.substring(start, end)
                TextFieldValue(current.text.removeRange(start, end), TextRange(start))
            } else if (start > 0) {
                var idx = start
                while (idx > 0 && current.text[idx - 1].isWhitespace()) idx--
                while (idx > 0 && !current.text[idx - 1].isWhitespace()) idx--
                if (idx < start) {
                    deleted = current.text.substring(idx, start)
                    TextFieldValue(current.text.removeRange(idx, start), TextRange(idx))
                } else current
            } else current
        }
        return deleted
    }

    override fun replaceTextBeforeCursor(deleteCount: Int, newText: String) {
        _textFieldValue.update { current ->
            val pos = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            val start = (pos - deleteCount).coerceAtLeast(0)
            val next = current.text.substring(0, start) + newText + current.text.substring(pos)
            TextFieldValue(next, TextRange(start + newText.length))
        }
    }

    override fun replaceCurrentWord(newText: String) {
        _textFieldValue.update { current ->
            val pos = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
            var wordStart = pos
            while (wordStart > 0) {
                val ch = current.text[wordStart - 1]
                if (ch.isLetterOrDigit() || ch == '\'') wordStart-- else break
            }
            val next = current.text.substring(0, wordStart) + newText + current.text.substring(pos)
            TextFieldValue(next, TextRange(wordStart + newText.length))
        }
    }

    override fun moveCursor(delta: Int, extendSelection: Boolean) {
        _textFieldValue.update { current ->
            val anchor = current.selection.start
            val active = current.selection.end
            val target = (active + delta).coerceIn(0, current.text.length)
            if (extendSelection) {
                TextFieldValue(current.text, TextRange(anchor, target))
            } else {
                TextFieldValue(current.text, TextRange(target))
            }
        }
    }

    override fun moveCursorVertical(up: Boolean, extendSelection: Boolean) {
        _textFieldValue.update { current ->
            val text = current.text
            val pos = current.selection.end.coerceIn(0, text.length)
            val lineStart = text.lastIndexOf('\n', (pos - 1).coerceAtLeast(0)).let { if (it == -1 || pos == 0) 0 else it + 1 }
            val col = pos - lineStart
            val target = if (up) {
                if (lineStart == 0) 0
                else {
                    val prevLineEnd = lineStart - 1
                    val prevLineStart = text.lastIndexOf('\n', (prevLineEnd - 1).coerceAtLeast(0))
                        .let { if (it == -1 || prevLineEnd == 0) 0 else it + 1 }
                    (prevLineStart + col).coerceAtMost(prevLineEnd)
                }
            } else {
                val nextNewline = text.indexOf('\n', pos)
                if (nextNewline == -1) text.length
                else {
                    val nextLineStart = nextNewline + 1
                    val nextLineEnd = text.indexOf('\n', nextLineStart).let { if (it == -1) text.length else it }
                    (nextLineStart + col).coerceAtMost(nextLineEnd)
                }
            }
            if (extendSelection) TextFieldValue(text, TextRange(current.selection.start, target))
            else TextFieldValue(text, TextRange(target))
        }
    }

    override fun moveCursorLineEdge(toEnd: Boolean, extendSelection: Boolean) {
        _textFieldValue.update { current ->
            val text = current.text
            val pos = current.selection.end.coerceIn(0, text.length)
            val target = if (toEnd) {
                text.indexOf('\n', pos).let { if (it == -1) text.length else it }
            } else {
                text.lastIndexOf('\n', (pos - 1).coerceAtLeast(0)).let { if (it == -1 || pos == 0) 0 else it + 1 }
            }
            if (extendSelection) TextFieldValue(text, TextRange(current.selection.start, target))
            else TextFieldValue(text, TextRange(target))
        }
    }

    override fun selectAll() {
        _textFieldValue.update { current ->
            TextFieldValue(current.text, TextRange(0, current.text.length))
        }
    }

    override fun cut(): String? {
        val copied = copy() ?: return null
        _textFieldValue.update { current ->
            val start = min(current.selection.start, current.selection.end)
            val end = max(current.selection.start, current.selection.end)
            if (start == end) current
            else TextFieldValue(current.text.removeRange(start, end), TextRange(start))
        }
        return copied
    }

    override fun copy(): String? {
        val current = _textFieldValue.value
        val start = min(current.selection.start, current.selection.end)
        val end = max(current.selection.start, current.selection.end)
        if (start == end) return null
        val selected = current.text.substring(start, end)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Vetro", selected))
        onActionFeedback("Copied to clipboard")
        return selected
    }

    override fun paste(fallbackText: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clipText = clipboard?.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.coerceToText(context)?.toString()
            ?: fallbackText
        if (!clipText.isNullOrEmpty()) {
            commitText(clipText)
        }
    }

    override fun performAction(action: ImeActionType) {
        when (action) {
            ImeActionType.ENTER -> commitText("\n")
            else -> onActionFeedback("${action.name.lowercase().replaceFirstChar { it.uppercase() }} action triggered")
        }
    }

    override fun getTextBeforeCursor(maxChars: Int): String {
        val current = _textFieldValue.value
        val pos = min(current.selection.start, current.selection.end).coerceIn(0, current.text.length)
        val start = (pos - maxChars).coerceAtLeast(0)
        return current.text.substring(start, pos)
    }

    override fun openInputMethodPicker() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun openSettingsApp() {
        onOpenSettings()
    }
}

class SystemImeInputTarget(
    private val service: InputMethodService,
    private val onClipboardCaptured: (String) -> Unit,
    private val onOpenSettings: () -> Unit,
) : InputTarget {

    private val ic: InputConnection?
        get() = service.currentInputConnection

    override fun commitText(text: String) {
        ic?.commitText(text, 1)
    }

    override fun deleteBackward(count: Int) {
        val conn = ic ?: return
        val selected = conn.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            conn.commitText("", 1)
            return
        }
        val before = conn.getTextBeforeCursor(count * 2, 0)?.toString().orEmpty()
        if (before.isEmpty()) {
            sendKey(KeyEvent.KEYCODE_DEL)
            return
        }
        var charDeleteCount = 0
        var idx = before.length
        var remaining = count
        while (remaining > 0 && idx > 0) {
            val cp = Character.codePointBefore(before, idx)
            val len = Character.charCount(cp)
            charDeleteCount += len
            idx -= len
            remaining--
        }
        conn.deleteSurroundingText(charDeleteCount, 0)
    }

    override fun deleteWordBackward(): String? {
        val conn = ic ?: return null
        val selected = conn.getSelectedText(0)?.toString()
        if (!selected.isNullOrEmpty()) {
            conn.commitText("", 1)
            return selected
        }
        val before = conn.getTextBeforeCursor(64, 0)?.toString().orEmpty()
        if (before.isEmpty()) return null
        var idx = before.length
        while (idx > 0 && before[idx - 1].isWhitespace()) idx--
        while (idx > 0 && !before[idx - 1].isWhitespace()) idx--
        val deleteLen = before.length - idx
        if (deleteLen <= 0) return null
        val deletedText = before.substring(idx)
        conn.deleteSurroundingText(deleteLen, 0)
        return deletedText
    }

    override fun replaceTextBeforeCursor(deleteCount: Int, newText: String) {
        val conn = ic ?: return
        conn.beginBatchEdit()
        conn.deleteSurroundingText(deleteCount, 0)
        conn.commitText(newText, 1)
        conn.endBatchEdit()
    }

    override fun replaceCurrentWord(newText: String) {
        val conn = ic ?: return
        val before = conn.getTextBeforeCursor(32, 0)?.toString().orEmpty()
        var deleteLen = 0
        var idx = before.length
        while (idx > 0) {
            val ch = before[idx - 1]
            if (ch.isLetterOrDigit() || ch == '\'') {
                deleteLen++
                idx--
            } else break
        }
        conn.beginBatchEdit()
        if (deleteLen > 0) conn.deleteSurroundingText(deleteLen, 0)
        conn.commitText(newText, 1)
        conn.endBatchEdit()
    }

    override fun moveCursor(delta: Int, extendSelection: Boolean) {
        val conn = ic ?: return
        if (!extendSelection) {
            val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
            repeat(kotlin.math.abs(delta).coerceAtMost(24)) { sendKey(keyCode) }
        } else {
            val extracted = conn.getExtractedText(ExtractedTextRequest(), 0)
            if (extracted != null) {
                val start = extracted.selectionStart
                val end = (extracted.selectionEnd + delta).coerceIn(0, extracted.text.length)
                conn.setSelection(start, end)
            } else {
                val keyCode = if (delta < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
                repeat(kotlin.math.abs(delta).coerceAtMost(12)) {
                    sendShiftedKey(keyCode)
                }
            }
        }
    }

    override fun moveCursorVertical(up: Boolean, extendSelection: Boolean) {
        val code = if (up) KeyEvent.KEYCODE_DPAD_UP else KeyEvent.KEYCODE_DPAD_DOWN
        if (extendSelection) sendShiftedKey(code) else sendKey(code)
    }

    override fun moveCursorLineEdge(toEnd: Boolean, extendSelection: Boolean) {
        val code = if (toEnd) KeyEvent.KEYCODE_MOVE_END else KeyEvent.KEYCODE_MOVE_HOME
        if (extendSelection) sendShiftedKey(code) else sendKey(code)
    }

    override fun selectAll() {
        ic?.performContextMenuAction(android.R.id.selectAll)
    }

    override fun cut(): String? {
        val conn = ic ?: return null
        val selected = conn.getSelectedText(0)?.toString()
        conn.performContextMenuAction(android.R.id.cut)
        if (!selected.isNullOrBlank()) onClipboardCaptured(selected)
        return selected
    }

    override fun copy(): String? {
        val conn = ic ?: return null
        val selected = conn.getSelectedText(0)?.toString()
        conn.performContextMenuAction(android.R.id.copy)
        if (!selected.isNullOrBlank()) onClipboardCaptured(selected)
        return selected
    }

    override fun paste(fallbackText: String?) {
        val conn = ic ?: return
        val handled = conn.performContextMenuAction(android.R.id.paste)
        if (!handled && !fallbackText.isNullOrEmpty()) {
            conn.commitText(fallbackText, 1)
        }
    }

    override fun performAction(action: ImeActionType) {
        val conn = ic ?: return
        val editorInfo = service.currentInputEditorInfo
        val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_UNSPECIFIED
        val shouldNewline = action == ImeActionType.ENTER ||
            (editorInfo != null && (editorInfo.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0)
        if (shouldNewline || imeAction == EditorInfo.IME_ACTION_NONE || imeAction == EditorInfo.IME_ACTION_UNSPECIFIED) {
            sendKey(KeyEvent.KEYCODE_ENTER)
        } else {
            conn.performEditorAction(imeAction)
        }
    }

    override fun getTextBeforeCursor(maxChars: Int): String {
        return ic?.getTextBeforeCursor(maxChars, 0)?.toString().orEmpty()
    }

    override fun openInputMethodPicker() {
        val imm = service.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    override fun openSettingsApp() {
        onOpenSettings()
    }

    private fun sendKey(keyCode: Int) {
        val conn = ic ?: return
        val now = SystemClock.uptimeMillis()
        conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    }

    private fun sendShiftedKey(keyCode: Int) {
        val conn = ic ?: return
        val now = SystemClock.uptimeMillis()
        conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, KeyEvent.META_SHIFT_ON))
        conn.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, KeyEvent.META_SHIFT_ON))
    }
}
