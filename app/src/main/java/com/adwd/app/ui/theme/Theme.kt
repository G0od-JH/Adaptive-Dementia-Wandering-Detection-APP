package com.adwd.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle

private val scheme = lightColorScheme(
    primary = AdwdColors.Purple,
    onPrimary = AdwdColors.White,
    secondary = AdwdColors.Slate,
    tertiary = AdwdColors.Teal,
    background = AdwdColors.White,
    onBackground = AdwdColors.Purple,
    surface = AdwdColors.White,
    onSurface = AdwdColors.Purple,
    error = AdwdColors.Level3,
)

private val base = Typography()
private fun TextStyle.pd() = copy(fontFamily = Pretendard)

private val typography = Typography(
    displayLarge = base.displayLarge.pd(), displayMedium = base.displayMedium.pd(), displaySmall = base.displaySmall.pd(),
    headlineLarge = base.headlineLarge.pd(), headlineMedium = base.headlineMedium.pd(), headlineSmall = base.headlineSmall.pd(),
    titleLarge = base.titleLarge.pd(), titleMedium = base.titleMedium.pd(), titleSmall = base.titleSmall.pd(),
    bodyLarge = base.bodyLarge.pd(), bodyMedium = base.bodyMedium.pd(), bodySmall = base.bodySmall.pd(),
    labelLarge = base.labelLarge.pd(), labelMedium = base.labelMedium.pd(), labelSmall = base.labelSmall.pd(),
)

@Composable
fun AdwdTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = typography, content = content)
}
