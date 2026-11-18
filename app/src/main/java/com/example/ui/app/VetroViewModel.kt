package com.example.ui.app

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KeyboardPrefsRepository
import com.example.ime.InAppPlaygroundInputTarget
import com.example.ime.KeyboardController
import com.example.ime.KeyboardUiState
import com.example.ime.VetroInputMethodService
import com.example.ime.VetroSoundManager
import com.example.ime.VetroSpeechManager
import com.example.model.EmojiKeyPlacement
import com.example.model.GlassThemePreset
import com.example.model.KeyboardHeightPreset
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImeSetupStatus(
    val isEnabledInSystem: Boolean = false,
    val isSelectedAsDefault: Boolean = false,
)

enum class AppTab(val label: String) {
    PLAYGROUND("Playground"),
    STUDIO("Glass Studio"),
}

class VetroViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = KeyboardPrefsRepository.getInstance(application)

    val settings: StateFlow<KeyboardSettings> = prefsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = KeyboardSettings(),
    )

    private val _selectedTab = MutableStateFlow(AppTab.PLAYGROUND)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _imeSetupStatus = MutableStateFlow(ImeSetupStatus())
    val imeSetupStatus: StateFlow<ImeSetupStatus> = _imeSetupStatus.asStateFlow()

    private val _playgroundFeedback = MutableStateFlow<String?>(null)
    val playgroundFeedback: StateFlow<String?> = _playgroundFeedback.asStateFlow()

    val playgroundTarget = InAppPlaygroundInputTarget(
        context = application,
        onActionFeedback = { msg -> _playgroundFeedback.value = msg },
        onOpenSettings = { _selectedTab.value = AppTab.STUDIO },
    )

    val playgroundTextValue: StateFlow<TextFieldValue> = playgroundTarget.textFieldValue

    val playgroundSpeechManager = VetroSpeechManager(application, viewModelScope)
    val playgroundSoundManager = VetroSoundManager(application)

    val playgroundController = KeyboardController(
        inputTarget = playgroundTarget,
        prefsRepository = prefsRepository,
        scope = viewModelScope,
        speechManager = playgroundSpeechManager,
        soundManager = playgroundSoundManager,
    )

    val playgroundKeyboardState: StateFlow<KeyboardUiState> = playgroundController.uiState

    init {
        refreshImeStatus()
        viewModelScope.launch {
            settings.collect { latest ->
                playgroundController.refreshSuggestions(latest)
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun clearPlaygroundFeedback() {
        _playgroundFeedback.value = null
    }

    fun resetPlaygroundSample() {
        val sample = "Hey! Testing Vetro's Liquid Glass keyboard ✨ Smooth, responsive, and super clean."
        playgroundTarget.updateFromExternalField(
            TextFieldValue(
                text = sample,
                selection = androidx.compose.ui.text.TextRange(sample.length),
            )
        )
        playgroundController.refreshSuggestions(settings.value)
    }

    fun clearPlaygroundText() {
        playgroundTarget.updateFromExternalField(TextFieldValue(""))
        playgroundController.refreshSuggestions(settings.value)
    }

    fun setSimulatedPlaygroundContext(
        contextMode: com.example.model.InputContextMode,
        actionType: com.example.model.ImeActionType,
    ) {
        playgroundController.setSimulatedInputContext(contextMode, actionType, settings.value)
    }

    fun refreshImeStatus() {
        val ctx = getApplication<Application>()
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val expectedComponent = ComponentName(ctx, VetroInputMethodService::class.java)
        val expectedShort = expectedComponent.flattenToShortString()
        val expectedFull = expectedComponent.flattenToString()

        val enabled = imm?.enabledInputMethodList?.any { info ->
            info.component == expectedComponent ||
                info.id == expectedShort ||
                info.id == expectedFull
        } ?: false

        val defaultIme = runCatching {
            Settings.Secure.getString(ctx.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        }.getOrNull()

        val selected = defaultIme == expectedShort || defaultIme == expectedFull
        _imeSetupStatus.value = ImeSetupStatus(
            isEnabledInSystem = enabled,
            isSelectedAsDefault = selected,
        )
    }

    fun openSystemImePicker() {
        val imm = getApplication<Application>().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }

    // Glass Studio Mutations
    fun setThemePreset(preset: GlassThemePreset) = viewModelScope.launch {
        prefsRepository.updateThemePreset(preset)
    }

    fun setKeycapStyle(style: com.example.model.KeycapStyle) = viewModelScope.launch {
        prefsRepository.updateKeycapStyle(style)
    }

    fun setCustomAccentArgb(argb: Long?) = viewModelScope.launch {
        prefsRepository.updateCustomAccentArgb(argb)
    }

    fun setGlassOpacity(value: Float) = viewModelScope.launch {
        prefsRepository.updateGlassOpacity(value)
    }

    fun setSpecularIntensity(value: Float) = viewModelScope.launch {
        prefsRepository.updateSpecularIntensity(value)
    }

    fun setFloatingMode(enabled: Boolean) = viewModelScope.launch {
        prefsRepository.updateFloatingMode(enabled)
    }

    fun setCornerRadius(radiusDp: Float) = viewModelScope.launch {
        prefsRepository.updateCornerRadius(radiusDp)
    }

    fun setHeightPreset(preset: KeyboardHeightPreset) = viewModelScope.launch {
        prefsRepository.updateHeightPreset(preset)
    }

    fun setOneHandedMode(mode: OneHandedMode) = viewModelScope.launch {
        prefsRepository.updateOneHandedMode(mode)
    }

    fun setBooleanPref(
        showNumberRow: Boolean? = null,
        showUtilityStrip: Boolean? = null,
        showSuggestions: Boolean? = null,
        autoCollapseStrip: Boolean? = null,
        showKeyPopups: Boolean? = null,
        hapticFeedback: Boolean? = null,
        autoCapitalize: Boolean? = null,
        doubleSpacePeriod: Boolean? = null,
        spacebarCursorControl: Boolean? = null,
        backspaceSwipeDelete: Boolean? = null,
    ) = viewModelScope.launch {
        prefsRepository.updateBooleanSetting(
            showNumberRow = showNumberRow,
            showUtilityStrip = showUtilityStrip,
            showSuggestions = showSuggestions,
            autoCollapseStrip = autoCollapseStrip,
            showKeyPopups = showKeyPopups,
            hapticFeedback = hapticFeedback,
            autoCapitalize = autoCapitalize,
            doubleSpacePeriod = doubleSpacePeriod,
            spacebarCursorControl = spacebarCursorControl,
            backspaceSwipeDelete = backspaceSwipeDelete,
        )
    }

    fun setHapticStrength(value: Float) = viewModelScope.launch {
        prefsRepository.updateHapticStrength(value)
    }

    fun setGlassBorderSoftness(value: Float) = viewModelScope.launch {
        prefsRepository.updateGlassBorderSoftness(value)
    }

    fun setVoiceHoldDuration(seconds: Float) = viewModelScope.launch {
        prefsRepository.updateVoiceHoldDuration(seconds)
    }

    fun setEmojiKeyPlacement(placement: EmojiKeyPlacement) = viewModelScope.launch {
        prefsRepository.updateEmojiKeyPlacement(placement)
    }

    fun setSoundSettings(enabled: Boolean? = null, volume: Float? = null) = viewModelScope.launch {
        prefsRepository.updateSoundSettings(enabled = enabled, volume = volume)
    }

    fun hasRecordAudioPermission(): Boolean {
        return playgroundSpeechManager.hasRecordPermission()
    }

    fun addSnippet(label: String, content: String) = viewModelScope.launch {
        prefsRepository.addSnippet(label, content)
    }

    fun removeSnippet(id: String) = viewModelScope.launch {
        prefsRepository.removeSnippet(id)
    }
}
