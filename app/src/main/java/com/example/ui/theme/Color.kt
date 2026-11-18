package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.GlassThemePreset

// Brand & Material 3 Accent Tokens
val VetroIcePrimary = Color(0xFF0A84FF)
val VetroIceOnPrimary = Color(0xFFFFFFFF)
val VetroIcePrimaryContainer = Color(0xFFD6EAFF)
val VetroIceOnPrimaryContainer = Color(0xFF002B5C)

val VetroCyanSecondary = Color(0xFF06B6D4)
val VetroCyanContainer = Color(0xFFCFFAFE)
val VetroOnCyanContainer = Color(0xFF083344)

val VetroSlateBackground = Color(0xFFF3F8FC)
val VetroSlateSurface = Color(0xFFFFFFFF)
val VetroSlateInk = Color(0xFF0F172A)
val VetroSlateMuted = Color(0xFF475569)

val VetroDarkBackground = Color(0xFF09111E)
val VetroDarkSurface = Color(0xFF111C2D)
val VetroDarkInk = Color(0xFFF1F5F9)
val VetroDarkMuted = Color(0xFF94A3B8)

val CustomAccentSwatches: List<Pair<String, Long>> = listOf(
    "iOS Blue" to 0xFF0A84FF,
    "Cyan Prism" to 0xFF06B6D4,
    "Violet Aura" to 0xFF7C3AED,
    "Emerald" to 0xFF10B981,
    "Rose Quartz" to 0xFFE11D48,
    "Amber Sun" to 0xFFF59E0B,
)

/**
 * Semantic palette defining the multi-layered iOS-inspired Liquid Glass visual appearance
 * for both the floating keyboard capsule and in-app glass surfaces.
 */
data class GlassThemePalette(
    val isLight: Boolean,
    val shellTopTint: Color,
    val shellBottomTint: Color,
    val shellRimHighlight: Color,
    val shellBottomRim: Color,
    val shellOuterShadow: Color,
    val keySurfaceTop: Color,
    val keySurfaceBottom: Color,
    val modifierKeyTop: Color,
    val modifierKeyBottom: Color,
    val keyRimHighlight: Color,
    val keyBottomLip: Color,
    val keyPressGlow: Color,
    val keyText: Color,
    val keySubText: Color,
    val accent: Color,
    val onAccent: Color,
    val stripSurface: Color,
    val popupSurface: Color,
    val popupBorder: Color,
    val activeChipBg: Color,
)

fun resolveGlassPalette(
    preset: GlassThemePreset,
    isSystemDark: Boolean,
    glassOpacity: Float,
    specularIntensity: Float,
    customAccentArgb: Long? = null,
): GlassThemePalette {
    val effectivePreset = when (preset) {
        GlassThemePreset.ADAPTIVE_AUTO ->
            if (isSystemDark) GlassThemePreset.OBSIDIAN_GLASS else GlassThemePreset.CRYSTAL_FROST
        else -> preset
    }

    val clampedOpacity = glassOpacity.coerceIn(0.42f, 0.96f)
    val rimAlpha = (0.35f + 0.58f * specularIntensity).coerceIn(0.22f, 0.96f)
    val overrideAccent = customAccentArgb?.let { Color(it.toInt()) }

    val base = when (effectivePreset) {
        GlassThemePreset.CRYSTAL_FROST, GlassThemePreset.ADAPTIVE_AUTO -> GlassThemePalette(
            isLight = true,
            shellTopTint = Color(0xFFF6FBFF).copy(alpha = clampedOpacity),
            shellBottomTint = Color(0xFFD9E9FA).copy(alpha = (clampedOpacity - 0.08f).coerceAtLeast(0.36f)),
            shellRimHighlight = Color.White.copy(alpha = rimAlpha),
            shellBottomRim = Color(0xFF93C5FD).copy(alpha = rimAlpha * 0.40f),
            shellOuterShadow = Color(0xFF0F2942).copy(alpha = 0.24f),
            keySurfaceTop = Color.White.copy(alpha = (clampedOpacity + 0.10f).coerceAtMost(0.95f)),
            keySurfaceBottom = Color(0xFFE8F2FC).copy(alpha = clampedOpacity),
            modifierKeyTop = Color(0xFFD6E6F7).copy(alpha = clampedOpacity),
            modifierKeyBottom = Color(0xFFBED4EC).copy(alpha = (clampedOpacity - 0.04f).coerceAtLeast(0.35f)),
            keyRimHighlight = Color.White.copy(alpha = rimAlpha),
            keyBottomLip = Color(0xFF64748B).copy(alpha = 0.16f),
            keyPressGlow = Color(0xFF38BDF8).copy(alpha = 0.30f),
            keyText = Color(0xFF0F172A),
            keySubText = Color(0xFF475569),
            accent = Color(0xFF0A84FF),
            onAccent = Color.White,
            stripSurface = Color.White.copy(alpha = (clampedOpacity * 0.62f).coerceIn(0.24f, 0.72f)),
            popupSurface = Color(0xFFFBFDFF).copy(alpha = 0.97f),
            popupBorder = Color.White.copy(alpha = 0.94f),
            activeChipBg = Color(0xFF0A84FF).copy(alpha = 0.14f),
        )

        GlassThemePreset.OBSIDIAN_GLASS -> GlassThemePalette(
            isLight = false,
            shellTopTint = Color(0xFF182438).copy(alpha = clampedOpacity),
            shellBottomTint = Color(0xFF09101D).copy(alpha = (clampedOpacity + 0.04f).coerceAtMost(0.96f)),
            shellRimHighlight = Color(0xFFBAE6FD).copy(alpha = rimAlpha * 0.50f),
            shellBottomRim = Color(0xFF38BDF8).copy(alpha = rimAlpha * 0.22f),
            shellOuterShadow = Color.Black.copy(alpha = 0.48f),
            keySurfaceTop = Color(0xFF2E3F5A).copy(alpha = clampedOpacity),
            keySurfaceBottom = Color(0xFF1B273B).copy(alpha = clampedOpacity),
            modifierKeyTop = Color(0xFF1E2B3F).copy(alpha = clampedOpacity),
            modifierKeyBottom = Color(0xFF131D2D).copy(alpha = clampedOpacity),
            keyRimHighlight = Color(0xFFE0F2FE).copy(alpha = rimAlpha * 0.38f),
            keyBottomLip = Color.Black.copy(alpha = 0.38f),
            keyPressGlow = Color(0xFF38BDF8).copy(alpha = 0.34f),
            keyText = Color(0xFFF8FAFC),
            keySubText = Color(0xFF94A3B8),
            accent = Color(0xFF38BDF8),
            onAccent = Color(0xFF041C2C),
            stripSurface = Color(0xFF1E2C42).copy(alpha = 0.68f),
            popupSurface = Color(0xFF24344D).copy(alpha = 0.97f),
            popupBorder = Color(0xFF7DD3FC).copy(alpha = 0.45f),
            activeChipBg = Color(0xFF38BDF8).copy(alpha = 0.22f),
        )

        GlassThemePreset.AURORA_PRISM -> GlassThemePalette(
            isLight = true,
            shellTopTint = Color(0xFFE5F8FF).copy(alpha = clampedOpacity),
            shellBottomTint = Color(0xFFEDE2FF).copy(alpha = (clampedOpacity - 0.05f).coerceAtLeast(0.38f)),
            shellRimHighlight = Color.White.copy(alpha = rimAlpha),
            shellBottomRim = Color(0xFFA5F3FC).copy(alpha = rimAlpha * 0.55f),
            shellOuterShadow = Color(0xFF2E1065).copy(alpha = 0.22f),
            keySurfaceTop = Color(0xFFFCFEFF).copy(alpha = (clampedOpacity + 0.08f).coerceAtMost(0.95f)),
            keySurfaceBottom = Color(0xFFE9F3FF).copy(alpha = clampedOpacity),
            modifierKeyTop = Color(0xFFD1EDFF).copy(alpha = clampedOpacity),
            modifierKeyBottom = Color(0xFFDACBFF).copy(alpha = clampedOpacity),
            keyRimHighlight = Color.White.copy(alpha = rimAlpha),
            keyBottomLip = Color(0xFF6366F1).copy(alpha = 0.16f),
            keyPressGlow = Color(0xFF06B6D4).copy(alpha = 0.32f),
            keyText = Color(0xFF1E1B4B),
            keySubText = Color(0xFF4338CA),
            accent = Color(0xFF06B6D4),
            onAccent = Color.White,
            stripSurface = Color(0xFFF0FAFF).copy(alpha = 0.64f),
            popupSurface = Color(0xFFF8FCFF).copy(alpha = 0.97f),
            popupBorder = Color(0xFFA5F3FC).copy(alpha = 0.88f),
            activeChipBg = Color(0xFF06B6D4).copy(alpha = 0.16f),
        )

        GlassThemePreset.ROSE_QUARTZ -> GlassThemePalette(
            isLight = true,
            shellTopTint = Color(0xFFFFF2F5).copy(alpha = clampedOpacity),
            shellBottomTint = Color(0xFFFCE0EB).copy(alpha = (clampedOpacity - 0.05f).coerceAtLeast(0.38f)),
            shellRimHighlight = Color.White.copy(alpha = rimAlpha),
            shellBottomRim = Color(0xFFFDA4AF).copy(alpha = rimAlpha * 0.45f),
            shellOuterShadow = Color(0xFF881337).copy(alpha = 0.19f),
            keySurfaceTop = Color.White.copy(alpha = (clampedOpacity + 0.08f).coerceAtMost(0.95f)),
            keySurfaceBottom = Color(0xFFFFECF1).copy(alpha = clampedOpacity),
            modifierKeyTop = Color(0xFFFAD7E4).copy(alpha = clampedOpacity),
            modifierKeyBottom = Color(0xFFF5C0D4).copy(alpha = clampedOpacity),
            keyRimHighlight = Color.White.copy(alpha = rimAlpha),
            keyBottomLip = Color(0xFF9F1239).copy(alpha = 0.15f),
            keyPressGlow = Color(0xFFFB7185).copy(alpha = 0.32f),
            keyText = Color(0xFF3F1124),
            keySubText = Color(0xFF883955),
            accent = Color(0xFFE11D48),
            onAccent = Color.White,
            stripSurface = Color(0xFFFFF7F9).copy(alpha = 0.66f),
            popupSurface = Color(0xFFFFFAFC).copy(alpha = 0.97f),
            popupBorder = Color(0xFFFDA4AF).copy(alpha = 0.80f),
            activeChipBg = Color(0xFFE11D48).copy(alpha = 0.14f),
        )
    }

    return if (overrideAccent != null) {
        base.copy(
            accent = overrideAccent,
            onAccent = Color.White,
            keyPressGlow = overrideAccent.copy(alpha = 0.30f),
            activeChipBg = overrideAccent.copy(alpha = if (base.isLight) 0.14f else 0.24f),
        )
    } else {
        base
    }
}
