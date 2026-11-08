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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.transdroid.protocol.TorrentStatus

// Palette of the qBittorrent remote controller design (v1.2): near-black neutral surfaces with a
// single blue accent, plus one hue per torrent state. Values are the design's own custom
// properties (index.html: --bg / --surface / --line / --text / --dim / --accent / --green /
// --yellow / --red), so the Compose theme and the HTML prototype cannot drift apart.
//
// The design is dark-first; the light variant is its html[data-theme="light"] block, where the
// accent is deliberately darkened (#1F6FE0) to keep contrast against white surfaces.

private val LightPrimary = Color(0xFF1F6FE0)
private val LightOnPrimary = Color(0xFFFFFFFF)
private val LightPrimaryContainer = Color(0xFFD8E6FB)
private val LightOnPrimaryContainer = Color(0xFF0A2A57)
private val LightSurface = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFEEF1F6)
private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val LightSurfaceContainerLow = Color(0xFFF4F6FA)
private val LightSurfaceContainer = Color(0xFFFFFFFF)
private val LightSurfaceContainerHigh = Color(0xFFF4F6FA)
private val LightSurfaceContainerHighest = Color(0xFFE9EDF4)
private val LightOnSurface = Color(0xFF10151C)
private val LightOnSurfaceVariant = Color(0xFF5D6A7A)
private val LightOutline = Color(0xFF8B96A5)
private val LightOutlineVariant = Color(0xFFDFE4EC)

private val DarkPrimary = Color(0xFF4F9CF9)
private val DarkOnPrimary = Color(0xFFFFFFFF)
private val DarkPrimaryContainer = Color(0xFF12335C)
private val DarkOnPrimaryContainer = Color(0xFFD6E6FF)
private val DarkSurface = Color(0xFF07090C)
private val DarkSurfaceContainerLowest = Color(0xFF07090C)
private val DarkSurfaceContainerLow = Color(0xFF0E1116)
private val DarkSurfaceContainer = Color(0xFF0E1116)
private val DarkSurfaceContainerHigh = Color(0xFF151A21)
private val DarkSurfaceContainerHighest = Color(0xFF1B222B)
private val DarkOnSurface = Color(0xFFE8ECF3)
private val DarkOnSurfaceVariant = Color(0xFF98A3B3)
private val DarkOutline = Color(0xFF6D7A8C)
private val DarkOutlineVariant = Color(0xFF232A34)

// Error is a state hue in this design (#F97066) rather than material's role colour, so it doubles
// as ColorScheme.error; the on-colours are dark mixes of the same red rather than plain black.
private val LightError = Color(0xFFD64545)
private val DarkError = Color(0xFFF97066)

// The design has exactly one accent, so the "selected chip" role is its 14% wash over the surface:
// rgba(79,156,249,.14) over #07090C / rgba(31,111,224,.10) over white.
private val LightSelectedContainer = Color(0xFFE4EDFB)
private val LightOnSelectedContainer = Color(0xFF17427F)
private val DarkSelectedContainer = Color(0xFF142334)
private val DarkOnSelectedContainer = Color(0xFFA8CBFA)

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightOnSurfaceVariant,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceContainerHighest,
    onSecondaryContainer = LightOnSurface,
    tertiary = LightPrimary,
    tertiaryContainer = LightSelectedContainer,
    onTertiaryContainer = LightOnSelectedContainer,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceContainerHighest,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceDim = LightSurfaceContainerHighest,
    surfaceBright = LightSurface,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = Color.White,
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkOnSurfaceVariant,
    onSecondary = Color(0xFF0B0E13),
    secondaryContainer = DarkSurfaceContainerHighest,
    onSecondaryContainer = DarkOnSurface,
    tertiary = DarkPrimary,
    tertiaryContainer = DarkSelectedContainer,
    onTertiaryContainer = DarkOnSelectedContainer,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceContainerHighest,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = DarkSurface,
    surfaceBright = DarkSurfaceContainerHighest,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = Color(0xFF2A0906),
)

/**
 * Torrent state accents plus a paired low-emphasis "track" tone for progress-bar backgrounds.
 *
 * The design encodes state as colour + text (never colour alone) and defines exactly five:
 * downloading = the blue accent, seeding = green, paused = amber, error = red, completed = grey.
 * [waiting] (queued/checking) and [idle] (unknown) borrow the two neutrals so no status is left
 * without a bar colour.
 */
data class StatusColors(
    val downloading: Color,
    val downloadingTrack: Color,
    val seeding: Color,
    val seedingTrack: Color,
    val paused: Color,
    val pausedTrack: Color,
    val waiting: Color,
    val waitingTrack: Color,
    val idle: Color,
    val idleTrack: Color,
    val error: Color,
)

val LightStatusColors = StatusColors(
    downloading = Color(0xFF1F6FE0),
    downloadingTrack = Color(0xFFDCE8FA),
    seeding = Color(0xFF0F9D6E),
    seedingTrack = Color(0xFFD3EDE4),
    paused = Color(0xFFB27A10),
    pausedTrack = Color(0xFFF4E7C9),
    waiting = Color(0xFF5D6A7A),
    waitingTrack = Color(0xFFE4E8EE),
    idle = Color(0xFF8B96A5),
    idleTrack = Color(0xFFE4E8EE),
    error = LightError,
)

val DarkStatusColors = StatusColors(
    downloading = Color(0xFF4F9CF9),
    downloadingTrack = Color(0xFF16283F),
    seeding = Color(0xFF34D399),
    seedingTrack = Color(0xFF10291F),
    paused = Color(0xFFF0B429),
    pausedTrack = Color(0xFF2E2510),
    waiting = Color(0xFFA9B4C4),
    waitingTrack = Color(0xFF1B222B),
    idle = Color(0xFF8B96A5),
    idleTrack = Color(0xFF1B222B),
    error = DarkError,
)

/** Progress/state accent for a torrent's current status. */
fun StatusColors.accent(status: TorrentStatus): Color = when (status) {
    TorrentStatus.DOWNLOADING -> downloading
    TorrentStatus.SEEDING -> seeding
    TorrentStatus.PAUSED -> paused
    TorrentStatus.QUEUED, TorrentStatus.CHECKING -> waiting
    TorrentStatus.ERROR -> error
    TorrentStatus.UNKNOWN -> idle
}

/** The low-emphasis tone paired with [accent]. */
fun StatusColors.track(status: TorrentStatus): Color = when (status) {
    TorrentStatus.DOWNLOADING -> downloadingTrack
    TorrentStatus.SEEDING -> seedingTrack
    TorrentStatus.PAUSED -> pausedTrack
    TorrentStatus.QUEUED, TorrentStatus.CHECKING -> waitingTrack
    TorrentStatus.ERROR, TorrentStatus.UNKNOWN -> idleTrack
}

/**
 * One label/category hue: [dot] for the filter drawer and set-label sheet, [content] and
 * [container] for the label pill on the torrent list and details screen. Labels are free-form user
 * text, so a fixed set of hues is cycled by a stable hash of the label name (see [labelHue]) rather
 * than matched to specific category keywords.
 */
data class LabelHue(val dot: Color, val content: Color, val container: Color)

val LightLabelHues: List<LabelHue> = listOf(
    LabelHue(dot = Color(0xFFB27A10), content = Color(0xFF8A5E0C), container = Color(0xFFF4E7C9)), // amber
    LabelHue(dot = Color(0xFF6B4BC0), content = Color(0xFF5A3EAD), container = Color(0xFFE7DEF8)), // purple
    LabelHue(dot = Color(0xFF1F6FE0), content = Color(0xFF17539F), container = Color(0xFFDCE8FA)), // blue
    LabelHue(dot = Color(0xFF0F8A80), content = Color(0xFF0B665F), container = Color(0xFFD2E9E5)), // teal
)

val DarkLabelHues: List<LabelHue> = listOf(
    LabelHue(dot = Color(0xFFF0B429), content = Color(0xFFF0B429), container = Color(0xFF2E2510)),
    LabelHue(dot = Color(0xFFA892E6), content = Color(0xFFC3AEF0), container = Color(0xFF2A2240)),
    LabelHue(dot = Color(0xFF4F9CF9), content = Color(0xFFA8CBFA), container = Color(0xFF142334)),
    LabelHue(dot = Color(0xFF34D399), content = Color(0xFF5FD3AC), container = Color(0xFF10291F)),
)
