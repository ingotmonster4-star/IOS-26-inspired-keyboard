package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.EmojiCatalog
import com.example.data.KeyboardPrefsRepository
import com.example.data.SmartSuggestionEngine
import com.example.ime.InAppPlaygroundInputTarget
import com.example.ime.KeyboardController
import com.example.model.EmojiKeyPlacement
import com.example.model.ImeActionType
import com.example.model.InputContextMode
import com.example.model.KeyboardLayer
import com.example.model.KeyboardPanel
import com.example.model.KeyboardSettings
import com.example.model.ShiftState
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VetroKeyboardTest {

    @Test
    fun `emoji catalog includes full Gboard categories country flags skin tones and search`() {
        assertEquals(9, EmojiCatalog.categories.size)
        assertTrue(EmojiCatalog.totalCount >= 1200)

        // All major Gboard categories populated deeply
        val flagsCat = EmojiCatalog.categories.first { it.id == "flags" }
        assertTrue(flagsCat.emojis.size >= 240)
        assertTrue(flagsCat.emojis.any { it.emoji == "🇯🇵" && it.name.contains("Japan") })

        // Search works across categories & countries
        assertTrue(EmojiCatalog.search("fire").any { it.emoji == "🔥" })
        assertTrue(EmojiCatalog.search("heart").any { it.emoji == "❤️" })
        assertTrue(EmojiCatalog.search("brazil").any { it.emoji == "🇧🇷" })

        // Skin tone variants supported for hands & people
        val wave = EmojiCatalog.findByGlyph("👋")
        assertTrue(wave?.supportsSkinTone == true)
        val tones = EmojiCatalog.skinToneVariants("👋")
        assertEquals(6, tones.size)

        // Emoticons tab populated
        assertTrue(EmojiCatalog.emoticonGroups.isNotEmpty())
    }

    @Test
    fun `smart suggestion engine provides clean pure text continuous predictions`() {
        val cleanAlpha = SmartSuggestionEngine.suggest("alpha")
        assertTrue(cleanAlpha.any { it.displayText == "alpha" })

        val prefixSuggestions = SmartSuggestionEngine.suggest("liq")
        assertTrue(prefixSuggestions.any { it.displayText == "liquid" })

        val nextWord = SmartSuggestionEngine.suggest("Thank ")
        assertTrue(nextWord.any { it.displayText.startsWith("you") })

        // Continuous prediction never abruptly stops after arbitrary words
        val afterUnknown = SmartSuggestionEngine.suggest("xylophone ")
        assertEquals(3, afterUnknown.size)
    }

    @Test
    fun `keyboard controller supports typing emoji search mode and swipe delete undo`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val target = InAppPlaygroundInputTarget(context = context, initialText = "")
        val prefs = KeyboardPrefsRepository.getInstance(context)
        val scope = TestScope()
        val controller = KeyboardController(target, prefs, scope)
        val settings = KeyboardSettings()

        // Starts shifted at empty buffer with clean word starters
        assertEquals(ShiftState.SHIFTED, controller.uiState.value.shiftState)
        assertEquals(listOf("I", "The", "How"), controller.uiState.value.suggestions.map { it.displayText })
        assertEquals(EmojiKeyPlacement.DEDICATED_BOTTOM_ROW, settings.emojiKeyPlacement)

        controller.onKeyTap("h", settings)
        controller.onKeyTap("i", settings)
        assertEquals("Hi", target.textFieldValue.value.text)

        // Backspace single tap deletes 1 char cleanly
        controller.stopBackspaceRepeatAndTapIfNeeded(settings, swipedLeft = false)
        assertEquals("H", target.textFieldValue.value.text)

        // Emoji search keyboard mode captures query without polluting editor text
        controller.enterEmojiSearchKeyboardMode()
        assertTrue(controller.uiState.value.isEmojiSearchMode)
        controller.onKeyTap("f", settings)
        controller.onKeyTap("i", settings)
        controller.onKeyTap("r", settings)
        controller.onKeyTap("e", settings)
        assertEquals("fire", controller.uiState.value.emojiSearchQuery)
        assertEquals("H", target.textFieldValue.value.text)

        controller.onEmojiTap("🔥", settings)
        assertEquals("H🔥", target.textFieldValue.value.text)
        controller.exitEmojiSearchKeyboardMode()
        assertFalse(controller.uiState.value.isEmojiSearchMode)
        assertEquals(KeyboardPanel.EMOJI, controller.uiState.value.activePanel)

        // Swipe left deletes word and offers undo
        controller.togglePanel(KeyboardPanel.KEYBOARD)
        controller.onKeyTap(" ", settings)
        controller.onKeyTap("w", settings)
        controller.onKeyTap("o", settings)
        controller.onBackspaceSwipeLeft(settings)
        assertEquals("H🔥 ", target.textFieldValue.value.text)
        assertEquals("wo", controller.uiState.value.lastDeletedWord)

        // Undo restores word
        controller.onUndoWordDelete(settings)
        assertEquals("H🔥 wo", target.textFieldValue.value.text)

        // Numeric context mode switches to NUMERIC_PAD and clears suggestions cleanly
        controller.setSimulatedInputContext(InputContextMode.NUMBER, ImeActionType.DONE, settings)
        assertEquals(KeyboardLayer.NUMERIC_PAD, controller.uiState.value.activeLayer)
        assertTrue(controller.uiState.value.suggestions.isEmpty())
    }
}
