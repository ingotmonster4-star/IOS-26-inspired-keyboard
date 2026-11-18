package com.example.ui.keyboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.KeycapStyle
import com.example.ui.theme.GlassThemePalette

fun Modifier.liquidGlassShell(
    palette: GlassThemePalette,
    shape: Shape,
    elevation: Dp = 16.dp,
    borderSoftness: Float = 0.65f,
): Modifier = composed {
    this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = palette.shellOuterShadow.copy(alpha = 0.20f),
            spotColor = palette.shellOuterShadow.copy(alpha = 0.26f),
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.shellTopTint,
                    palette.shellBottomTint.copy(alpha = (palette.shellBottomTint.alpha * 0.92f)),
                ),
            )
        )
        .drawBehind {
            // Diagonal caustic reflection across upper glass shell
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        palette.shellRimHighlight.copy(alpha = palette.shellRimHighlight.alpha * (0.32f * (1f - borderSoftness * 0.3f))),
                        Color.Transparent,
                        palette.accent.copy(alpha = 0.06f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                ),
                cornerRadius = CornerRadius(32f, 32f),
            )

            // Feathered apex specular light glint at top edge (seamless gradient fade without wireframe box)
            val glintAlpha = (palette.shellRimHighlight.alpha * (1f - borderSoftness * 0.45f)).coerceIn(0.12f, 0.90f)
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        palette.shellRimHighlight.copy(alpha = glintAlpha),
                        palette.shellRimHighlight.copy(alpha = glintAlpha),
                        Color.Transparent,
                    ),
                ),
                topLeft = Offset(size.width * 0.08f, 0f),
                size = Size(size.width * 0.84f, 1.2f),
            )
        }
}

fun Modifier.liquidGlassKeycap(
    palette: GlassThemePalette,
    keycapStyle: KeycapStyle = KeycapStyle.LIQUID_PILL,
    isModifier: Boolean = false,
    isAccent: Boolean = false,
    isPressed: Boolean = false,
    borderSoftness: Float = 0.65f,
    cornerRadius: Dp? = null,
): Modifier = composed {
    val resolvedRadius = cornerRadius ?: when (keycapStyle) {
        KeycapStyle.LIQUID_PILL -> 13.dp
        KeycapStyle.SOFT_SQUIRCLE -> 8.dp
        KeycapStyle.FRAMELESS_AIR -> 14.dp
    }
    val shape = RoundedCornerShape(resolvedRadius)

    val pressGlow by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "key_press_glow",
    )

    val isAirRest = keycapStyle == KeycapStyle.FRAMELESS_AIR && !isPressed && !isAccent

    // Multi-stop diffuse gradient that smoothly blends into dark or light backgrounds
    val topColor = when {
        isAccent -> palette.accent.copy(alpha = if (isPressed) 1f else 0.92f)
        isAirRest -> if (isModifier) palette.modifierKeyTop.copy(alpha = 0.20f) else Color.Transparent
        isModifier -> palette.modifierKeyTop
        else -> palette.keySurfaceTop
    }
    val midColor = when {
        isAccent -> palette.accent.copy(alpha = if (isPressed) 0.96f else 0.86f)
        isAirRest -> Color.Transparent
        isModifier -> palette.modifierKeyBottom.copy(alpha = palette.modifierKeyBottom.alpha * 0.95f)
        else -> palette.keySurfaceBottom
    }
    val bottomColor = when {
        isAccent -> palette.accent.copy(alpha = if (isPressed) 0.92f else 0.78f)
        isAirRest -> if (isModifier) palette.modifierKeyBottom.copy(alpha = 0.14f) else Color.Transparent
        isModifier -> palette.modifierKeyBottom
        else -> palette.keySurfaceBottom.copy(alpha = (palette.keySurfaceBottom.alpha * 0.94f).coerceAtLeast(0.35f))
    }

    val keyElevation = when {
        isAirRest -> 0.dp
        isPressed -> 0.8.dp
        else -> 2.2.dp
    }

    this
        .then(
            if (keyElevation > 0.dp) {
                Modifier.shadow(
                    elevation = keyElevation,
                    shape = shape,
                    ambientColor = palette.shellOuterShadow.copy(alpha = 0.18f),
                    spotColor = palette.shellOuterShadow.copy(alpha = 0.24f),
                )
            } else {
                Modifier
            }
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(topColor, midColor, bottomColor),
            )
        )
        .drawBehind {
            // Press bloom glow
            if (pressGlow > 0.01f && !isAccent) {
                drawRect(color = palette.keyPressGlow.copy(alpha = palette.keyPressGlow.alpha * pressGlow))
            }

            // Authentic feathered specular glint along top apex (NO hard 4-sided wireframe box!)
            if (!isAirRest) {
                val glintAlpha = if (isAccent) {
                    0.65f
                } else {
                    (palette.keyRimHighlight.alpha * (1f - borderSoftness * 0.50f)).coerceIn(0.10f, 0.85f)
                }

                // Horizontal feathered glint: brightest in center, soft fade toward lateral corners
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            palette.keyRimHighlight.copy(alpha = glintAlpha),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * 0.14f, 1f),
                    end = Offset(size.width * 0.86f, 1f),
                    strokeWidth = 1.3f,
                )

                // Very soft bottom lip reflection
                val lipAlpha = (palette.keyRimHighlight.alpha * (0.22f * (1f - borderSoftness * 0.4f))).coerceIn(0.04f, 0.35f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            palette.keyRimHighlight.copy(alpha = lipAlpha),
                            Color.Transparent,
                        ),
                    ),
                    start = Offset(size.width * 0.24f, size.height - 1.2f),
                    end = Offset(size.width * 0.76f, size.height - 1.2f),
                    strokeWidth = 1.0f,
                )
            }
        }
}
