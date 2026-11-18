package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.ClipboardEntry
import com.example.model.EmojiKeyPlacement
import com.example.model.GlassThemePreset
import com.example.model.KeyboardHeightPreset
import com.example.model.KeyboardSettings
import com.example.model.KeycapStyle
import com.example.model.OneHandedMode
import com.example.model.TextSnippet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.vetroDataStore: DataStore<Preferences> by preferencesDataStore(name = "vetro_settings")

class KeyboardPrefsRepository private constructor(private val appContext: Context) {

    private object Keys {
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val KEYCAP_STYLE = stringPreferencesKey("keycap_style")
        val CUSTOM_ACCENT_ARGB = longPreferencesKey("custom_accent_argb")
        val GLASS_OPACITY = floatPreferencesKey("glass_opacity")
        val SPECULAR_INTENSITY = floatPreferencesKey("specular_intensity")
        val GLASS_BORDER_SOFTNESS = floatPreferencesKey("glass_border_softness")
        val FLOATING_MODE = booleanPreferencesKey("floating_mode")
        val CORNER_RADIUS_DP = floatPreferencesKey("corner_radius_dp")
        val HEIGHT_PRESET = stringPreferencesKey("height_preset")
        val ONE_HANDED_MODE = stringPreferencesKey("one_handed_mode")
        val SHOW_NUMBER_ROW = booleanPreferencesKey("show_number_row")
        val SHOW_UTILITY_STRIP = booleanPreferencesKey("show_utility_strip")
        val SHOW_SUGGESTIONS = booleanPreferencesKey("show_suggestions")
        val AUTO_COLLAPSE_STRIP = booleanPreferencesKey("auto_collapse_strip")
        val SHOW_KEY_POPUPS = booleanPreferencesKey("show_key_popups")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val HAPTIC_STRENGTH = floatPreferencesKey("haptic_strength")
        val VOICE_HOLD_DURATION_SEC = floatPreferencesKey("voice_hold_duration_sec")
        val EMOJI_KEY_PLACEMENT = stringPreferencesKey("emoji_key_placement")
        val SOUND_ON_KEYPRESS = booleanPreferencesKey("sound_on_keypress")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
        val DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
        val SPACEBAR_CURSOR = booleanPreferencesKey("spacebar_cursor")
        val BACKSPACE_SWIPE_DELETE = booleanPreferencesKey("backspace_swipe_delete")
        val RECENT_EMOJIS = stringPreferencesKey("recent_emojis_json")
        val SNIPPETS_JSON = stringPreferencesKey("snippets_json")
        val CLIPBOARD_JSON = stringPreferencesKey("clipboard_json")
        val LEARNED_WORDS_JSON = stringPreferencesKey("learned_words_json")
    }

    val settingsFlow: Flow<KeyboardSettings> = appContext.vetroDataStore.data.map { prefs ->
        KeyboardSettings(
            themePreset = prefs[Keys.THEME_PRESET]?.let { runCatching { GlassThemePreset.valueOf(it) }.getOrNull() }
                ?: GlassThemePreset.CRYSTAL_FROST,
            keycapStyle = prefs[Keys.KEYCAP_STYLE]?.let { runCatching { KeycapStyle.valueOf(it) }.getOrNull() }
                ?: KeycapStyle.LIQUID_PILL,
            customAccentArgb = prefs[Keys.CUSTOM_ACCENT_ARGB]?.takeIf { it != 0L },
            glassOpacity = prefs[Keys.GLASS_OPACITY] ?: 0.80f,
            specularIntensity = prefs[Keys.SPECULAR_INTENSITY] ?: 0.82f,
            glassBorderSoftness = prefs[Keys.GLASS_BORDER_SOFTNESS] ?: 0.65f,
            floatingMode = prefs[Keys.FLOATING_MODE] ?: true,
            cornerRadiusDp = prefs[Keys.CORNER_RADIUS_DP] ?: 24f,
            heightPreset = prefs[Keys.HEIGHT_PRESET]?.let { runCatching { KeyboardHeightPreset.valueOf(it) }.getOrNull() }
                ?: KeyboardHeightPreset.COMPACT,
            oneHandedMode = prefs[Keys.ONE_HANDED_MODE]?.let { runCatching { OneHandedMode.valueOf(it) }.getOrNull() }
                ?: OneHandedMode.OFF,
            showNumberRow = prefs[Keys.SHOW_NUMBER_ROW] ?: false,
            showUtilityStrip = prefs[Keys.SHOW_UTILITY_STRIP] ?: true,
            showSuggestions = prefs[Keys.SHOW_SUGGESTIONS] ?: true,
            autoCollapseStrip = prefs[Keys.AUTO_COLLAPSE_STRIP] ?: false,
            showKeyPopups = prefs[Keys.SHOW_KEY_POPUPS] ?: true,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            hapticStrength = prefs[Keys.HAPTIC_STRENGTH] ?: 0.65f,
            voiceHoldDurationSec = prefs[Keys.VOICE_HOLD_DURATION_SEC] ?: 3.0f,
            emojiKeyPlacement = prefs[Keys.EMOJI_KEY_PLACEMENT]?.let { runCatching { EmojiKeyPlacement.valueOf(it) }.getOrNull() }
                ?: EmojiKeyPlacement.DEDICATED_BOTTOM_ROW,
            soundOnKeypress = prefs[Keys.SOUND_ON_KEYPRESS] ?: false,
            soundVolume = prefs[Keys.SOUND_VOLUME] ?: 0.5f,
            autoCapitalize = prefs[Keys.AUTO_CAPITALIZE] ?: true,
            doubleSpacePeriod = prefs[Keys.DOUBLE_SPACE_PERIOD] ?: true,
            spacebarCursorControl = prefs[Keys.SPACEBAR_CURSOR] ?: true,
            backspaceSwipeDelete = prefs[Keys.BACKSPACE_SWIPE_DELETE] ?: true,
            recentEmojis = prefs[Keys.RECENT_EMOJIS]?.let(::decodeStringList)
                ?: KeyboardSettings.DEFAULT_RECENT_EMOJIS,
            savedSnippets = prefs[Keys.SNIPPETS_JSON]?.let(::decodeSnippets)
                ?: KeyboardSettings.DEFAULT_SNIPPETS,
            clipboardHistory = prefs[Keys.CLIPBOARD_JSON]?.let(::decodeClipboard)
                ?: KeyboardSettings.DEFAULT_CLIPBOARD,
        )
    }

    val learnedWordsFlow: Flow<Set<String>> = appContext.vetroDataStore.data.map { prefs ->
        prefs[Keys.LEARNED_WORDS_JSON]?.let(::decodeStringList)?.toSet() ?: emptySet()
    }

    suspend fun updateThemePreset(preset: GlassThemePreset) {
        appContext.vetroDataStore.edit { it[Keys.THEME_PRESET] = preset.name }
    }

    suspend fun updateKeycapStyle(style: KeycapStyle) {
        appContext.vetroDataStore.edit { it[Keys.KEYCAP_STYLE] = style.name }
    }

    suspend fun updateCustomAccentArgb(argb: Long?) {
        appContext.vetroDataStore.edit {
            if (argb == null) it.remove(Keys.CUSTOM_ACCENT_ARGB)
            else it[Keys.CUSTOM_ACCENT_ARGB] = argb
        }
    }

    suspend fun updateGlassOpacity(opacity: Float) {
        appContext.vetroDataStore.edit { it[Keys.GLASS_OPACITY] = opacity.coerceIn(0.42f, 0.96f) }
    }

    suspend fun updateSpecularIntensity(intensity: Float) {
        appContext.vetroDataStore.edit { it[Keys.SPECULAR_INTENSITY] = intensity.coerceIn(0.15f, 1.0f) }
    }

    suspend fun updateGlassBorderSoftness(softness: Float) {
        appContext.vetroDataStore.edit { it[Keys.GLASS_BORDER_SOFTNESS] = softness.coerceIn(0.0f, 1.0f) }
    }

    suspend fun updateFloatingMode(enabled: Boolean) {
        appContext.vetroDataStore.edit { it[Keys.FLOATING_MODE] = enabled }
    }

    suspend fun updateCornerRadius(radiusDp: Float) {
        appContext.vetroDataStore.edit { it[Keys.CORNER_RADIUS_DP] = radiusDp.coerceIn(8f, 32f) }
    }

    suspend fun updateHeightPreset(preset: KeyboardHeightPreset) {
        appContext.vetroDataStore.edit { it[Keys.HEIGHT_PRESET] = preset.name }
    }

    suspend fun updateOneHandedMode(mode: OneHandedMode) {
        appContext.vetroDataStore.edit { it[Keys.ONE_HANDED_MODE] = mode.name }
    }

    suspend fun updateEmojiKeyPlacement(placement: EmojiKeyPlacement) {
        appContext.vetroDataStore.edit { it[Keys.EMOJI_KEY_PLACEMENT] = placement.name }
    }

    suspend fun updateHapticStrength(strength: Float) {
        appContext.vetroDataStore.edit { it[Keys.HAPTIC_STRENGTH] = strength.coerceIn(0.1f, 1.0f) }
    }

    suspend fun updateVoiceHoldDuration(seconds: Float) {
        appContext.vetroDataStore.edit { it[Keys.VOICE_HOLD_DURATION_SEC] = seconds.coerceIn(1.0f, 3.5f) }
    }

    suspend fun updateSoundSettings(enabled: Boolean? = null, volume: Float? = null) {
        appContext.vetroDataStore.edit { prefs ->
            enabled?.let { prefs[Keys.SOUND_ON_KEYPRESS] = it }
            volume?.let { prefs[Keys.SOUND_VOLUME] = it.coerceIn(0.0f, 1.0f) }
        }
    }

    suspend fun updateBooleanSetting(
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
    ) {
        appContext.vetroDataStore.edit { prefs ->
            showNumberRow?.let { prefs[Keys.SHOW_NUMBER_ROW] = it }
            showUtilityStrip?.let { prefs[Keys.SHOW_UTILITY_STRIP] = it }
            showSuggestions?.let { prefs[Keys.SHOW_SUGGESTIONS] = it }
            autoCollapseStrip?.let { prefs[Keys.AUTO_COLLAPSE_STRIP] = it }
            showKeyPopups?.let { prefs[Keys.SHOW_KEY_POPUPS] = it }
            hapticFeedback?.let { prefs[Keys.HAPTIC_FEEDBACK] = it }
            autoCapitalize?.let { prefs[Keys.AUTO_CAPITALIZE] = it }
            doubleSpacePeriod?.let { prefs[Keys.DOUBLE_SPACE_PERIOD] = it }
            spacebarCursorControl?.let { prefs[Keys.SPACEBAR_CURSOR] = it }
            backspaceSwipeDelete?.let { prefs[Keys.BACKSPACE_SWIPE_DELETE] = it }
        }
    }

    suspend fun learnWord(word: String) {
        val clean = word.trim().lowercase()
        if (clean.length < 2 || clean.length > 28 || !clean.all { it.isLetter() || it == '\'' }) return
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.LEARNED_WORDS_JSON]?.let(::decodeStringList) ?: emptyList()
            val updated = (listOf(clean) + current.filterNot { it == clean }).take(150)
            prefs[Keys.LEARNED_WORDS_JSON] = encodeStringList(updated)
        }
    }

    suspend fun recordRecentEmoji(emoji: String) {
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.RECENT_EMOJIS]?.let(::decodeStringList)
                ?: KeyboardSettings.DEFAULT_RECENT_EMOJIS
            val updated = (listOf(emoji) + current.filterNot { it == emoji }).take(36)
            prefs[Keys.RECENT_EMOJIS] = encodeStringList(updated)
        }
    }

    suspend fun addSnippet(label: String, content: String) {
        val cleanLabel = label.trim()
        if (cleanLabel.isEmpty() || content.isEmpty()) return
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.SNIPPETS_JSON]?.let(::decodeSnippets)
                ?: KeyboardSettings.DEFAULT_SNIPPETS
            val newItem = TextSnippet(
                id = "snip_${System.currentTimeMillis()}",
                label = cleanLabel,
                content = content,
            )
            prefs[Keys.SNIPPETS_JSON] = encodeSnippets(listOf(newItem) + current)
        }
    }

    suspend fun removeSnippet(id: String) {
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.SNIPPETS_JSON]?.let(::decodeSnippets)
                ?: KeyboardSettings.DEFAULT_SNIPPETS
            prefs[Keys.SNIPPETS_JSON] = encodeSnippets(current.filterNot { it.id == id })
        }
    }

    suspend fun addClipboardText(text: String) {
        if (text.isBlank()) return
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPBOARD_JSON]?.let(::decodeClipboard)
                ?: KeyboardSettings.DEFAULT_CLIPBOARD
            val existingPinned = current.find { it.text == text }?.isPinned ?: false
            val filtered = current.filterNot { it.text == text }
            val newEntry = ClipboardEntry(
                id = "clip_${System.currentTimeMillis()}",
                text = text.take(500),
                isPinned = existingPinned,
                timestampMs = System.currentTimeMillis(),
            )
            val combined = (listOf(newEntry) + filtered)
                .sortedByDescending { it.isPinned }
                .take(24)
            prefs[Keys.CLIPBOARD_JSON] = encodeClipboard(combined)
        }
    }

    suspend fun togglePinClipboard(id: String) {
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPBOARD_JSON]?.let(::decodeClipboard)
                ?: KeyboardSettings.DEFAULT_CLIPBOARD
            val updated = current.map {
                if (it.id == id) it.copy(isPinned = !it.isPinned) else it
            }.sortedByDescending { it.isPinned }
            prefs[Keys.CLIPBOARD_JSON] = encodeClipboard(updated)
        }
    }

    suspend fun deleteClipboardItem(id: String) {
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPBOARD_JSON]?.let(::decodeClipboard)
                ?: KeyboardSettings.DEFAULT_CLIPBOARD
            prefs[Keys.CLIPBOARD_JSON] = encodeClipboard(current.filterNot { it.id == id })
        }
    }

    suspend fun clearUnpinnedClipboard() {
        appContext.vetroDataStore.edit { prefs ->
            val current = prefs[Keys.CLIPBOARD_JSON]?.let(::decodeClipboard)
                ?: KeyboardSettings.DEFAULT_CLIPBOARD
            prefs[Keys.CLIPBOARD_JSON] = encodeClipboard(current.filter { it.isPinned })
        }
    }

    private fun encodeStringList(items: List<String>): String =
        JSONArray().apply { items.forEach { put(it) } }.toString()

    private fun decodeStringList(raw: String): List<String> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx -> arr.getString(idx) }
    }.getOrDefault(emptyList())

    private fun encodeSnippets(items: List<TextSnippet>): String {
        val arr = JSONArray()
        items.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("label", s.label)
                put("content", s.content)
            })
        }
        return arr.toString()
    }

    private fun decodeSnippets(raw: String): List<TextSnippet> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            TextSnippet(
                id = obj.getString("id"),
                label = obj.getString("label"),
                content = obj.getString("content"),
            )
        }
    }.getOrDefault(KeyboardSettings.DEFAULT_SNIPPETS)

    private fun encodeClipboard(items: List<ClipboardEntry>): String {
        val arr = JSONArray()
        items.forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id)
                put("text", c.text)
                put("isPinned", c.isPinned)
                put("timestampMs", c.timestampMs)
            })
        }
        return arr.toString()
    }

    private fun decodeClipboard(raw: String): List<ClipboardEntry> = runCatching {
        val arr = JSONArray(raw)
        List(arr.length()) { idx ->
            val obj = arr.getJSONObject(idx)
            ClipboardEntry(
                id = obj.getString("id"),
                text = obj.getString("text"),
                isPinned = obj.optBoolean("isPinned", false),
                timestampMs = obj.optLong("timestampMs", 0L),
            )
        }
    }.getOrDefault(KeyboardSettings.DEFAULT_CLIPBOARD)

    companion object {
        @Volatile
        private var instance: KeyboardPrefsRepository? = null

        fun getInstance(context: Context): KeyboardPrefsRepository {
            return instance ?: synchronized(this) {
                instance ?: KeyboardPrefsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
