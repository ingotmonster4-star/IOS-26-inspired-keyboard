package com.example.ui.app

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ime.KeyboardController
import com.example.ime.KeyboardUiState
import com.example.model.ImeActionType
import com.example.model.InputContextMode
import com.example.model.KeyboardSettings
import com.example.ui.keyboard.VetroKeyboardSurface
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.PlusJakartaSansFamily
import kotlinx.coroutines.delay

private enum class BackdropMood(val label: String) {
    ICE_FROST("Ice"),
    AURORA("Aurora"),
    MIDNIGHT("Night"),
}

@Composable
fun PlaygroundScreen(
    settings: KeyboardSettings,
    imeStatus: ImeSetupStatus,
    textValue: TextFieldValue,
    keyboardState: KeyboardUiState,
    controller: KeyboardController,
    feedbackMessage: String?,
    onClearFeedback: () -> Unit,
    onTextSelectionChanged: (TextFieldValue) -> Unit,
    onClearText: () -> Unit,
    onSampleText: () -> Unit,
    onSelectContext: (InputContextMode, ImeActionType) -> Unit,
    onOpenImePicker: () -> Unit,
    onOpenStudio: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var backdropMood by remember { mutableStateOf(BackdropMood.ICE_FROST) }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2200L)
            onClearFeedback()
        }
    }

    val isNightBackdrop = backdropMood == BackdropMood.MIDNIGHT
    val headerInk = if (isNightBackdrop) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val headerSubInk = if (isNightBackdrop) Color(0xFFCBD5E1) else Color(0xFF334155)

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_glass_backdrop),
            contentDescription = "Liquid glass refraction background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        val backdropOverlay = when (backdropMood) {
            BackdropMood.ICE_FROST -> listOf(Color(0x66F5FAFF), Color(0x33E2F1FF), Color(0x55D4E7FA))
            BackdropMood.AURORA -> listOf(Color(0x55CCFBF1), Color(0x44DDD6FE), Color(0x55BAE6FD))
            BackdropMood.MIDNIGHT -> listOf(Color(0xCC091324), Color(0xBA0F1E36), Color(0xCC060D1A))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = backdropOverlay)),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Vetro",
                            style = MaterialTheme.typography.displaySmall,
                            color = headerInk,
                        )
                        Text(
                            text = "${settings.themePreset.displayName} • ${settings.keycapStyle.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = headerSubInk,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Quick backdrop mood cycler to admire translucent refraction
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = if (isNightBackdrop) 0.16f else 0.72f))
                                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .clickable {
                                    val all = BackdropMood.entries
                                    backdropMood = all[(all.indexOf(backdropMood) + 1) % all.size]
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("playground_backdrop_btn"),
                        ) {
                            Text(
                                text = backdropMood.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = headerInk,
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = if (isNightBackdrop) 0.18f else 0.76f))
                                .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                .clickable(onClick = onOpenStudio)
                                .padding(horizontal = 11.dp, vertical = 8.dp)
                                .testTag("playground_tune_glass_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = "Customize glass",
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(15.dp),
                            )
                            Text(
                                text = "Studio",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = headerInk,
                            )
                        }
                    }
                }

                val micLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {
                    controller.speechManager?.hasRecordPermission()
                }

                SystemImeStatusCard(
                    status = imeStatus,
                    onOpenSettings = {
                        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    },
                    onSwitchKeyboard = onOpenImePicker,
                )

                if (controller.speechManager?.hasRecordPermission() == false) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = if (isNightBackdrop) 0.16f else 0.75f))
                            .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .clickable { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Voice Ramble: Grant mic access",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = headerInk,
                            )
                        }
                        Text(
                            text = "Enable",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A84FF),
                        )
                    }
                }

                // Live Interactive Frosted Editor Card
                val editorCardBg = if (isNightBackdrop) Color(0xBF152238) else Color.White.copy(alpha = 0.72f)
                val editorInk = if (isNightBackdrop) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                val editorMuted = if (isNightBackdrop) Color(0xFF94A3B8) else Color(0xFF64748B)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(editorCardBg)
                        .border(1.dp, Color.White.copy(alpha = if (isNightBackdrop) 0.25f else 0.92f), RoundedCornerShape(22.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0A84FF)),
                            )
                            val wordCount = remember(textValue.text) {
                                textValue.text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
                            }
                            Text(
                                text = "$wordCount words • ${textValue.text.length} chars",
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 10.5.sp,
                                color = editorMuted,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MiniGlassChip(
                                icon = Icons.Rounded.Refresh,
                                label = "Sample",
                                onClick = onSampleText,
                                modifier = Modifier.testTag("playground_sample_btn"),
                            )
                            MiniGlassChip(
                                icon = Icons.Rounded.DeleteSweep,
                                label = "Clear",
                                onClick = onClearText,
                                modifier = Modifier.testTag("playground_clear_btn"),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Smart Context Mode Chips (test Email @, Search 🔍, Notes ↵, 123 Pad live!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        ContextModePill(
                            label = "Notes ↵",
                            selected = keyboardState.inputContextMode == InputContextMode.TEXT &&
                                keyboardState.imeActionType == ImeActionType.ENTER,
                            onClick = { onSelectContext(InputContextMode.TEXT, ImeActionType.ENTER) },
                        )
                        ContextModePill(
                            label = "Search 🔍",
                            selected = keyboardState.imeActionType == ImeActionType.SEARCH,
                            onClick = { onSelectContext(InputContextMode.TEXT, ImeActionType.SEARCH) },
                        )
                        ContextModePill(
                            label = "Email @",
                            selected = keyboardState.inputContextMode == InputContextMode.EMAIL,
                            onClick = { onSelectContext(InputContextMode.EMAIL, ImeActionType.SEND) },
                        )
                        ContextModePill(
                            label = "URL /",
                            selected = keyboardState.inputContextMode == InputContextMode.URI,
                            onClick = { onSelectContext(InputContextMode.URI, ImeActionType.GO) },
                        )
                        ContextModePill(
                            label = "123 Pad",
                            selected = keyboardState.inputContextMode == InputContextMode.NUMBER,
                            onClick = { onSelectContext(InputContextMode.NUMBER, ImeActionType.DONE) },
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        BasicTextField(
                            value = textValue,
                            onValueChange = { updated ->
                                if (updated.text == textValue.text) {
                                    onTextSelectionChanged(updated)
                                }
                            },
                            readOnly = true,
                            textStyle = TextStyle(
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 15.5.sp,
                                lineHeight = 22.sp,
                                color = editorInk,
                            ),
                            cursorBrush = SolidColor(Color(0xFF0A84FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("playground_editor_field"),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (textValue.text.isEmpty()) {
                                        Text(
                                            text = "Type on Vetro below — open Emoji for all Unicode emojis & skin tones, drag Spacebar for Cursor Glide, hold Spacebar for Voice Ramble, or tap ⊞ for Control Center...",
                                            color = editorMuted,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                        )
                    }

                    AnimatedVisibility(visible = feedbackMessage != null) {
                        Surface(
                            color = Color(0xFF0A84FF).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(top = 6.dp),
                        ) {
                            Text(
                                text = feedbackMessage.orEmpty(),
                                color = if (isNightBackdrop) Color(0xFF7DD3FC) else Color(0xFF0369A1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            VetroKeyboardSurface(
                settings = settings,
                uiState = keyboardState,
                controller = controller,
                isSystemIme = false,
            )
        }
    }
}

@Composable
private fun ContextModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) Color(0xFF0A84FF) else Color(0xFF94A3B8).copy(alpha = 0.18f))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFF475569),
        )
    }
}

@Composable
private fun SystemImeStatusCard(
    status: ImeSetupStatus,
    onOpenSettings: () -> Unit,
    onSwitchKeyboard: () -> Unit,
) {
    val isReady = status.isEnabledInSystem && status.isSelectedAsDefault
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(1.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = if (isReady) Icons.Rounded.CheckCircle else Icons.Rounded.Keyboard,
            contentDescription = null,
            tint = if (isReady) Color(0xFF059669) else Color(0xFF0A84FF),
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when {
                    isReady -> "Vetro is active system-wide"
                    status.isEnabledInSystem -> "Step 2: Select Vetro as active keyboard"
                    else -> "Step 1: Enable Vetro in Android Settings"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    isReady -> "Ready across all apps"
                    status.isEnabledInSystem -> "Tap Switch to activate everywhere"
                    else -> "Toggle Vetro Liquid Glass Keyboard on"
                },
                fontSize = 11.sp,
                color = Color(0xFF475569),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!status.isEnabledInSystem) {
            Button(
                onClick = onOpenSettings,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                modifier = Modifier.height(34.dp).testTag("ime_setup_enable_btn"),
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enable", fontSize = 12.sp)
            }
        } else if (!status.isSelectedAsDefault) {
            Button(
                onClick = onSwitchKeyboard,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                modifier = Modifier.height(34.dp).testTag("ime_setup_switch_btn"),
            ) {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Switch", fontSize = 12.sp)
            }
        } else {
            OutlinedButton(
                onClick = onSwitchKeyboard,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(32.dp).testTag("ime_setup_picker_btn"),
            ) {
                Text("Switch", fontSize = 11.sp, color = Color(0xFF0A84FF))
            }
        }
    }
}

@Composable
private fun MiniGlassChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFE2E8F0).copy(alpha = 0.72f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = label, tint = Color(0xFF334155), modifier = Modifier.size(13.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
    }
}
