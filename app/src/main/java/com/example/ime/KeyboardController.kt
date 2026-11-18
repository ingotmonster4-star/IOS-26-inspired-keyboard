package com.example.ime

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import com.example.data.KeyboardPrefsRepository
import com.example.data.SmartSuggestionEngine
import com.example.model.EmojiKeyPlacement
import com.example.model.GlassThemePreset
import com.example.model.ImeActionType
import com.example.model.InputContextMode
import com.example.model.KeyboardLayer
import com.example.model.KeyboardPanel
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.model.ShiftState
import com.example.model.SmartSuggestion
import com.example.model.VetroSpeechState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PopupVariantState(
    val keyId: String,
    val baseLabel: String,
    val variants: List<String>,
    val selectedIndex: Int = 0,
    val isExpandedVariants: Boolean = false,
)

data class KeyboardUiState(
    val activeLayer: KeyboardLayer = KeyboardLayer.ALPHA,
    val shiftState: ShiftState = ShiftState.SHIFTED,
    val activePanel: KeyboardPanel = KeyboardPanel.KEYBOARD,
    val inputContextMode: InputContextMode = InputContextMode.TEXT,
    val emojiSearchQuery: String = "",
    val isEmojiSearchMode: Boolean = false,
    val isUtilityStripCollapsed: Boolean = false,
    val isSelectionActive: Boolean = false,
    val imeActionType: ImeActionType = ImeActionType.ENTER,
    val popupState: PopupVariantState? = null,
    val suggestions: List<SmartSuggestion> = emptyList(),
    val cursorScrubActive: Boolean = false,
    val lastDeletedWord: String? = null,
    val toastBadge: String? = null,
    val speechState: VetroSpeechState = VetroSpeechState(),
)

class KeyboardController(
    private val inputTarget: InputTarget,
    private val prefsRepository: KeyboardPrefsRepository,
    private val scope: CoroutineScope,
    var speechManager: VetroSpeechManager? = null,
    var soundManager: VetroSoundManager? = null,
) {
    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    val speechState: StateFlow<VetroSpeechState> = _uiState
        .map { it.speechState }
        .stateIn(scope, SharingStarted.Eagerly, VetroSpeechState())

    private var learnedWords: Set<String> = emptySet()
    private var lastShiftTapTimeMs: Long = 0L
    private var lastSpaceTapTimeMs: Long = 0L
    private var undoHideJob: Job? = null
    private var toastHideJob: Job? = null
    private var backspaceRepeatJob: Job? = null
    private var backspaceRepeatTriggered: Boolean = false
    private var cursorDeltaRemainder: Float = 0f
    private var spacebarHoldJob: Job? = null

    init {
        scope.launch {
            prefsRepository.learnedWordsFlow.collect { words ->
                learnedWords = words
            }
        }
        observeSpeechState()
        refreshSuggestions(KeyboardSettings())
    }

    private fun observeSpeechState() {
        val manager = speechManager ?: return
        scope.launch {
            manager.speechState.collect { speech ->
                _uiState.update { it.copy(speechState = speech) }
            }
        }
    }

    fun attachSpeechManager(manager: VetroSpeechManager) {
        this.speechManager = manager
        observeSpeechState()
    }

    fun attachSoundManager(manager: VetroSoundManager) {
        this.soundManager = manager
    }

    fun onStartInput(editorInfo: EditorInfo?, settings: KeyboardSettings) {
        val action = when (editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
            EditorInfo.IME_ACTION_SEARCH -> ImeActionType.SEARCH
            EditorInfo.IME_ACTION_SEND -> ImeActionType.SEND
            EditorInfo.IME_ACTION_GO -> ImeActionType.GO
            EditorInfo.IME_ACTION_NEXT -> ImeActionType.NEXT
            EditorInfo.IME_ACTION_DONE -> ImeActionType.DONE
            else -> ImeActionType.ENTER
        }
        val inputType = editorInfo?.inputType ?: 0
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val contextMode = when {
            inputClass == InputType.TYPE_CLASS_NUMBER || inputClass == InputType.TYPE_CLASS_PHONE ->
                InputContextMode.NUMBER
            variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ->
                InputContextMode.EMAIL
            variation == InputType.TYPE_TEXT_VARIATION_URI ->
                InputContextMode.URI
            else -> InputContextMode.TEXT
        }

        val initialLayer = if (contextMode == InputContextMode.NUMBER) {
            KeyboardLayer.NUMERIC_PAD
        } else {
            KeyboardLayer.ALPHA
        }

        val shouldAutoShift = contextMode == InputContextMode.TEXT &&
            settings.autoCapitalize &&
            shouldCapitalizeNextChar()

        _uiState.update {
            it.copy(
                activeLayer = initialLayer,
                activePanel = KeyboardPanel.KEYBOARD,
                inputContextMode = contextMode,
                imeActionType = action,
                shiftState = if (shouldAutoShift) ShiftState.SHIFTED else ShiftState.OFF,
                isEmojiSearchMode = false,
                popupState = null,
            )
        }
        refreshSuggestions(settings)
    }

    fun setSimulatedInputContext(contextMode: InputContextMode, actionType: ImeActionType, settings: KeyboardSettings) {
        val layer = if (contextMode == InputContextMode.NUMBER) KeyboardLayer.NUMERIC_PAD else KeyboardLayer.ALPHA
        _uiState.update {
            it.copy(
                inputContextMode = contextMode,
                imeActionType = actionType,
                activeLayer = layer,
                activePanel = KeyboardPanel.KEYBOARD,
            )
        }
        refreshSuggestions(settings)
    }

    fun performHaptic(view: View?, settings: KeyboardSettings, isCursorTick: Boolean = false) {
        if (!settings.hapticFeedback || view == null) return
        val strength = settings.hapticStrength.coerceIn(0.1f, 1.0f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                view.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator() && vibrator.hasAmplitudeControl()) {
                val baseMs = if (isCursorTick) 6f else 10f
                val durationMs = (baseMs + strength * (if (isCursorTick) 8f else 18f)).toLong()
                val amplitude = (strength * (if (isCursorTick) 160 else 255)).toInt().coerceIn(12, 255)
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
                return
            }
        }
        val constant = if (isCursorTick) {
            HapticFeedbackConstants.CLOCK_TICK
        } else {
            HapticFeedbackConstants.KEYBOARD_TAP
        }
        view.performHapticFeedback(constant)
    }

    fun onKeyTap(rawText: String, settings: KeyboardSettings) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        val state = _uiState.value
        if (state.isEmojiSearchMode) {
            _uiState.update { it.copy(emojiSearchQuery = it.emojiSearchQuery + rawText.lowercase()) }
            return
        }

        val textToCommit = if (state.activeLayer == KeyboardLayer.ALPHA && state.shiftState != ShiftState.OFF) {
            rawText.uppercase()
        } else {
            rawText
        }
        inputTarget.commitText(textToCommit)

        if (state.activeLayer == KeyboardLayer.ALPHA && state.shiftState == ShiftState.SHIFTED) {
            _uiState.update { it.copy(shiftState = ShiftState.OFF) }
        }
        refreshSuggestions(settings)
    }

    fun onSuggestionTap(suggestion: SmartSuggestion, settings: KeyboardSettings) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        val currentWord = SmartSuggestionEngine.extractCurrentWord(inputTarget.getTextBeforeCursor(32))
        if (currentWord.isNotEmpty()) {
            inputTarget.replaceCurrentWord(suggestion.commitText)
            scope.launch { prefsRepository.learnWord(suggestion.displayText) }
        } else {
            inputTarget.commitText(suggestion.commitText)
        }
        checkAutoCapitalize(settings)
        refreshSuggestions(settings)
    }

    fun onSpaceTap(settings: KeyboardSettings) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        val state = _uiState.value
        if (state.isEmojiSearchMode) {
            _uiState.update { it.copy(emojiSearchQuery = it.emojiSearchQuery + " ") }
            return
        }

        val currentWord = SmartSuggestionEngine.extractCurrentWord(inputTarget.getTextBeforeCursor(32))
        if (currentWord.length >= 2) {
            scope.launch { prefsRepository.learnWord(currentWord) }
        }

        val now = System.currentTimeMillis()
        val before = inputTarget.getTextBeforeCursor(6)
        val isDoubleSpace = settings.doubleSpacePeriod &&
            (now - lastSpaceTapTimeMs) < 350L &&
            before.endsWith(" ") &&
            before.length >= 2 &&
            before[before.length - 2].isLetterOrDigit()

        if (isDoubleSpace) {
            inputTarget.replaceTextBeforeCursor(1, ". ")
            lastSpaceTapTimeMs = 0L
            if (settings.autoCapitalize) {
                _uiState.update { it.copy(shiftState = ShiftState.SHIFTED) }
            }
        } else {
            inputTarget.commitText(" ")
            lastSpaceTapTimeMs = now
            checkAutoCapitalize(settings)
        }
        refreshSuggestions(settings)
    }

    fun onEnterTap(settings: KeyboardSettings) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        if (_uiState.value.isEmojiSearchMode) {
            exitEmojiSearchKeyboardMode()
            return
        }
        inputTarget.performAction(_uiState.value.imeActionType)
        if (_uiState.value.imeActionType == ImeActionType.ENTER && settings.autoCapitalize) {
            _uiState.update { it.copy(shiftState = ShiftState.SHIFTED) }
        }
        refreshSuggestions(settings)
    }

    fun onBackspaceTap(settings: KeyboardSettings) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        if (_uiState.value.isEmojiSearchMode) {
            _uiState.update {
                val q = it.emojiSearchQuery
                it.copy(emojiSearchQuery = if (q.isNotEmpty()) q.dropLast(1) else q)
            }
            return
        }
        inputTarget.deleteBackward(1)
        checkAutoCapitalize(settings)
        refreshSuggestions(settings)
    }

    fun startBackspaceRepeat(view: View?, settings: KeyboardSettings) {
        backspaceRepeatJob?.cancel()
        backspaceRepeatTriggered = false
        backspaceRepeatJob = scope.launch {
            delay(360L)
            backspaceRepeatTriggered = true
            var interval = 70L
            while (true) {
                onBackspaceTap(settings)
                performHaptic(view, settings, isCursorTick = true)
                delay(interval)
                interval = (interval - 5L).coerceAtLeast(30L)
            }
        }
    }

    fun stopBackspaceRepeatAndTapIfNeeded(settings: KeyboardSettings, swipedLeft: Boolean) {
        backspaceRepeatJob?.cancel()
        backspaceRepeatJob = null
        val didRepeat = backspaceRepeatTriggered
        backspaceRepeatTriggered = false
        if (!swipedLeft && !didRepeat) {
            onBackspaceTap(settings)
        }
    }

    fun onBackspaceSwipeLeft(settings: KeyboardSettings) {
        if (!settings.backspaceSwipeDelete) return
        backspaceRepeatJob?.cancel()
        backspaceRepeatJob = null
        backspaceRepeatTriggered = false
        val deleted = inputTarget.deleteWordBackward()
        if (!deleted.isNullOrEmpty()) {
            _uiState.update { it.copy(lastDeletedWord = deleted) }
            undoHideJob?.cancel()
            undoHideJob = scope.launch {
                delay(4500L)
                _uiState.update { it.copy(lastDeletedWord = null) }
            }
        }
        refreshSuggestions(settings)
    }

    fun onUndoWordDelete(settings: KeyboardSettings) {
        val word = _uiState.value.lastDeletedWord ?: return
        undoHideJob?.cancel()
        inputTarget.commitText(word)
        _uiState.update { it.copy(lastDeletedWord = null) }
        refreshSuggestions(settings)
    }

    fun onSpacebarDrag(deltaPx: Float, stepPx: Float, view: View?, settings: KeyboardSettings) {
        if (!settings.spacebarCursorControl) return
        _uiState.update { it.copy(cursorScrubActive = true) }
        cursorDeltaRemainder += deltaPx
        val steps = (cursorDeltaRemainder / stepPx).toInt()
        if (steps != 0) {
            cursorDeltaRemainder -= steps * stepPx
            inputTarget.moveCursor(steps, extendSelection = false)
            performHaptic(view, settings, isCursorTick = true)
        }
    }

    fun onSpacebarDragEnd(settings: KeyboardSettings) {
        cursorDeltaRemainder = 0f
        _uiState.update { it.copy(cursorScrubActive = false) }
        refreshSuggestions(settings)
    }

    fun onSpacebarLongPress() {
        inputTarget.openInputMethodPicker()
    }

    fun onShiftTap() {
        val now = System.currentTimeMillis()
        val isDoubleTap = (now - lastShiftTapTimeMs) < 320L
        lastShiftTapTimeMs = now
        _uiState.update { state ->
            val next = when {
                isDoubleTap -> ShiftState.CAPS_LOCK
                state.shiftState == ShiftState.OFF -> ShiftState.SHIFTED
                else -> ShiftState.OFF
            }
            state.copy(shiftState = next)
        }
    }

    fun toggleSymbolsLayer() {
        _uiState.update { state ->
            val next = if (state.activeLayer == KeyboardLayer.ALPHA) {
                KeyboardLayer.SYMBOLS_1
            } else {
                KeyboardLayer.ALPHA
            }
            state.copy(activeLayer = next)
        }
    }

    fun toggleSecondarySymbolsLayer() {
        _uiState.update { state ->
            val next = if (state.activeLayer == KeyboardLayer.SYMBOLS_2) {
                KeyboardLayer.SYMBOLS_1
            } else {
                KeyboardLayer.SYMBOLS_2
            }
            state.copy(activeLayer = next)
        }
    }

    fun switchToAlphaFromNumeric() {
        _uiState.update { it.copy(activeLayer = KeyboardLayer.ALPHA) }
    }

    fun togglePanel(panel: KeyboardPanel) {
        _uiState.update { state ->
            val next = if (state.activePanel == panel) KeyboardPanel.KEYBOARD else panel
            state.copy(
                activePanel = next,
                isEmojiSearchMode = false,
                isUtilityStripCollapsed = false,
            )
        }
    }

    fun toggleUtilityStripCollapsed() {
        _uiState.update { it.copy(isUtilityStripCollapsed = !it.isUtilityStripCollapsed) }
    }

    fun enterEmojiSearchKeyboardMode() {
        _uiState.update {
            it.copy(
                activePanel = KeyboardPanel.KEYBOARD,
                activeLayer = KeyboardLayer.ALPHA,
                shiftState = ShiftState.OFF,
                isEmojiSearchMode = true,
            )
        }
    }

    fun exitEmojiSearchKeyboardMode() {
        _uiState.update {
            it.copy(
                isEmojiSearchMode = false,
                activePanel = KeyboardPanel.EMOJI,
            )
        }
    }

    fun clearEmojiSearchQuery() {
        _uiState.update { it.copy(emojiSearchQuery = "") }
    }

    fun onEmojiTap(emoji: String, settings: KeyboardSettings, recordRecent: Boolean = true) {
        soundManager?.playClick(settings.soundOnKeypress, settings.soundVolume)
        inputTarget.commitText(emoji)
        if (recordRecent) {
            scope.launch { prefsRepository.recordRecentEmoji(emoji) }
        }
        refreshSuggestions(settings)
    }

    fun startSpacebarHoldTimer(settings: KeyboardSettings, onHoldCompleted: () -> Unit) {
        spacebarHoldJob?.cancel()
        val durationMs = (settings.voiceHoldDurationSec * 1000f).toLong().coerceIn(1000L, 3500L)
        val intervalMs = 25L
        val totalSteps = (durationMs / intervalMs).coerceAtLeast(1L)
        spacebarHoldJob = scope.launch {
            _uiState.update { it.copy(speechState = it.speechState.copy(isHolding = true, holdProgress = 0f)) }
            for (step in 1..totalSteps) {
                delay(intervalMs)
                val progress = step.toFloat() / totalSteps
                _uiState.update { it.copy(speechState = it.speechState.copy(holdProgress = progress, isHolding = true)) }
            }
            _uiState.update { it.copy(speechState = it.speechState.copy(holdProgress = 1f, isHolding = false)) }
            onHoldCompleted()
        }
    }

    fun cancelSpacebarHoldTimer() {
        spacebarHoldJob?.cancel()
        spacebarHoldJob = null
        _uiState.update { it.copy(speechState = it.speechState.copy(holdProgress = 0f, isHolding = false)) }
    }

    fun toggleVoiceTyping(settings: KeyboardSettings) {
        val currentListening = _uiState.value.speechState.isListening
        if (currentListening) {
            stopVoiceTyping()
        } else {
            startVoiceTyping(settings)
        }
    }

    fun startVoiceTyping(settings: KeyboardSettings) {
        val manager = speechManager
        if (manager == null) {
            showToast("Voice typing unavailable in this preview")
            return
        }
        if (!manager.hasRecordPermission()) {
            showToast("Mic permission required for Voice Ramble")
            return
        }
        manager.startListening { delta ->
            inputTarget.commitText(delta)
            refreshSuggestions(settings)
        }
    }

    fun stopVoiceTyping() {
        speechManager?.stopListening()
        _uiState.update { it.copy(speechState = it.speechState.copy(isListening = false, rmsDb = 0f, partialText = "")) }
    }

    fun onClipboardInsert(text: String, settings: KeyboardSettings) {
        inputTarget.commitText(text)
        _uiState.update { it.copy(activePanel = KeyboardPanel.KEYBOARD) }
        refreshSuggestions(settings)
    }

    fun onTogglePinClipboard(id: String) {
        scope.launch { prefsRepository.togglePinClipboard(id) }
    }

    fun onDeleteClipboard(id: String) {
        scope.launch { prefsRepository.deleteClipboardItem(id) }
    }

    fun onClearUnpinnedClipboard() {
        scope.launch { prefsRepository.clearUnpinnedClipboard() }
    }

    fun onEditMove(dx: Int = 0, dy: Int = 0) {
        val select = _uiState.value.isSelectionActive
        if (dx != 0) inputTarget.moveCursor(dx, extendSelection = select)
        if (dy != 0) inputTarget.moveCursorVertical(up = dy < 0, extendSelection = select)
    }

    fun onEditLineEdge(toEnd: Boolean) {
        inputTarget.moveCursorLineEdge(toEnd, extendSelection = _uiState.value.isSelectionActive)
    }

    fun onToggleSelectionMode() {
        _uiState.update { it.copy(isSelectionActive = !it.isSelectionActive) }
    }

    fun onEditSelectAll() = inputTarget.selectAll()
    fun onEditCopy() = inputTarget.copy()
    fun onEditCut() = inputTarget.cut()
    fun onEditPaste(fallback: String? = null) = inputTarget.paste(fallback)

    // In-Keyboard Quick Tools Hub Actions (zero friction!)
    fun cycleThemePreset(current: GlassThemePreset) {
        val entries = GlassThemePreset.entries
        val next = entries[(entries.indexOf(current) + 1) % entries.size]
        scope.launch { prefsRepository.updateThemePreset(next) }
        showToast("Theme: ${next.displayName}")
    }

    fun toggleFloatingMode(current: Boolean) {
        scope.launch { prefsRepository.updateFloatingMode(!current) }
        showToast(if (!current) "Floating Glass Pill" else "Docked Glass")
    }

    fun cycleOneHandedMode(current: OneHandedMode) {
        val entries = OneHandedMode.entries
        val next = entries[(entries.indexOf(current) + 1) % entries.size]
        scope.launch { prefsRepository.updateOneHandedMode(next) }
        showToast("Reach: ${next.displayName}")
    }

    fun toggleNumberRow(current: Boolean) {
        scope.launch { prefsRepository.updateBooleanSetting(showNumberRow = !current) }
    }

    fun cycleEmojiPlacement(current: EmojiKeyPlacement) {
        val entries = EmojiKeyPlacement.entries
        val next = entries[(entries.indexOf(current) + 1) % entries.size]
        scope.launch { prefsRepository.updateEmojiKeyPlacement(next) }
        showToast("Emoji Key: ${next.displayName}")
    }

    fun showKeyPreview(keyId: String, label: String, variants: List<String>) {
        _uiState.update {
            it.copy(
                popupState = PopupVariantState(
                    keyId = keyId,
                    baseLabel = label,
                    variants = variants,
                    selectedIndex = 0,
                    isExpandedVariants = false,
                )
            )
        }
    }

    fun expandKeyVariants(keyId: String) {
        _uiState.update { state ->
            val current = state.popupState
            if (current != null && current.keyId == keyId && current.variants.isNotEmpty()) {
                state.copy(popupState = current.copy(isExpandedVariants = true, selectedIndex = 0))
            } else state
        }
    }

    fun updateHoveredVariant(index: Int) {
        _uiState.update { state ->
            val current = state.popupState ?: return@update state
            if (!current.isExpandedVariants || current.variants.isEmpty()) return@update state
            state.copy(
                popupState = current.copy(
                    selectedIndex = index.coerceIn(0, current.variants.lastIndex)
                )
            )
        }
    }

    fun commitActiveVariantOrDismiss(settings: KeyboardSettings): Boolean {
        val popup = _uiState.value.popupState ?: return false
        _uiState.update { it.copy(popupState = null) }
        if (popup.isExpandedVariants && popup.variants.isNotEmpty()) {
            val chosen = popup.variants[popup.selectedIndex.coerceIn(0, popup.variants.lastIndex)]
            if (popup.keyId == "key_comma" && chosen == "😊") {
                togglePanel(KeyboardPanel.EMOJI)
                return true
            }
            onKeyTap(chosen, settings)
            return true
        }
        return false
    }

    fun dismissKeyPreview() {
        _uiState.update { it.copy(popupState = null) }
    }

    fun openSettingsApp() {
        inputTarget.openSettingsApp()
    }

    fun refreshSuggestions(settings: KeyboardSettings) {
        if (!settings.showSuggestions || _uiState.value.inputContextMode == InputContextMode.NUMBER) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }
        val before = inputTarget.getTextBeforeCursor(32)
        val next = SmartSuggestionEngine.suggest(
            textBeforeCursor = before,
            learnedWords = learnedWords,
        )
        _uiState.update { it.copy(suggestions = next) }
    }

    private fun checkAutoCapitalize(settings: KeyboardSettings) {
        if (!settings.autoCapitalize || _uiState.value.activeLayer != KeyboardLayer.ALPHA) return
        if (_uiState.value.shiftState == ShiftState.CAPS_LOCK) return
        if (shouldCapitalizeNextChar()) {
            _uiState.update { it.copy(shiftState = ShiftState.SHIFTED) }
        }
    }

    private fun shouldCapitalizeNextChar(): Boolean {
        val before = inputTarget.getTextBeforeCursor(16)
        if (before.isEmpty()) return true
        if (before.endsWith("\n")) return true
        val trimmed = before.trimEnd(' ')
        if (trimmed.isEmpty()) return true
        val last = trimmed.last()
        return (last == '.' || last == '!' || last == '?') && before.endsWith(" ")
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastBadge = message) }
        toastHideJob?.cancel()
        toastHideJob = scope.launch {
            delay(2200L)
            _uiState.update { it.copy(toastBadge = null) }
        }
    }
}
