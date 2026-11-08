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
package org.transdroid.ui.settings

import androidx.annotation.StringRes
import org.transdroid.R

enum class ChangeKind { NEW, IMPROVED, FIXED }

/** One bullet; its text lives in res/values/strings_changelog.xml so it can be translated. */
data class ChangelogEntry(val kind: ChangeKind, @StringRes val textRes: Int)

data class Release(val version: String, @StringRes val dateRes: Int, val changes: List<ChangelogEntry>)

/**
 * The in-app "What's new" history, newest release first - this is hand-maintained, there is no
 * generator. Add one [Release] here (with a handful of user-facing bullets, not every commit)
 * once a version is actually tagged and released, not while it's still in development; the
 * first item is shown with a "Latest" badge regardless of what BuildConfig.VERSION_NAME says.
 *
 * The bullets themselves are resource strings (see strings_changelog.xml), so a release needs
 * its keys added to every translated values-* folder along with the entry here.
 */
val CHANGELOG: List<Release> = listOf(
    Release(
        version = "3.0.0-beta4",
        dateRes = R.string.changelog_date_2026_11,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta4_1),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta4_2),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta4_3),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_beta4_4),
        ),
    ),
    Release(
        version = "3.0.0-beta3",
        dateRes = R.string.changelog_date_2026_11,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta3_1),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta3_2),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta3_3),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta3_4),
        ),
    ),
    Release(
        version = "3.0.0-beta2",
        dateRes = R.string.changelog_date_2026_11,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta2_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta2_2),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta2_3),
        ),
    ),
    Release(
        version = "3.0.0-beta1",
        dateRes = R.string.changelog_date_2026_10,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta1_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta1_2),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta1_3),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_beta1_4),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_beta1_5),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_beta1_6),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_beta1_7),
        ),
    ),
    Release(
        version = "3.0.0-alpha6",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha6_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha6_2),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha6_3),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha6_4),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha6_5),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha6_6),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha6_7),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha6_8),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha6_9),
        ),
    ),
    Release(
        version = "3.0.0-alpha5",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha5_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha5_2),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha5_3),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha5_4),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha5_5),
        ),
    ),
    Release(
        version = "3.0.0-alpha4",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha4_1),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha4_2),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha4_3),
        ),
    ),
    Release(
        version = "3.0.0-alpha3",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha3_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha3_2),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha3_3),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha3_4),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha3_5),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha3_6),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha3_7),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha3_8),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha3_9),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha3_10),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha3_11),
        ),
    ),
    Release(
        version = "3.0.0-alpha2",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_2),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_3),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_4),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_5),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha2_6),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha2_7),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha2_8),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha2_9),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha2_10),
            ChangelogEntry(ChangeKind.FIXED, R.string.changelog_3_0_0_alpha2_11),
        ),
    ),
    Release(
        version = "3.0.0-alpha1",
        dateRes = R.string.changelog_date_2026_09,
        changes = listOf(
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha1_1),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha1_2),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha1_3),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha1_4),
            ChangelogEntry(ChangeKind.NEW, R.string.changelog_3_0_0_alpha1_5),
            ChangelogEntry(ChangeKind.IMPROVED, R.string.changelog_3_0_0_alpha1_6),
        ),
    ),
)
