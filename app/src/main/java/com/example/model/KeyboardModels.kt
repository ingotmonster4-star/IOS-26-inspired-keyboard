package com.example.model

enum class GlassThemePreset(val displayName: String, val subtitle: String) {
    CRYSTAL_FROST("Crystal Frost", "iOS 26 light translucent ice"),
    OBSIDIAN_GLASS("Obsidian Glass", "Smoked sapphire dark frost"),
    AURORA_PRISM("Aurora Prism", "Iridescent cyan-violet glass"),
    ROSE_QUARTZ("Rose Quartz", "Warm blush crystal sheen"),
    ADAPTIVE_AUTO("Adaptive Auto", "Follows system light & dark"),
}

enum class KeycapStyle(val displayName: String, val subtitle: String) {
    LIQUID_PILL("Liquid Glass", "Rounded 3D glass bevels"),
    SOFT_SQUIRCLE("iOS Squircle", "Classic crisp iOS geometry"),
    FRAMELESS_AIR("Air Frameless", "Ultra-minimal zero-clutter keys"),
}

enum class EmojiKeyPlacement(
    val displayName: String,
    val shortLabel: String,
    val description: String,
) {
    DEDICATED_BOTTOM_ROW("Bottom Row Key", "Bottom Row", "Dedicated emoji icon beside Comma & Spacebar"),
    TOP_STRIP("Top Utility Strip", "Top Strip", "Emoji icon in the top utility strip for thumb reach"),
    LONG_PRESS_COMMA("Long-Press Comma", "Hold ,", "Clean bottom row; press & hold ',' to open Emoji"),
    ALL("Both Row & Top Strip", "Row + Strip", "Show emoji icon on bottom row, top strip & comma"),
    TOOLS_ONLY("Control Center Only", "Tools Hub", "Ultra-minimal keys; open Emoji via ⊞ Tools"),
}

enum class KeyboardHeightPreset(val displayName: String, val keyHeightDp: Float) {
    COMPACT("Compact", 42f),
    BALANCED("Balanced", 47f),
    ERGONOMIC("Ergonomic", 53f),
}

enum class OneHandedMode(val displayName: String) {
    OFF("Full Width"),
    LEFT("Left"),
    RIGHT("Right"),
}

enum class KeyboardLayer {
    ALPHA,
    SYMBOLS_1,
    SYMBOLS_2,
    NUMERIC_PAD,
}

enum class ShiftState {
    OFF,
    SHIFTED,
    CAPS_LOCK,
}

enum class KeyboardPanel {
    KEYBOARD,
    TOOLS,
    EMOJI,
    CLIPBOARD,
    EDIT_PAD,
}

enum class ImeActionType {
    ENTER,
    SEARCH,
    SEND,
    GO,
    NEXT,
    DONE,
}

enum class InputContextMode {
    TEXT,
    EMAIL,
    URI,
    NUMBER,
}

data class SmartSuggestion(
    val displayText: String,
    val commitText: String,
    val isPrimary: Boolean = false,
)

data class VetroSpeechState(
    val isListening: Boolean = false,
    val rmsDb: Float = 0f,
    val partialText: String = "",
    val holdProgress: Float = 0f,
    val isHolding: Boolean = false,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null,
)

data class TextSnippet(
    val id: String,
    val label: String,
    val content: String,
)

data class ClipboardEntry(
    val id: String,
    val text: String,
    val isPinned: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis(),
)

data class KeyboardSettings(
    val themePreset: GlassThemePreset = GlassThemePreset.CRYSTAL_FROST,
    val keycapStyle: KeycapStyle = KeycapStyle.LIQUID_PILL,
    val customAccentArgb: Long? = null,
    val glassOpacity: Float = 0.80f,
    val specularIntensity: Float = 0.82f,
    val glassBorderSoftness: Float = 0.65f,
    val floatingMode: Boolean = true,
    val cornerRadiusDp: Float = 24f,
    val heightPreset: KeyboardHeightPreset = KeyboardHeightPreset.COMPACT,
    val oneHandedMode: OneHandedMode = OneHandedMode.OFF,
    val showNumberRow: Boolean = false,
    val showUtilityStrip: Boolean = true,
    val showSuggestions: Boolean = true,
    val autoCollapseStrip: Boolean = false,
    val showKeyPopups: Boolean = true,
    val hapticFeedback: Boolean = true,
    val hapticStrength: Float = 0.65f,
    val voiceHoldDurationSec: Float = 3.0f,
    val emojiKeyPlacement: EmojiKeyPlacement = EmojiKeyPlacement.DEDICATED_BOTTOM_ROW,
    val soundOnKeypress: Boolean = false,
    val soundVolume: Float = 0.5f,
    val autoCapitalize: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val spacebarCursorControl: Boolean = true,
    val backspaceSwipeDelete: Boolean = true,
    val recentEmojis: List<String> = DEFAULT_RECENT_EMOJIS,
    val savedSnippets: List<TextSnippet> = DEFAULT_SNIPPETS,
    val clipboardHistory: List<ClipboardEntry> = DEFAULT_CLIPBOARD,
) {
    companion object {
        val DEFAULT_RECENT_EMOJIS = listOf("😀", "😂", "❤️", "✨", "🔥", "👍", "🙏", "🥺", "🎉", "💯", "🥳", "😎")
        val DEFAULT_SNIPPETS = listOf(
            TextSnippet("omw", "On my way", "On my way! Be there in 10 mins."),
            TextSnippet("thanks", "Thank you", "Thanks so much! 🙏"),
            TextSnippet("sig", "Sign-off", "Best regards,\n"),
            TextSnippet("shrug", "Shrug", "¯\\_(ツ)_/¯"),
        )
        val DEFAULT_CLIPBOARD = listOf(
            ClipboardEntry("welcome_1", "Welcome to Vetro Liquid Glass ✨", isPinned = true),
            ClipboardEntry("welcome_2", "Hey! Let me know when you're free 👍", isPinned = false),
        )
    }
}
