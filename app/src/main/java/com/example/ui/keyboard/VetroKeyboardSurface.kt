package com.example.ui.keyboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.EmojiCatalog
import com.example.data.KeySpec
import com.example.data.KeyboardLayouts
import com.example.ime.KeyboardController
import com.example.ime.KeyboardUiState
import com.example.model.EmojiKeyPlacement
import com.example.model.ImeActionType
import com.example.model.InputContextMode
import com.example.model.KeyboardLayer
import com.example.model.KeyboardPanel
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.model.ShiftState
import com.example.ui.theme.GlassThemePalette
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.resolveGlassPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VetroKeyboardSurface(
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
    isSystemIme: Boolean,
    modifier: Modifier = Modifier,
) {
    val isSystemDark = isSystemInDarkTheme()
    val palette = remember(
        settings.themePreset,
        isSystemDark,
        settings.glassOpacity,
        settings.specularIntensity,
        settings.customAccentArgb,
    ) {
        resolveGlassPalette(
            preset = settings.themePreset,
            isSystemDark = isSystemDark,
            glassOpacity = settings.glassOpacity,
            specularIntensity = settings.specularIntensity,
            customAccentArgb = settings.customAccentArgb,
        )
    }

    val outerHorizontalPadding = if (settings.floatingMode) 8.dp else 0.dp
    val outerBottomPadding = if (settings.floatingMode) 8.dp else 0.dp
    val shellShape = if (settings.floatingMode) {
        RoundedCornerShape(settings.cornerRadiusDp.dp)
    } else {
        RoundedCornerShape(topStart = settings.cornerRadiusDp.dp, topEnd = settings.cornerRadiusDp.dp)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isSystemIme) Modifier.navigationBarsPadding() else Modifier)
            .padding(
                start = outerHorizontalPadding,
                end = outerHorizontalPadding,
                bottom = outerBottomPadding,
                top = 4.dp,
            ),
    ) {
        val widthFraction = if (settings.oneHandedMode == OneHandedMode.OFF) 1f else 0.85f
        val capsuleAlignment = when (settings.oneHandedMode) {
            OneHandedMode.LEFT -> Alignment.CenterStart
            OneHandedMode.RIGHT -> Alignment.CenterEnd
            OneHandedMode.OFF -> Alignment.Center
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .align(capsuleAlignment)
                .liquidGlassShell(
                    palette = palette,
                    shape = shellShape,
                    elevation = if (settings.floatingMode) 18.dp else 8.dp,
                    borderSoftness = settings.glassBorderSoftness,
                )
                .padding(horizontal = 5.dp, vertical = 6.dp)
                .testTag("vetro_keyboard_capsule"),
        ) {
            // 1. De-Cramped Ergonomic Gboard Strip (Tools Hub + Smart Suggestions / Quick Pinned + Atlas + Clips)
            if (settings.showUtilityStrip) {
                ErgonomicUtilityStrip(
                    palette = palette,
                    settings = settings,
                    uiState = uiState,
                    controller = controller,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 2. Non-obstructive inline toast pill (pin confirmation / theme change)
            AnimatedVisibility(visible = uiState.toastBadge != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(palette.activeChipBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = uiState.toastBadge.orEmpty(),
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFamily,
                        )
                    }
                }
            }

            // 3. Main Keyboard Body (fixed ergonomic height across all drawers)
            val keyHeight = settings.heightPreset.keyHeightDp.dp
            val rowSpacing = 2.dp
            val rowCount = if (settings.showNumberRow && uiState.activeLayer == KeyboardLayer.ALPHA) 5 else 4
            val drawerHeight = ((keyHeight + 5.dp) * rowCount) + (rowSpacing * (rowCount - 1))

            AnimatedContent(
                targetState = uiState.activePanel,
                transitionSpec = {
                    (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMedium)))
                        .togetherWith(
                            fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                scaleOut(targetScale = 0.98f)
                        )
                },
                label = "keyboard_panel_switch",
            ) { panel ->
                when (panel) {
                    KeyboardPanel.KEYBOARD -> KeyboardKeyGrid(
                        keySlotHeight = keyHeight + 5.dp,
                        palette = palette,
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                    )
                    KeyboardPanel.TOOLS -> ToolsHubDrawerContent(
                        height = drawerHeight,
                        palette = palette,
                        settings = settings,
                        controller = controller,
                    )
                    KeyboardPanel.EMOJI -> EmojiDrawerContent(
                        height = drawerHeight,
                        palette = palette,
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                    )
                    KeyboardPanel.EDIT_PAD -> EditPadDrawerContent(
                        height = drawerHeight,
                        palette = palette,
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                    )
                    KeyboardPanel.CLIPBOARD -> ClipboardDrawerContent(
                        height = drawerHeight,
                        palette = palette,
                        settings = settings,
                        controller = controller,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErgonomicUtilityStrip(
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
) {
    val view = LocalView.current

    if (uiState.isEmojiSearchMode) {
        val matches = remember(uiState.emojiSearchQuery) {
            if (uiState.emojiSearchQuery.isBlank()) {
                EmojiCatalog.categories.first().emojis.take(16)
            } else {
                EmojiCatalog.search(uiState.emojiSearchQuery, limit = 24)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.activeChipBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.stripSurface)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.accent, modifier = Modifier.size(14.dp))
                Text(
                    text = if (uiState.emojiSearchQuery.isEmpty()) "Type emoji..." else uiState.emojiSearchQuery,
                    color = if (uiState.emojiSearchQuery.isEmpty()) palette.keySubText else palette.keyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                matches.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                controller.performHaptic(view, settings)
                                controller.onEmojiTap(item.emoji, settings)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = item.emoji, fontSize = 19.sp)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent)
                    .clickable { controller.exitEmojiSearchKeyboardMode() }
                    .padding(horizontal = 9.dp, vertical = 5.dp)
                    .testTag("ime_search_done_btn"),
            ) {
                Text(
                    text = "Done",
                    color = palette.onAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        return
    }

    if (uiState.isUtilityStripCollapsed) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .liquidGlassKeycap(
                        palette = palette,
                        keycapStyle = settings.keycapStyle,
                        isModifier = true,
                        borderSoftness = settings.glassBorderSoftness,
                        cornerRadius = 11.dp,
                    )
                    .clickable { controller.toggleUtilityStripCollapsed() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Expand toolbar",
                    tint = palette.keySubText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        return
    }

    val toolsOpen = uiState.activePanel == KeyboardPanel.TOOLS
    val toolsRotation by animateFloatAsState(
        targetValue = if (toolsOpen) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tools_icon_spin",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // Left: Gboard-style Tools Control Center Hub button
        UtilityPillButton(
            isActive = toolsOpen,
            palette = palette,
            modifier = Modifier.testTag("ime_btn_tools_hub"),
            onClick = {
                controller.performHaptic(view, settings)
                controller.togglePanel(KeyboardPanel.TOOLS)
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = "Tools Control Center",
                tint = if (toolsOpen) palette.onAccent else palette.keyText,
                modifier = Modifier.size(16.dp).rotate(toolsRotation),
            )
        }

        if (settings.emojiKeyPlacement == EmojiKeyPlacement.TOP_STRIP || settings.emojiKeyPlacement == EmojiKeyPlacement.ALL) {
            UtilityPillButton(
                isActive = uiState.activePanel == KeyboardPanel.EMOJI,
                palette = palette,
                modifier = Modifier.testTag("ime_btn_emoji_strip"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.togglePanel(KeyboardPanel.EMOJI)
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mood,
                    contentDescription = "Emoji",
                    tint = if (uiState.activePanel == KeyboardPanel.EMOJI) palette.onAccent else palette.keyText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Center: Undo Chip OR Smart Gboard Suggestions OR Roomy Pinned Unicode (never cramped!)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.lastDeletedWord != null -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(palette.accent)
                            .clickable {
                                controller.performHaptic(view, settings)
                                controller.onUndoWordDelete(settings)
                            }
                            .padding(horizontal = 12.dp)
                            .testTag("ime_undo_word_btn"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Undo,
                            contentDescription = "Undo delete",
                            tint = palette.onAccent,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo \"${uiState.lastDeletedWord}\"",
                            color = palette.onAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                settings.showSuggestions && uiState.suggestions.isNotEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        uiState.suggestions.forEachIndexed { idx, suggestion ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (suggestion.isPrimary) palette.stripSurface else Color.Transparent)
                                    .clickable {
                                        controller.performHaptic(view, settings, isCursorTick = true)
                                        controller.onSuggestionTap(suggestion, settings)
                                    }
                                    .padding(horizontal = 6.dp)
                                    .testTag("ime_suggestion_$idx"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = suggestion.displayText,
                                    color = palette.keyText,
                                    fontSize = 12.sp,
                                    fontWeight = if (suggestion.isPrimary) FontWeight.SemiBold else FontWeight.Medium,
                                    fontFamily = PlusJakartaSansFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Clean distraction-free frosted space when predictions are hidden or numeric
                    Spacer(modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Right: Clean Clipboard & Ramble Voice toggles (Unicode lives on its own dedicated page in ⊞ Tools)

        UtilityPillButton(
            isActive = uiState.activePanel == KeyboardPanel.CLIPBOARD,
            palette = palette,
            modifier = Modifier.testTag("ime_btn_clipboard_panel"),
            onClick = {
                controller.performHaptic(view, settings)
                controller.togglePanel(KeyboardPanel.CLIPBOARD)
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.ContentPaste,
                contentDescription = "Clipboard and Snippets",
                tint = if (uiState.activePanel == KeyboardPanel.CLIPBOARD) palette.onAccent else palette.keyText,
                modifier = Modifier.size(15.dp),
            )
        }

        UtilityPillButton(
            isActive = uiState.speechState.isListening,
            palette = palette,
            modifier = Modifier.testTag("ime_btn_mic_strip"),
            onClick = {
                controller.performHaptic(view, settings)
                controller.toggleVoiceTyping(settings)
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = "Voice Typing",
                tint = if (uiState.speechState.isListening) palette.onAccent else palette.keyText,
                modifier = Modifier.size(15.dp),
            )
        }

        if (settings.autoCollapseStrip) {
            UtilityPillButton(
                isActive = false,
                palette = palette,
                onClick = { controller.toggleUtilityStripCollapsed() },
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExpandLess,
                    contentDescription = "Collapse toolbar",
                    tint = palette.keySubText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun UtilityPillButton(
    isActive: Boolean,
    palette: GlassThemePalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(width = 34.dp, height = 30.dp)
            .liquidGlassKeycap(
                palette = palette,
                isModifier = !isActive,
                isAccent = isActive,
                borderSoftness = 0.75f,
                cornerRadius = 10.dp,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun KeyboardKeyGrid(
    keySlotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
) {
    if (uiState.activeLayer == KeyboardLayer.NUMERIC_PAD) {
        NumericPadGrid(
            keySlotHeight = keySlotHeight,
            palette = palette,
            settings = settings,
            uiState = uiState,
            controller = controller,
        )
        return
    }

    val isUpper = uiState.activeLayer == KeyboardLayer.ALPHA && uiState.shiftState != ShiftState.OFF

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        // Optional Number Row
        if (settings.showNumberRow && uiState.activeLayer == KeyboardLayer.ALPHA) {
            Row(modifier = Modifier.fillMaxWidth()) {
                KeyboardLayouts.numberRow.forEachIndexed { idx, key ->
                    CharacterKeyButton(
                        spec = key,
                        displayLabel = key.primary,
                        slotHeight = keySlotHeight,
                        palette = palette,
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                        isLeftEdge = idx == 0,
                        isRightEdge = idx >= 7,
                        isTopRow = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Row 1
        val row1 = when (uiState.activeLayer) {
            KeyboardLayer.ALPHA -> KeyboardLayouts.alphaRow1
            KeyboardLayer.SYMBOLS_1 -> KeyboardLayouts.symbol1Row1
            else -> KeyboardLayouts.symbol2Row1
        }
        val isFirstRowTop = !settings.showNumberRow || uiState.activeLayer != KeyboardLayer.ALPHA
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEachIndexed { idx, key ->
                val label = if (isUpper) key.primary.uppercase() else key.primary
                CharacterKeyButton(
                    spec = key,
                    displayLabel = label,
                    slotHeight = keySlotHeight,
                    palette = palette,
                    settings = settings,
                    uiState = uiState,
                    controller = controller,
                    isLeftEdge = idx == 0,
                    isRightEdge = idx >= 7,
                    isTopRow = isFirstRowTop,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Row 2
        val row2 = when (uiState.activeLayer) {
            KeyboardLayer.ALPHA -> KeyboardLayouts.alphaRow2
            KeyboardLayer.SYMBOLS_1 -> KeyboardLayouts.symbol1Row2
            else -> KeyboardLayouts.symbol2Row2
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            if (uiState.activeLayer == KeyboardLayer.ALPHA) {
                Spacer(modifier = Modifier.weight(0.45f))
            }
            row2.forEachIndexed { idx, key ->
                val label = if (isUpper) key.primary.uppercase() else key.primary
                CharacterKeyButton(
                    spec = key,
                    displayLabel = label,
                    slotHeight = keySlotHeight,
                    palette = palette,
                    settings = settings,
                    uiState = uiState,
                    controller = controller,
                    isLeftEdge = idx == 0 && uiState.activeLayer != KeyboardLayer.ALPHA,
                    isRightEdge = idx >= 6,
                    isTopRow = false,
                    modifier = Modifier.weight(1f),
                )
            }
            if (uiState.activeLayer == KeyboardLayer.ALPHA) {
                Spacer(modifier = Modifier.weight(0.45f))
            }
        }

        // Row 3
        val row3 = when (uiState.activeLayer) {
            KeyboardLayer.ALPHA -> KeyboardLayouts.alphaRow3Letters
            KeyboardLayer.SYMBOLS_1 -> KeyboardLayouts.symbol1Row3
            else -> KeyboardLayouts.symbol2Row3
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            if (uiState.activeLayer == KeyboardLayer.ALPHA) {
                ShiftKeyButton(
                    slotHeight = keySlotHeight,
                    shiftState = uiState.shiftState,
                    palette = palette,
                    settings = settings,
                    controller = controller,
                    modifier = Modifier.weight(1.45f),
                )
            } else {
                ModifierLabelKeyButton(
                    label = if (uiState.activeLayer == KeyboardLayer.SYMBOLS_1) "=\\<" else "?123",
                    slotHeight = keySlotHeight,
                    palette = palette,
                    settings = settings,
                    controller = controller,
                    modifier = Modifier.weight(1.45f).testTag("ime_key_sym_page"),
                    onClick = { controller.toggleSecondarySymbolsLayer() },
                )
            }

            row3.forEachIndexed { idx, key ->
                val label = if (isUpper) key.primary.uppercase() else key.primary
                CharacterKeyButton(
                    spec = key,
                    displayLabel = label,
                    slotHeight = keySlotHeight,
                    palette = palette,
                    settings = settings,
                    uiState = uiState,
                    controller = controller,
                    isLeftEdge = false,
                    isRightEdge = idx >= 5,
                    isTopRow = false,
                    modifier = Modifier.weight(1f),
                )
            }

            BackspaceKeyButton(
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                controller = controller,
                modifier = Modifier.weight(1.45f),
            )
        }

        // Row 4 (Context-aware Gboard Bottom Row with dynamic Emoji placement)
        val leftAuxSpec = when (uiState.inputContextMode) {
            InputContextMode.EMAIL -> KeySpec(
                id = "key_at",
                primary = "@",
                secondaryHint = ",",
                longPressVariants = listOf("@", ",", "_", "-", "."),
            )
            InputContextMode.URI -> KeySpec(
                id = "key_slash",
                primary = "/",
                secondaryHint = ":",
                longPressVariants = listOf("/", ":", "?", "&", "=", "."),
            )
            else -> {
                val showCommaEmoji = settings.emojiKeyPlacement == EmojiKeyPlacement.LONG_PRESS_COMMA ||
                    settings.emojiKeyPlacement == EmojiKeyPlacement.ALL
                KeySpec(
                    id = "key_comma",
                    primary = ",",
                    secondaryHint = if (showCommaEmoji) "😊" else "—",
                    longPressVariants = if (showCommaEmoji) {
                        listOf("😊") + KeyboardLayouts.commaLongPressVariants
                    } else {
                        KeyboardLayouts.commaLongPressVariants
                    },
                )
            }
        }

        val periodVariants = when (uiState.inputContextMode) {
            InputContextMode.EMAIL -> listOf(".", ".com", ".org", ".net", ".edu", "!", "?")
            InputContextMode.URI -> listOf(".", ".com", ".io", ".dev", ".org", "/", ":")
            else -> KeyboardLayouts.periodLongPressVariants
        }

        val showBottomEmojiKey = settings.emojiKeyPlacement == EmojiKeyPlacement.DEDICATED_BOTTOM_ROW ||
            settings.emojiKeyPlacement == EmojiKeyPlacement.ALL

        Row(modifier = Modifier.fillMaxWidth()) {
            ModifierLabelKeyButton(
                label = if (uiState.activeLayer == KeyboardLayer.ALPHA) "?123" else "ABC",
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                controller = controller,
                modifier = Modifier.weight(1.45f).testTag("ime_key_layer_toggle"),
                onClick = { controller.toggleSymbolsLayer() },
            )

            CharacterKeyButton(
                spec = leftAuxSpec,
                displayLabel = leftAuxSpec.primary,
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                uiState = uiState,
                controller = controller,
                isModifierStyle = true,
                isLeftEdge = false,
                isRightEdge = false,
                isTopRow = false,
                modifier = Modifier.weight(if (showBottomEmojiKey) 0.90f else 0.95f),
            )

            if (showBottomEmojiKey) {
                ModifierLabelKeyButton(
                    label = "Emoji",
                    icon = Icons.Rounded.Mood,
                    isActive = uiState.activePanel == KeyboardPanel.EMOJI,
                    slotHeight = keySlotHeight,
                    palette = palette,
                    settings = settings,
                    controller = controller,
                    modifier = Modifier.weight(0.90f).testTag("ime_key_bottom_emoji"),
                    onClick = { controller.togglePanel(KeyboardPanel.EMOJI) },
                )
            }

            SpacebarKeyButton(
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                uiState = uiState,
                controller = controller,
                modifier = Modifier.weight(if (showBottomEmojiKey) 3.20f else 4.10f),
            )

            CharacterKeyButton(
                spec = KeySpec(
                    id = "key_period",
                    primary = ".",
                    secondaryHint = if (uiState.inputContextMode == InputContextMode.EMAIL || uiState.inputContextMode == InputContextMode.URI) ".com" else "…",
                    longPressVariants = periodVariants,
                ),
                displayLabel = ".",
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                uiState = uiState,
                controller = controller,
                isModifierStyle = true,
                isLeftEdge = false,
                isRightEdge = true,
                isTopRow = false,
                modifier = Modifier.weight(if (showBottomEmojiKey) 0.90f else 0.95f),
            )

            SmartActionKeyButton(
                slotHeight = keySlotHeight,
                actionType = uiState.imeActionType,
                isSearchMode = uiState.isEmojiSearchMode,
                palette = palette,
                settings = settings,
                controller = controller,
                modifier = Modifier.weight(1.55f),
            )
        }
    }
}

@Composable
private fun NumericPadGrid(
    keySlotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        KeyboardLayouts.numericPadRows.forEachIndexed { rowIdx, rowKeys ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowKeys.forEachIndexed { colIdx, key ->
                    CharacterKeyButton(
                        spec = key,
                        displayLabel = key.primary,
                        slotHeight = keySlotHeight,
                        palette = palette,
                        settings = settings,
                        uiState = uiState,
                        controller = controller,
                        isModifierStyle = colIdx == 3 || rowIdx == 3 && colIdx != 1,
                        isLeftEdge = colIdx == 0,
                        isRightEdge = colIdx >= 2,
                        isTopRow = rowIdx == 0,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowIdx == 3) {
                    BackspaceKeyButton(
                        slotHeight = keySlotHeight,
                        palette = palette,
                        settings = settings,
                        controller = controller,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            ModifierLabelKeyButton(
                label = "ABC",
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                controller = controller,
                modifier = Modifier.weight(1f),
                onClick = { controller.switchToAlphaFromNumeric() },
            )
            SpacebarKeyButton(
                slotHeight = keySlotHeight,
                palette = palette,
                settings = settings,
                uiState = uiState,
                controller = controller,
                modifier = Modifier.weight(2f),
            )
            SmartActionKeyButton(
                slotHeight = keySlotHeight,
                actionType = uiState.imeActionType,
                isSearchMode = false,
                palette = palette,
                settings = settings,
                controller = controller,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CharacterKeyButton(
    spec: KeySpec,
    displayLabel: String,
    slotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
    isModifierStyle: Boolean = false,
    isLeftEdge: Boolean = false,
    isRightEdge: Boolean = false,
    isTopRow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "key_press_scale",
    )

    val activePopup = uiState.popupState?.takeIf { it.keyId == spec.id }
    val variantsToUse = remember(spec.longPressVariants, uiState.shiftState, uiState.activeLayer) {
        if (uiState.activeLayer == KeyboardLayer.ALPHA && uiState.shiftState != ShiftState.OFF) {
            spec.longPressVariants.map { it.uppercase() }
        } else {
            spec.longPressVariants
        }
    }

    // Zero dead-zone touch slot wrapping visual padded glass keycap
    Box(
        modifier = modifier
            .height(slotHeight)
            .pointerInput(spec.id, variantsToUse, displayLabel, isRightEdge) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    controller.performHaptic(view, settings)
                    if (settings.showKeyPopups) {
                        controller.showKeyPreview(spec.id, displayLabel, variantsToUse)
                    }

                    var longPressTriggered = false
                    val longPressJob = scope.launch {
                        delay(300L)
                        if (variantsToUse.isNotEmpty()) {
                            longPressTriggered = true
                            controller.expandKeyVariants(spec.id)
                            controller.performHaptic(view, settings, isCursorTick = true)
                        }
                    }

                    val cellWidthPx = with(density) { 34.dp.toPx() }
                    var releasedCleanly = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) {
                            releasedCleanly = change != null
                            break
                        }
                        if (longPressTriggered && variantsToUse.isNotEmpty()) {
                            val dx = change.position.x - down.position.x
                            val idx = if (isRightEdge) {
                                // Dragging left selects variants when key sits on right edge
                                val stepsLeft = (-dx / cellWidthPx).roundToInt()
                                (variantsToUse.lastIndex - stepsLeft).coerceIn(0, variantsToUse.lastIndex)
                            } else {
                                val stepsRight = (dx / cellWidthPx).roundToInt()
                                stepsRight.coerceIn(0, variantsToUse.lastIndex)
                            }
                            controller.updateHoveredVariant(idx)
                        }
                    }

                    longPressJob.cancel()
                    isPressed = false

                    if (releasedCleanly) {
                        val committedVariant = controller.commitActiveVariantOrDismiss(settings)
                        if (!committedVariant) {
                            controller.onKeyTap(displayLabel, settings)
                        }
                    } else {
                        controller.dismissKeyPreview()
                    }
                }
            }
            .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
            .testTag("ime_key_${spec.id}"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .liquidGlassKeycap(
                    palette = palette,
                    keycapStyle = settings.keycapStyle,
                    isModifier = isModifierStyle,
                    isPressed = isPressed,
                    borderSoftness = settings.glassBorderSoftness,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (spec.secondaryHint != null && !(settings.showNumberRow && spec.secondaryHint.all { it.isDigit() })) {
                Text(
                    text = spec.secondaryHint,
                    color = palette.keySubText.copy(alpha = 0.72f),
                    fontSize = 8.5.sp,
                    fontFamily = PlusJakartaSansFamily,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 2.dp, end = 4.dp),
                )
            }
            Text(
                text = displayLabel,
                color = palette.keyText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PlusJakartaSansFamily,
                textAlign = TextAlign.Center,
            )
        }

        // Animated iOS Liquid Glass Key Preview & Smart Edge-Aware Diacritic Fan (Clamped to Screen)
        if (activePopup != null) {
            val yOffsetPx = with(density) {
                if (isTopRow) (-slotHeight + 4.dp).roundToPx() else (-slotHeight - 8.dp).roundToPx()
            }
            val xOffsetPx = with(density) {
                when {
                    isLeftEdge -> 6.dp.roundToPx()
                    isRightEdge -> (-6).dp.roundToPx()
                    else -> 0
                }
            }
            val popupAlignment = when {
                activePopup.isExpandedVariants && isRightEdge -> Alignment.TopEnd
                activePopup.isExpandedVariants && isLeftEdge -> Alignment.TopStart
                isLeftEdge -> Alignment.TopStart
                isRightEdge -> Alignment.TopEnd
                else -> Alignment.TopCenter
            }
            Popup(
                alignment = popupAlignment,
                offset = IntOffset(xOffsetPx, yOffsetPx),
                properties = PopupProperties(focusable = false, clippingEnabled = false),
            ) {
                var shown by remember { mutableStateOf(false) }
                LaunchedEffect(activePopup.keyId) { shown = true }
                val popupScale by animateFloatAsState(
                    targetValue = if (shown) 1f else 0.82f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
                    label = "popup_scale",
                )

                if (activePopup.isExpandedVariants && activePopup.variants.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .scale(popupScale)
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.popupSurface)
                            .border(1.dp, palette.popupBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 6.dp, vertical = 5.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val displayList = if (isRightEdge) activePopup.variants.reversed() else activePopup.variants
                        displayList.forEachIndexed { visualIdx, variant ->
                            val logicalIdx = if (isRightEdge) activePopup.variants.lastIndex - visualIdx else visualIdx
                            val isSelected = logicalIdx == activePopup.selectedIndex
                            Box(
                                modifier = Modifier
                                    .size(width = 34.dp, height = 38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) palette.accent else Color.Transparent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = variant,
                                    color = if (isSelected) palette.onAccent else palette.keyText,
                                    fontSize = if (variant.length > 2) 12.sp else 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .scale(popupScale)
                            .size(width = 48.dp, height = 52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(palette.popupSurface)
                            .border(1.dp, palette.popupBorder, RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = activePopup.baseLabel,
                            color = palette.keyText,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShiftKeyButton(
    slotHeight: Dp,
    shiftState: ShiftState,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val isActive = shiftState != ShiftState.OFF
    Box(
        modifier = modifier
            .height(slotHeight)
            .clickable {
                controller.performHaptic(view, settings)
                controller.onShiftTap()
            }
            .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
            .testTag("ime_key_shift"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .liquidGlassKeycap(
                    palette = palette,
                    keycapStyle = settings.keycapStyle,
                    isModifier = !isActive,
                    isAccent = shiftState == ShiftState.CAPS_LOCK,
                    borderSoftness = settings.glassBorderSoftness,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = when (shiftState) {
                        ShiftState.OFF -> "Shift off"
                        ShiftState.SHIFTED -> "Shift active"
                        ShiftState.CAPS_LOCK -> "Caps lock active"
                    },
                    tint = when (shiftState) {
                        ShiftState.CAPS_LOCK -> palette.onAccent
                        ShiftState.SHIFTED -> palette.accent
                        ShiftState.OFF -> palette.keyText
                    },
                    modifier = Modifier.size(19.dp),
                )
                if (shiftState == ShiftState.CAPS_LOCK) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .height(2.5.dp)
                            .clip(CircleShape)
                            .background(palette.onAccent),
                    )
                }
            }
        }
    }
}

@Composable
private fun BackspaceKeyButton(
    slotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(slotHeight)
            .pointerInput(settings.backspaceSwipeDelete, settings.hapticFeedback) {
                val swipeThreshold = with(density) { 28.dp.toPx() }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    controller.performHaptic(view, settings)
                    controller.startBackspaceRepeat(view, settings)

                    var swipedLeft = false
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) break
                        val dx = change.position.x - down.position.x
                        if (!swipedLeft && dx < -swipeThreshold && settings.backspaceSwipeDelete) {
                            swipedLeft = true
                            controller.onBackspaceSwipeLeft(settings)
                            controller.performHaptic(view, settings, isCursorTick = true)
                        }
                    }

                    isPressed = false
                    controller.stopBackspaceRepeatAndTapIfNeeded(settings, swipedLeft)
                }
            }
            .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
            .testTag("ime_key_backspace"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .liquidGlassKeycap(
                    palette = palette,
                    keycapStyle = settings.keycapStyle,
                    isModifier = true,
                    isPressed = isPressed,
                    borderSoftness = settings.glassBorderSoftness,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = "Backspace (swipe left to delete word)",
                tint = palette.keyText,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun SpacebarKeyButton(
    slotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val density = LocalDensity.current
    var isPressed by remember { mutableStateOf(false) }

    if (uiState.speechState.isListening) {
        // Spacebar Morphs into the Liquid Audio Waveform Bar!
        Box(
            modifier = modifier
                .height(slotHeight)
                .clickable {
                    controller.performHaptic(view, settings)
                    controller.stopVoiceTyping()
                }
                .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
                .testTag("ime_key_space_morph_listening"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlassKeycap(
                        palette = palette,
                        keycapStyle = settings.keycapStyle,
                        isModifier = false,
                        isAccent = true,
                        borderSoftness = settings.glassBorderSoftness,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                LiquidWaveformMorphContent(
                    palette = palette,
                    rmsDb = uiState.speechState.rmsDb,
                    partialText = uiState.speechState.partialText,
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .height(slotHeight)
                .pointerInput(settings.spacebarCursorControl, settings.doubleSpacePeriod, settings.voiceHoldDurationSec) {
                    val dragThresholdPx = with(density) { 10.dp.toPx() }
                    val stepPx = with(density) { 14.dp.toPx() }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        isPressed = true
                        var draggingCursor = false
                        var holdTriggered = false

                        // Continuous 3s (or slider-set duration) hold-to-voice timer
                        controller.startSpacebarHoldTimer(settings) {
                            holdTriggered = true
                            controller.performHaptic(view, settings, isCursorTick = true)
                            controller.startVoiceTyping(settings)
                        }

                        var lastX = down.position.x
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break
                            val totalDx = kotlin.math.abs(change.position.x - down.position.x)
                            if (!draggingCursor && totalDx > dragThresholdPx && settings.spacebarCursorControl) {
                                draggingCursor = true
                                controller.cancelSpacebarHoldTimer()
                            }
                            if (draggingCursor) {
                                val stepDelta = change.position.x - lastX
                                controller.onSpacebarDrag(stepDelta, stepPx, view, settings)
                            }
                            lastX = change.position.x
                        }

                        isPressed = false
                        if (draggingCursor) {
                            controller.onSpacebarDragEnd(settings)
                        } else if (!holdTriggered) {
                            controller.cancelSpacebarHoldTimer()
                            controller.performHaptic(view, settings)
                            controller.onSpaceTap(settings)
                        }
                    }
                }
                .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
                .testTag("ime_key_space"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlassKeycap(
                        palette = palette,
                        keycapStyle = settings.keycapStyle,
                        isModifier = false,
                        isPressed = isPressed,
                        borderSoftness = settings.glassBorderSoftness,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (uiState.speechState.isHolding && uiState.speechState.holdProgress > 0f) {
                    // Luminous Liquid Cyan progress fill during Spacebar hold
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(uiState.speechState.holdProgress)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(palette.accent),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Hold for Ramble...",
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFamily,
                        )
                    }
                } else if (uiState.cursorScrubActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("‹", color = palette.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Cursor Glide",
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = PlusJakartaSansFamily,
                        )
                        Text("›", color = palette.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = palette.keySubText.copy(alpha = 0.72f),
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            text = "Vetro",
                            color = palette.keySubText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PlusJakartaSansFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidWaveformMorphContent(
    palette: GlassThemePalette,
    rmsDb: Float,
    partialText: String,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val barCount = 5
        // Left bouncing bars
        for (i in 0 until barCount) {
            val wave = kotlin.math.sin(phase + (i * 0.9f)).toFloat()
            val amp = ((rmsDb * 18f) + (wave * 4f) + 6f).coerceIn(4f, 26f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.8.dp)
                    .width(3.dp)
                    .height(amp.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(palette.onAccent),
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = if (partialText.isNotBlank()) partialText else "Listening... (tap to finish)",
            color = palette.onAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PlusJakartaSansFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Right bouncing bars
        for (i in 0 until barCount) {
            val wave = kotlin.math.cos(phase + (i * 0.9f)).toFloat()
            val amp = ((rmsDb * 18f) + (wave * 4f) + 6f).coerceIn(4f, 26f)
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.8.dp)
                    .width(3.dp)
                    .height(amp.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(palette.onAccent),
            )
        }
    }
}

@Composable
private fun ModifierLabelKeyButton(
    label: String,
    slotHeight: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isActive: Boolean = false,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .height(slotHeight)
            .clickable {
                controller.performHaptic(view, settings)
                onClick()
            }
            .padding(horizontal = 2.2.dp, vertical = 2.5.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .liquidGlassKeycap(
                    palette = palette,
                    keycapStyle = settings.keycapStyle,
                    isModifier = !isActive,
                    isAccent = isActive,
                    borderSoftness = settings.glassBorderSoftness,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) palette.onAccent else palette.keyText,
                    modifier = Modifier.size(19.dp),
                )
            } else {
                Text(
                    text = label,
                    color = if (isActive) palette.onAccent else palette.keyText,
                    fontSize = if (label.length == 1 && label.first().code > 255) 15.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PlusJakartaSansFamily,
                )
            }
        }
    }
}

@Composable
private fun SmartActionKeyButton(
    slotHeight: Dp,
    actionType: ImeActionType,
    isSearchMode: Boolean,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val icon = when {
        isSearchMode || actionType == ImeActionType.SEARCH -> Icons.Rounded.Search
        actionType == ImeActionType.SEND -> Icons.AutoMirrored.Rounded.Send
        actionType == ImeActionType.GO -> Icons.AutoMirrored.Rounded.ArrowForward
        actionType == ImeActionType.NEXT -> Icons.AutoMirrored.Rounded.ArrowForward
        actionType == ImeActionType.DONE -> Icons.Rounded.Check
        else -> Icons.AutoMirrored.Rounded.KeyboardReturn
    }

    Box(
        modifier = modifier
            .height(slotHeight)
            .clickable {
                controller.performHaptic(view, settings)
                controller.onEnterTap(settings)
            }
            .padding(horizontal = 2.2.dp, vertical = 2.5.dp)
            .testTag("ime_key_enter"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .liquidGlassKeycap(
                    palette = palette,
                    keycapStyle = settings.keycapStyle,
                    isAccent = true,
                    borderSoftness = settings.glassBorderSoftness,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = actionType.name,
                tint = palette.onAccent,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}
