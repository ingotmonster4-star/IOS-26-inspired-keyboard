package com.example.ui.keyboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Dock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmojiCatalog
import com.example.data.EmojiItem
import com.example.ime.KeyboardController
import com.example.ime.KeyboardUiState
import com.example.model.KeyboardPanel
import com.example.model.KeyboardSettings
import com.example.model.OneHandedMode
import com.example.ui.theme.GlassThemePalette
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlusJakartaSansFamily

@Composable
fun ToolsHubDrawerContent(
    height: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
) {
    val view = LocalView.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Vetro Control Center",
                color = palette.keyText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PlusJakartaSansFamily,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.stripSurface)
                        .clickable {
                            controller.performHaptic(view, settings)
                            controller.cycleEmojiPlacement(settings.emojiKeyPlacement)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("ime_hub_cycle_emoji"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mood,
                        contentDescription = "Cycle Emoji icon placement",
                        tint = palette.keyText,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = settings.emojiKeyPlacement.shortLabel,
                        color = palette.keyText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.stripSurface)
                        .clickable {
                            controller.performHaptic(view, settings)
                            controller.togglePanel(KeyboardPanel.KEYBOARD)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("ime_tools_back"),
                ) {
                    Text("Keyboard", color = palette.accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ControlCenterTile(
                    icon = Icons.Rounded.Mood,
                    title = "Emoji",
                    subtitle = "All categories",
                    palette = palette,
                    settings = settings,
                    isHighlighted = true,
                    modifier = Modifier.weight(1f).testTag("ime_hub_emoji"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.togglePanel(KeyboardPanel.EMOJI)
                    },
                )
                ControlCenterTile(
                    icon = Icons.Rounded.Mic,
                    title = "Ramble",
                    subtitle = "Voice typing",
                    palette = palette,
                    settings = settings,
                    isHighlighted = controller.speechState.value.isListening,
                    modifier = Modifier.weight(1f).testTag("ime_hub_mic"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.toggleVoiceTyping(settings)
                    },
                )
                ControlCenterTile(
                    icon = Icons.Rounded.StayCurrentPortrait,
                    title = "1-Handed",
                    subtitle = settings.oneHandedMode.displayName,
                    palette = palette,
                    settings = settings,
                    isHighlighted = settings.oneHandedMode != OneHandedMode.OFF,
                    modifier = Modifier.weight(1f).testTag("ime_hub_onehanded"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.cycleOneHandedMode(settings.oneHandedMode)
                    },
                )
                ControlCenterTile(
                    icon = Icons.Rounded.OpenWith,
                    title = "Edit Pad",
                    subtitle = "Cursor glide",
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.weight(1f).testTag("ime_hub_edit"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.togglePanel(KeyboardPanel.EDIT_PAD)
                    },
                )
            }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ControlCenterTile(
                    icon = Icons.Rounded.ContentPaste,
                    title = "Clips",
                    subtitle = "${settings.clipboardHistory.size} saved",
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.weight(1f).testTag("ime_hub_clips"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.togglePanel(KeyboardPanel.CLIPBOARD)
                    },
                )
                ControlCenterTile(
                    icon = Icons.Rounded.Palette,
                    title = "Theme",
                    subtitle = settings.themePreset.displayName.substringBefore(" "),
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.weight(1f).testTag("ime_hub_theme"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.cycleThemePreset(settings.themePreset)
                    },
                )
                ControlCenterTile(
                    icon = if (settings.floatingMode) Icons.Rounded.CropFree else Icons.Rounded.Dock,
                    title = if (settings.floatingMode) "Floating" else "Docked",
                    subtitle = "Capsule mode",
                    palette = palette,
                    settings = settings,
                    isHighlighted = settings.floatingMode,
                    modifier = Modifier.weight(1f).testTag("ime_hub_float"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.toggleFloatingMode(settings.floatingMode)
                    },
                )
                ControlCenterTile(
                    icon = Icons.Rounded.Settings,
                    title = "Studio",
                    subtitle = "Sliders & Haptics",
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.weight(1f).testTag("ime_hub_studio"),
                    onClick = {
                        controller.performHaptic(view, settings)
                        controller.openSettingsApp()
                    },
                )
            }
        }
    }
}

@Composable
private fun ControlCenterTile(
    title: String,
    subtitle: String,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    badgeText: String? = null,
    isHighlighted: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .liquidGlassKeycap(
                palette = palette,
                keycapStyle = settings.keycapStyle,
                isModifier = !isHighlighted,
                isAccent = isHighlighted,
                borderSoftness = settings.glassBorderSoftness,
                cornerRadius = 14.dp,
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (badgeText != null) {
            Text(
                text = badgeText,
                color = if (isHighlighted) palette.onAccent else palette.keyText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isHighlighted) palette.onAccent else palette.keyText,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = title,
            color = if (isHighlighted) palette.onAccent else palette.keyText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            color = if (isHighlighted) palette.onAccent.copy(alpha = 0.85f) else palette.keySubText,
            fontSize = 9.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiDrawerContent(
    height: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
) {
    val view = LocalView.current
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showEmoticonsTab by remember { mutableStateOf(false) }
    var activeSkinToneItem by remember { mutableStateOf<EmojiItem?>(null) }

    val searchQuery = uiState.emojiSearchQuery

    val currentEmojis = remember(selectedCategoryIndex, searchQuery, settings.recentEmojis) {
        if (searchQuery.isNotBlank()) {
            EmojiCatalog.search(searchQuery, limit = 160)
        } else if (selectedCategoryIndex == 0) {
            val recents = settings.recentEmojis.map { glyph ->
                EmojiCatalog.findByGlyph(glyph) ?: EmojiItem(glyph, glyph)
            }
            (recents + EmojiCatalog.categories[0].emojis).distinctBy { it.emoji }
        } else {
            EmojiCatalog.categories.getOrElse(selectedCategoryIndex - 1) { EmojiCatalog.categories[0] }.emojis
        }
    }

    val categoryLabel = remember(selectedCategoryIndex, searchQuery, showEmoticonsTab) {
        when {
            searchQuery.isNotBlank() -> "Matches for \"$searchQuery\" (${currentEmojis.size})"
            showEmoticonsTab -> "Emoticons"
            selectedCategoryIndex == 0 -> "Recent & Smileys"
            else -> EmojiCatalog.categories.getOrElse(selectedCategoryIndex - 1) { EmojiCatalog.categories[0] }.title
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        // Top Search + Emoji/Emoticon Mode Switch + Backspace + ABC
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(palette.stripSurface)
                    .clickable { controller.enterEmojiSearchKeyboardMode() }
                    .padding(horizontal = 10.dp)
                    .testTag("emoji_search_input"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search emojis",
                    tint = palette.keySubText,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (searchQuery.isEmpty()) "Search ${EmojiCatalog.totalCount}+ emojis..." else searchQuery,
                    color = if (searchQuery.isEmpty()) palette.keySubText else palette.keyText,
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSansFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear emoji search",
                        tint = palette.keySubText,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { controller.clearEmojiSearchQuery() },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showEmoticonsTab) palette.accent else palette.stripSurface)
                    .clickable {
                        controller.performHaptic(view, settings)
                        showEmoticonsTab = !showEmoticonsTab
                        activeSkinToneItem = null
                    }
                    .padding(horizontal = 9.dp)
                    .testTag("emoji_emoticons_toggle"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (showEmoticonsTab) "😀" else ":-)",
                    color = if (showEmoticonsTab) palette.onAccent else palette.keyText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = JetBrainsMonoFamily,
                )
            }

            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.modifierKeyTop)
                    .clickable {
                        controller.performHaptic(view, settings)
                        controller.onBackspaceTap(settings)
                    }
                    .testTag("emoji_backspace_btn"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = palette.keyText,
                    modifier = Modifier.size(17.dp),
                )
            }

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.modifierKeyTop)
                    .clickable {
                        controller.performHaptic(view, settings)
                        controller.togglePanel(KeyboardPanel.KEYBOARD)
                    }
                    .padding(horizontal = 10.dp)
                    .testTag("emoji_back_abc"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ABC",
                    color = palette.keyText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PlusJakartaSansFamily,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Long-press Skin Tone Picker Bar OR Category Tabs
        val skinItem = activeSkinToneItem
        if (skinItem != null) {
            val tones = remember(skinItem.emoji) { EmojiCatalog.skinToneVariants(skinItem.emoji) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.popupSurface)
                    .border(1.dp, palette.popupBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tones.forEach { variant ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                controller.performHaptic(view, settings)
                                controller.onEmojiTap(variant, settings)
                                activeSkinToneItem = null
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = variant, fontSize = 22.sp)
                    }
                }
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close skin tone picker",
                    tint = palette.keySubText,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { activeSkinToneItem = null },
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        } else if (!showEmoticonsTab && searchQuery.isBlank()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
            ) {
                item {
                    val isSelected = selectedCategoryIndex == 0
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) palette.accent else palette.stripSurface)
                            .clickable {
                                controller.performHaptic(view, settings, true)
                                selectedCategoryIndex = 0
                            }
                            .testTag("emoji_cat_recent"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("🕒", fontSize = 14.sp)
                    }
                }
                items(EmojiCatalog.categories.size) { idx ->
                    val isSelected = selectedCategoryIndex == idx + 1
                    val cat = EmojiCatalog.categories[idx]
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) palette.accent else palette.stripSurface)
                            .clickable {
                                controller.performHaptic(view, settings, true)
                                selectedCategoryIndex = idx + 1
                            }
                            .testTag("emoji_cat_${cat.id}"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cat.icon, fontSize = 15.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
        }

        Text(
            text = categoryLabel,
            color = palette.keySubText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PlusJakartaSansFamily,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
        )

        if (showEmoticonsTab) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 86.dp),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                EmojiCatalog.emoticonGroups.forEach { group ->
                    items(group.items) { kaomoji ->
                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .liquidGlassKeycap(
                                    palette = palette,
                                    keycapStyle = settings.keycapStyle,
                                    borderSoftness = settings.glassBorderSoftness,
                                    cornerRadius = 10.dp,
                                )
                                .clickable {
                                    controller.performHaptic(view, settings)
                                    controller.onEmojiTap(kaomoji, settings, recordRecent = false)
                                }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = kaomoji,
                                color = palette.keyText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        } else if (currentEmojis.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No matching emojis found",
                    color = palette.keySubText,
                    fontSize = 12.sp,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 40.dp),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                items(currentEmojis, key = { "${selectedCategoryIndex}_${it.emoji}_${it.name}" }) { item ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .combinedClickable(
                                onClick = {
                                    activeSkinToneItem = null
                                    controller.performHaptic(view, settings)
                                    controller.onEmojiTap(item.emoji, settings)
                                },
                                onLongClick = {
                                    if (item.supportsSkinTone) {
                                        controller.performHaptic(view, settings, isCursorTick = true)
                                        activeSkinToneItem = item
                                    }
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = item.emoji, fontSize = 23.sp, textAlign = TextAlign.Center)
                        if (item.supportsSkinTone) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(3.dp)
                                    .size(3.5.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(palette.keySubText.copy(alpha = 0.45f)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPadDrawerContent(
    height: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    uiState: KeyboardUiState,
    controller: KeyboardController,
) {
    val view = LocalView.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            EditActionPill(
                label = if (uiState.isSelectionActive) "Selecting" else "Select",
                isActive = uiState.isSelectionActive,
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_edit_select_toggle"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.onToggleSelectionMode()
                },
            )
            EditIconPill(
                icon = Icons.Rounded.SelectAll,
                label = "All",
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_edit_select_all"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.onEditSelectAll()
                },
            )
            EditIconPill(
                icon = Icons.Rounded.ContentCut,
                label = "Cut",
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_edit_cut"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.onEditCut()
                },
            )
            EditIconPill(
                icon = Icons.Rounded.ContentCopy,
                label = "Copy",
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_edit_copy"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.onEditCopy()
                },
            )
            EditIconPill(
                icon = Icons.Rounded.ContentPaste,
                label = "Paste",
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_edit_paste"),
                onClick = {
                    controller.performHaptic(view, settings)
                    controller.onEditPaste(settings.clipboardHistory.firstOrNull()?.text)
                },
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EditActionPill(
                    label = "⇤ Line Start",
                    isActive = false,
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    onClick = {
                        controller.performHaptic(view, settings, true)
                        controller.onEditLineEdge(toEnd = false)
                    },
                )
                EditActionPill(
                    label = "Word ←",
                    isActive = false,
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    onClick = {
                        controller.performHaptic(view, settings, true)
                        controller.onEditMove(dx = -5)
                    },
                )
            }

            Column(
                modifier = Modifier.weight(1.3f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                        .clickable {
                            controller.performHaptic(view, settings, true)
                            controller.onEditMove(dy = -1)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", tint = palette.keyText)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                            .clickable {
                                controller.performHaptic(view, settings, true)
                                controller.onEditMove(dx = -1)
                            }
                            .testTag("ime_edit_left"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Move left", tint = palette.keyText)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                            .clickable {
                                controller.performHaptic(view, settings, true)
                                controller.onEditMove(dx = 1)
                            }
                            .testTag("ime_edit_right"),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Move right", tint = palette.keyText)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                        .clickable {
                            controller.performHaptic(view, settings, true)
                            controller.onEditMove(dy = 1)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", tint = palette.keyText)
                }
            }

            Column(
                modifier = Modifier.weight(0.9f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                EditActionPill(
                    label = "Line End ⇥",
                    isActive = false,
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    onClick = {
                        controller.performHaptic(view, settings, true)
                        controller.onEditLineEdge(toEnd = true)
                    },
                )
                EditActionPill(
                    label = "→ Word",
                    isActive = false,
                    palette = palette,
                    settings = settings,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    onClick = {
                        controller.performHaptic(view, settings, true)
                        controller.onEditMove(dx = 5)
                    },
                )
            }
        }
    }
}

@Composable
fun ClipboardDrawerContent(
    height: Dp,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    controller: KeyboardController,
) {
    val view = LocalView.current
    var showSnippetsTab by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditActionPill(
                label = "Clipboard (${settings.clipboardHistory.size})",
                isActive = !showSnippetsTab,
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_tab_clipboard"),
                onClick = { showSnippetsTab = false },
            )
            EditActionPill(
                label = "Snippets (${settings.savedSnippets.size})",
                isActive = showSnippetsTab,
                palette = palette,
                settings = settings,
                modifier = Modifier.weight(1f).testTag("ime_tab_snippets"),
                onClick = { showSnippetsTab = true },
            )
            if (!showSnippetsTab && settings.clipboardHistory.any { !it.isPinned }) {
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.stripSurface)
                        .clickable { controller.onClearUnpinnedClipboard() }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Clear",
                        color = palette.keySubText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (!showSnippetsTab) {
            if (settings.clipboardHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Copy text anywhere to see recent glass clips here",
                        color = palette.keySubText,
                        fontSize = 12.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    items(settings.clipboardHistory, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                                .clickable {
                                    controller.performHaptic(view, settings)
                                    controller.onClipboardInsert(item.text, settings)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item.text,
                                color = palette.keyText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { controller.onTogglePinClipboard(item.id) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = if (item.isPinned) "Unpin clip" else "Pin clip",
                                    tint = if (item.isPinned) palette.accent else palette.keySubText,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            IconButton(
                                onClick = { controller.onDeleteClipboard(item.id) },
                                modifier = Modifier.size(32.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete clip",
                                    tint = palette.keySubText,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                items(settings.savedSnippets, key = { it.id }) { snippet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassKeycap(palette = palette, keycapStyle = settings.keycapStyle, borderSoftness = settings.glassBorderSoftness, cornerRadius = 12.dp)
                            .clickable {
                                controller.performHaptic(view, settings)
                                controller.onClipboardInsert(snippet.content, settings)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.activeChipBg)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = snippet.label,
                                color = palette.accent,
                                fontSize = 11.sp,
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        Text(
                            text = snippet.content.replace("\n", " "),
                            color = palette.keyText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditActionPill(
    label: String,
    isActive: Boolean,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .liquidGlassKeycap(
                palette = palette,
                keycapStyle = settings.keycapStyle,
                isModifier = !isActive,
                isAccent = isActive,
                borderSoftness = settings.glassBorderSoftness,
                cornerRadius = 11.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isActive) palette.onAccent else palette.keyText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PlusJakartaSansFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EditIconPill(
    icon: ImageVector,
    label: String,
    palette: GlassThemePalette,
    settings: KeyboardSettings,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(34.dp)
            .liquidGlassKeycap(
                palette = palette,
                keycapStyle = settings.keycapStyle,
                isModifier = true,
                borderSoftness = settings.glassBorderSoftness,
                cornerRadius = 11.dp,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = palette.keyText, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            color = palette.keyText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
