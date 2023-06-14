package dev.andrew.prosto

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.andrew.prosto.android.compose.theme.md_theme_light_background
import dev.andrew.prosto.android.compose.theme.md_theme_light_error
import dev.andrew.prosto.android.compose.theme.md_theme_light_errorContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_inverseOnSurface
import dev.andrew.prosto.android.compose.theme.md_theme_light_inversePrimary
import dev.andrew.prosto.android.compose.theme.md_theme_light_inverseSurface
import dev.andrew.prosto.android.compose.theme.md_theme_light_onBackground
import dev.andrew.prosto.android.compose.theme.md_theme_light_onError
import dev.andrew.prosto.android.compose.theme.md_theme_light_onErrorContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_onPrimary
import dev.andrew.prosto.android.compose.theme.md_theme_light_onPrimaryContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_onSecondary
import dev.andrew.prosto.android.compose.theme.md_theme_light_onSecondaryContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_onSurface
import dev.andrew.prosto.android.compose.theme.md_theme_light_onSurfaceVariant
import dev.andrew.prosto.android.compose.theme.md_theme_light_onTertiary
import dev.andrew.prosto.android.compose.theme.md_theme_light_onTertiaryContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_outline
import dev.andrew.prosto.android.compose.theme.md_theme_light_outlineVariant
import dev.andrew.prosto.android.compose.theme.md_theme_light_primary
import dev.andrew.prosto.android.compose.theme.md_theme_light_primaryContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_scrim
import dev.andrew.prosto.android.compose.theme.md_theme_light_secondary
import dev.andrew.prosto.android.compose.theme.md_theme_light_secondaryContainer
import dev.andrew.prosto.android.compose.theme.md_theme_light_surface
import dev.andrew.prosto.android.compose.theme.md_theme_light_surfaceTint
import dev.andrew.prosto.android.compose.theme.md_theme_light_surfaceVariant
import dev.andrew.prosto.android.compose.theme.md_theme_light_tertiary
import dev.andrew.prosto.android.compose.theme.md_theme_light_tertiaryContainer

private val ProsotoLightScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val ProstoTypography = Typography(
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

@Composable
fun ProstoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProsotoLightScheme,
        typography = ProstoTypography,
        content = content
    )
}
