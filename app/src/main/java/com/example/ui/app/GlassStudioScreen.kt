package com.example.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import com.example.model.EmojiKeyPlacement
import com.example.model.GlassThemePreset
import com.example.model.KeyboardHeightPreset
import com.example.model.KeyboardSettings
import com.example.model.KeycapStyle
import com.example.model.OneHandedMode
import com.example.ui.keyboard.liquidGlassKeycap
import com.example.ui.keyboard.liquidGlassShell
import com.example.ui.theme.CustomAccentSwatches
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.resolveGlassPalette
import kotlin.math.roundToInt

@Composable
fun GlassStudioScreen(
    settings: KeyboardSettings,
    viewModel: VetroViewModel,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val livePalette = remember(
        settings.themePreset,
        isDark,
        settings.glassOpacity,
        settings.specularIntensity,
        settings.customAccentArgb,
    ) {
        resolveGlassPalette(
            preset = settings.themePreset,
            isSystemDark = isDark,
            glassOpacity = settings.glassOpacity,
            specularIntensity = settings.specularIntensity,
            customAccentArgb = settings.customAccentArgb,
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Glass Studio",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Craft your non-obstructive Liquid Glass refraction & Gboard ergonomics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Live Glass Capsule Mini Preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer,
                            )
                        )
                    )
                    .padding(14.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlassShell(
                            palette = livePalette,
                            shape = RoundedCornerShape(
                                if (settings.floatingMode) settings.cornerRadiusDp.dp else 12.dp
                            ),
                            elevation = 10.dp,
                            borderSoftness = settings.glassBorderSoftness,
                        )
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("Q", "W", "E", "R", "T", "Y").forEach { ch ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .liquidGlassKeycap(
                                        palette = livePalette,
                                        keycapStyle = settings.keycapStyle,
                                        borderSoftness = settings.glassBorderSoftness,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(ch, color = livePalette.keyText, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(34.dp)
                                .liquidGlassKeycap(
                                    palette = livePalette,
                                    keycapStyle = settings.keycapStyle,
                                    isModifier = true,
                                    borderSoftness = settings.glassBorderSoftness,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("?123", color = livePalette.keyText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(3.4f)
                                .height(34.dp)
                                .liquidGlassKeycap(
                                    palette = livePalette,
                                    keycapStyle = settings.keycapStyle,
                                    borderSoftness = settings.glassBorderSoftness,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Vetro • ${settings.themePreset.displayName}",
                                color = livePalette.keySubText,
                                fontSize = 11.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(34.dp)
                                .liquidGlassKeycap(
                                    palette = livePalette,
                                    keycapStyle = settings.keycapStyle,
                                    isAccent = true,
                                    borderSoftness = settings.glassBorderSoftness,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("↵", color = livePalette.onAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Theme Presets
        item {
            Text(
                text = "Liquid Glass Presets",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(GlassThemePreset.entries) { preset ->
                    val selected = settings.themePreset == preset
                    val swatch = remember(preset, isDark) {
                        resolveGlassPalette(preset, isDark, 0.85f, 0.8f)
                    }
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(18.dp),
                            )
                            .clickable { viewModel.setThemePreset(preset) }
                            .padding(12.dp)
                            .testTag("studio_preset_${preset.name.lowercase()}"),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(swatch.shellTopTint, swatch.accent))
                                    )
                                    .border(1.dp, swatch.shellRimHighlight, CircleShape),
                            )
                            Text(
                                text = preset.displayName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.subtitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Keycap Style & Custom Accent Color Creativity
        item {
            StudioSectionCard(title = "Keycap Style & Accent Color") {
                Text("Keycap Geometry", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeycapStyle.entries.forEach { style ->
                        SegmentedPill(
                            label = style.displayName,
                            selected = settings.keycapStyle == style,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setKeycapStyle(style) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Accent Refraction Color", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        val isDefault = settings.customAccentArgb == null
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isDefault) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isDefault) 1.5.dp else 1.dp,
                                    color = if (isDefault) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { viewModel.setCustomAccentArgb(null) }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Preset Auto", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(CustomAccentSwatches) { (name, argb) ->
                        val selected = settings.customAccentArgb == argb
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(argb.toInt()))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onBackground else Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                )
                                .clickable { viewModel.setCustomAccentArgb(argb) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "$name selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Glass Physics Sliders
        item {
            StudioSectionCard(title = "Glass Optics & Geometry") {
                StudioSwitchRow(
                    title = "Floating Glass Pill",
                    subtitle = "Detached capsule with rounded bottom gap for non-obstructive feel",
                    checked = settings.floatingMode,
                    tag = "studio_switch_floating",
                    onCheckedChange = { viewModel.setFloatingMode(it) },
                )

                StudioSliderRow(
                    title = "Frosted Translucency",
                    valueLabel = "${(settings.glassOpacity * 100).roundToInt()}%",
                    value = settings.glassOpacity,
                    range = 0.42f..0.96f,
                    onValueChange = { viewModel.setGlassOpacity(it) },
                )

                StudioSliderRow(
                    title = "Specular Rim Sheen",
                    valueLabel = "${(settings.specularIntensity * 100).roundToInt()}%",
                    value = settings.specularIntensity,
                    range = 0.15f..1.0f,
                    onValueChange = { viewModel.setSpecularIntensity(it) },
                )

                StudioSliderRow(
                    title = "Border Softness & Feathering",
                    valueLabel = "${(settings.glassBorderSoftness * 100).roundToInt()}%",
                    value = settings.glassBorderSoftness,
                    range = 0.0f..1.0f,
                    onValueChange = { viewModel.setGlassBorderSoftness(it) },
                )

                StudioSliderRow(
                    title = "Pill Corner Radius",
                    valueLabel = "${settings.cornerRadiusDp.roundToInt()}dp",
                    value = settings.cornerRadiusDp,
                    range = 8f..32f,
                    onValueChange = { viewModel.setCornerRadius(it) },
                )

                Text("Vertical Footprint", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeyboardHeightPreset.entries.forEach { preset ->
                        SegmentedPill(
                            label = preset.displayName,
                            selected = settings.heightPreset == preset,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setHeightPreset(preset) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("One-Handed Reach", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OneHandedMode.entries.forEach { mode ->
                        SegmentedPill(
                            label = mode.displayName,
                            selected = settings.oneHandedMode == mode,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setOneHandedMode(mode) },
                        )
                    }
                }
            }
        }

        // Feedback, Sensory Dynamics & Voice Typing
        item {
            val view = LocalView.current
            StudioSectionCard(title = "Haptics, Sound & Voice Ramble") {
                StudioSwitchRow(
                    title = "Tactile Haptic Feedback",
                    subtitle = "Vibration response on keystroke and cursor scrub",
                    checked = settings.hapticFeedback,
                    tag = "studio_switch_haptic",
                    onCheckedChange = { viewModel.setBooleanPref(hapticFeedback = it) },
                )
                if (settings.hapticFeedback) {
                    StudioSliderRow(
                        title = "Vibration Intensity",
                        valueLabel = "${(settings.hapticStrength * 100).roundToInt()}%",
                        value = settings.hapticStrength,
                        range = 0.1f..1.0f,
                        onValueChange = { viewModel.setHapticStrength(it) },
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                viewModel.playgroundController.performHaptic(view, settings)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Test Vibration Tap",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                StudioSwitchRow(
                    title = "Keypress Sound Effect",
                    subtitle = "Crisp iOS-style glass click audio on tap",
                    checked = settings.soundOnKeypress,
                    tag = "studio_switch_sound",
                    onCheckedChange = { viewModel.setSoundSettings(enabled = it) },
                )
                if (settings.soundOnKeypress) {
                    StudioSliderRow(
                        title = "Click Volume",
                        valueLabel = "${(settings.soundVolume * 100).roundToInt()}%",
                        value = settings.soundVolume,
                        range = 0.0f..1.0f,
                        onValueChange = { viewModel.setSoundSettings(volume = it) },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                StudioSliderRow(
                    title = "Spacebar Hold-to-Voice Duration",
                    valueLabel = "${String.format("%.1f", settings.voiceHoldDurationSec)}s",
                    value = settings.voiceHoldDurationSec,
                    range = 1.0f..3.5f,
                    onValueChange = { viewModel.setVoiceHoldDuration(it) },
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Emoji Key Placement", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text(
                    text = "Choose where the emoji icon sits—or switch live inside ⊞ Control Center",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    EmojiKeyPlacement.entries.forEach { placement ->
                        val isSelected = settings.emojiKeyPlacement == placement
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable { viewModel.setEmojiKeyPlacement(placement) }
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                                .testTag("studio_emoji_placement_${placement.name.lowercase()}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = placement.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = placement.description,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Gboard Ergonomics & Gestures
        item {
            StudioSectionCard(title = "Gboard Ergonomics & Smart Typing") {
                StudioSwitchRow(
                    title = "Smart Suggestions & Sentence Prediction",
                    subtitle = "Clean continuous Markov word predictions with zero symbol clutter",
                    checked = settings.showSuggestions,
                    tag = "studio_switch_suggestions",
                    onCheckedChange = { viewModel.setBooleanPref(showSuggestions = it) },
                )
                StudioSwitchRow(
                    title = "Spacebar Cursor Glide",
                    subtitle = "Drag horizontally across spacebar to move cursor smoothly",
                    checked = settings.spacebarCursorControl,
                    tag = "studio_switch_space_cursor",
                    onCheckedChange = { viewModel.setBooleanPref(spacebarCursorControl = it) },
                )
                StudioSwitchRow(
                    title = "Swipe-Left Word Delete",
                    subtitle = "Swipe left from Backspace to delete whole words with Undo chip",
                    checked = settings.backspaceSwipeDelete,
                    tag = "studio_switch_swipe_delete",
                    onCheckedChange = { viewModel.setBooleanPref(backspaceSwipeDelete = it) },
                )
                StudioSwitchRow(
                    title = "Dedicated Number Row",
                    subtitle = "Show 1–0 row above QWERTY (otherwise long-press Q–P)",
                    checked = settings.showNumberRow,
                    tag = "studio_switch_number_row",
                    onCheckedChange = { viewModel.setBooleanPref(showNumberRow = it) },
                )
                StudioSwitchRow(
                    title = "Top Utility Strip",
                    subtitle = "Slim Gboard bar with Control Center ⊞, Smart Predictions, Clips & Mic",
                    checked = settings.showUtilityStrip,
                    tag = "studio_switch_utility_strip",
                    onCheckedChange = { viewModel.setBooleanPref(showUtilityStrip = it) },
                )
                StudioSwitchRow(
                    title = "Allow Strip Collapse",
                    subtitle = "Add a collapse button to minimize top strip for ultra-clean typing",
                    checked = settings.autoCollapseStrip,
                    tag = "studio_switch_auto_collapse",
                    onCheckedChange = { viewModel.setBooleanPref(autoCollapseStrip = it) },
                )
                StudioSwitchRow(
                    title = "Floating Key Popups",
                    subtitle = "Show frosted key bubble & edge-aware diacritic wheel",
                    checked = settings.showKeyPopups,
                    tag = "studio_switch_popups",
                    onCheckedChange = { viewModel.setBooleanPref(showKeyPopups = it) },
                )
                StudioSwitchRow(
                    title = "Double-Space Period",
                    subtitle = "Tap space twice quickly to insert a period and space",
                    checked = settings.doubleSpacePeriod,
                    tag = "studio_switch_double_space",
                    onCheckedChange = { viewModel.setBooleanPref(doubleSpacePeriod = it) },
                )
                StudioSwitchRow(
                    title = "Auto-Capitalization",
                    subtitle = "Automatically shift after sentence punctuation",
                    checked = settings.autoCapitalize,
                    tag = "studio_switch_autocap",
                    onCheckedChange = { viewModel.setBooleanPref(autoCapitalize = it) },
                )
            }
        }
    }
}

@Composable
private fun StudioSectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun StudioSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(
                subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag),
        )
    }
}

@Composable
private fun StudioSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(
                text = valueLabel,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}

@Composable
private fun SegmentedPill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
