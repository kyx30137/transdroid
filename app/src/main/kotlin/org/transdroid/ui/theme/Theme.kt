/*
 * Copyright 2010-2026 Eric Kok et al.
 *
 * Transdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Transdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transdroid. If not, see <https://www.gnu.org/licenses/>.
 */
package org.transdroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.transdroid.protocol.TorrentStatus

/** System fonts retain Android's locale-aware Chinese fallback. */
private fun appTextStyle(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = height.sp,
    letterSpacing = 0.sp,
)

val TransdroidTypography = Typography(
    displayLarge = appTextStyle(57, 64),
    displayMedium = appTextStyle(45, 52),
    displaySmall = appTextStyle(36, 44),
    headlineLarge = appTextStyle(32, 40),
    headlineMedium = appTextStyle(28, 36),
    headlineSmall = appTextStyle(22, 28, FontWeight.SemiBold),
    titleLarge = appTextStyle(20, 28, FontWeight.SemiBold),
    titleMedium = appTextStyle(16, 24, FontWeight.Medium),
    titleSmall = appTextStyle(14, 20, FontWeight.SemiBold),
    bodyLarge = appTextStyle(16, 24),
    bodyMedium = appTextStyle(14, 20),
    bodySmall = appTextStyle(13, 20),
    labelLarge = appTextStyle(14, 20, FontWeight.Medium),
    labelMedium = appTextStyle(13, 20, FontWeight.Medium),
    labelSmall = appTextStyle(12, 16, FontWeight.Medium),
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

val LocalLabelHues = staticCompositionLocalOf { LightLabelHues }

/** The stable hue for a label/category name, in the current light or dark theme. */
@Composable
@ReadOnlyComposable
fun labelHue(label: String): LabelHue = LocalLabelHues.current.let { it[label.hashCode().mod(it.size)] }

/** Drop-in replacement for the pre-redesign flat `torrent.status.accentColor` call sites. */
val TorrentStatus.accentColor: Color
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current.accent(this)

/** The lighter progress-track tone paired with [accentColor]. */
val TorrentStatus.trackColor: Color
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current.track(this)

@Composable
fun TransdroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors
    CompositionLocalProvider(
        LocalStatusColors provides statusColors,
        LocalLabelHues provides if (darkTheme) DarkLabelHues else LightLabelHues,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            shapes = TransdroidShapes,
            typography = TransdroidTypography,
            content = content,
        )
    }
}
