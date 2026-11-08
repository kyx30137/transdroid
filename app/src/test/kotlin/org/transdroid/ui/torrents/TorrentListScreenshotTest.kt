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
package org.transdroid.ui.torrents

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.transdroid.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.transdroid.data.ServerProfile
import org.transdroid.protocol.DaemonType
import org.transdroid.protocol.Torrent
import org.transdroid.protocol.TorrentStatus
import org.transdroid.ui.theme.TransdroidTheme
import java.io.File
import java.io.FileOutputStream

/**
 * Renders the real torrent screen to PNG on the JVM: no emulator, no device, no KVM, no
 * instrumentation. Everything in the picture comes from production composables ([PhoneTopBar],
 * [TorrentListContent], [TorrentsToolbar], [FilterChipsRow], [TorrentRow]) plus the real theme; only
 * the demo data is fixed and no-op callbacks stand in for the view model's actions.
 *
 * Output: `app/build/reports/screenshots/torrents-phone-{dark,light}.png`, 780x1688 px - the design
 * canvas of 390x844 dp at 2x.
 *
 * Run: `./gradlew :app:testFullDebugUnitTest --tests '*TorrentListScreenshotTest'`
 * (pass `-Probolectric.repo.url=<mirror>` if Maven Central is unreachable for the android-all jar).
 *
 * Host requirements: a JDK for the host CPU (Robolectric's native graphics runtime ships for macOS
 * arm64/x86_64, linux x86_64 and Windows x86_64). On an arm64 Linux host, run this with an x86_64 JDK
 * plus amd64 libc - e.g. under Rosetta/qemu - since linux-arm64 is not covered yet; Paparazzi/layoutlib
 * cannot help there either, as its bridge is x86_64-only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-xhdpi", application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TorrentListScreenshotTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun darkTorrentList() = render(dark = true, fileName = "torrents-phone-dark.png")

    @Test
    fun lightTorrentList() = render(dark = false, fileName = "torrents-phone-light.png")

    /**
     * The same screen with the device locale switched to Simplified Chinese: every label here comes
     * from `values-zh-rCN/strings.xml` through the normal resource lookup, so a missing or stale
     * translation would fall back to English and show up in the picture.
     */
    @Test
    @Config(qualifiers = "zh-rCN-w390dp-h844dp-xhdpi")
    fun darkTorrentListChinese() = render(dark = true, fileName = "torrents-phone-dark-zh-rCN.png")

    @Test
    @Config(qualifiers = "zh-rCN-w390dp-h844dp-xhdpi")
    fun lightTorrentListChinese() = render(dark = false, fileName = "torrents-phone-light-zh-rCN.png")

    @Test
    fun statusChipsFilterRowsKeepCountsAndUpdateForTheCurrentServer() {
        val state = mutableStateOf(sampleUiState())
        val all = compose.activity.getString(R.string.filter_all)
        val downloading = compose.activity.getString(R.string.filter_downloading)
        val paused = compose.activity.getString(R.string.filter_paused)
        compose.setContent {
            TransdroidTheme {
                Surface {
                    TorrentListContent(
                        ui = state.value, onOpenDetails = {}, onAddTorrent = {}, onRefresh = {},
                        onSetFilter = { state.value = state.value.copy(filter = it) },
                        onClearFilters = {}, onSetSort = {}, onToggleAltSpeed = {},
                        onStartSelection = {}, onToggleSelected = {}, onToggleStartPause = {},
                        onDismissSelectionHint = {},
                    )
                }
            }
        }
        compose.onNode(hasText(all) and hasText("6")).assertIsSelected()
        compose.onNode(hasText(downloading) and hasText("2")).performClick().assertIsSelected()
        compose.onNodeWithText("Ubuntu 26.04 Desktop amd64").assertExists()
        compose.onNodeWithText("Arch Linux 2026.11.01 x86_64").assertDoesNotExist()
        compose.onNode(hasText(all) and hasText("6")).assertIsNotSelected()
        compose.onNode(hasText(paused) and isSelectable()).performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithText("Arch Linux 2026.11.01 x86_64").assertExists()
        compose.onNodeWithText("Ubuntu 26.04 Desktop amd64").assertDoesNotExist()
        // Model the existing server-switch reset before the replacement server responds.
        compose.runOnIdle {
            state.value = state.value.copy(
                activeProfile = state.value.activeProfile!!.copy(id = "second"),
                torrents = emptyList(), hasLoaded = false,
            )
        }
        compose.onNode(hasText(paused) and hasText("0")).assertIsSelected()
        compose.runOnIdle {
            state.value = state.value.copy(
                torrents = listOf(sampleUiState().torrents.first()), hasLoaded = true,
            )
        }
        compose.onNode(hasText(all) and hasText("1")).performScrollTo().performClick()
        compose.onNode(hasText(downloading) and hasText("1")).assertExists()
        compose.onNodeWithText("Ubuntu 26.04 Desktop amd64").assertExists()
    }

    @Test
    @Config(qualifiers = "en-rUS-w320dp-h700dp-mdpi")
    fun narrowChipsLargeCountsLight() = renderChips(dark = false, prefix = "chips-narrow-light")

    @Test
    @Config(qualifiers = "zh-rCN-w320dp-h700dp-mdpi")
    fun narrowChipsLargeCountsDarkChinese() = renderChips(dark = true, prefix = "chips-narrow-dark-zh")

    @Test
    @Config(qualifiers = "en-rUS-w800dp-h1024dp-mdpi")
    fun tabletChipsLargeCountsDark() = renderChips(dark = true, prefix = "chips-tablet-dark")

    @Test
    @Config(qualifiers = "zh-rCN-w800dp-h1024dp-mdpi")
    fun tabletChipsLargeCountsLightChinese() = renderChips(dark = false, prefix = "chips-tablet-light-zh")

    private fun renderChips(dark: Boolean, prefix: String) {
        val sample = sampleUiState()
        val state = mutableStateOf(sample.copy(torrents = List(9999) { index ->
            sample.torrents.first().copy(
                id = index.toString(),
                status = if (index < 99) TorrentStatus.PAUSED else TorrentStatus.DOWNLOADING,
                progress = 0.5f,
            )
        }))
        compose.setContent {
            TransdroidTheme(darkTheme = dark) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.padding(16.dp)) {
                        FilterChipsRow(state.value, { state.value = state.value.copy(filter = it) })
                    }
                }
            }
        }
        compose.waitForIdle()
        saveFrame("$prefix-start.png", minimumDrawnFraction = 1000)
        val all = compose.activity.getString(R.string.filter_all)
        compose.onNode(hasText(all) and hasText("9999")).assertIsSelected()
        // Every full label remains reachable, including the two-digit count and zero count.
        val labels = listOf(R.string.filter_all, R.string.filter_downloading, R.string.filter_seeding,
            R.string.filter_completed, R.string.filter_paused, R.string.filter_error)
        labels.forEach { resource ->
            compose.onNode(hasText(compose.activity.getString(resource)) and isSelectable())
                .performScrollTo().assertIsDisplayed()
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onAllNodesWithText(compose.activity.getString(resource), useUnmergedTree = true)[0]
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            check(layouts.isNotEmpty() && layouts.all { layout ->
                // Compose can round measured text down by a fraction of one physical pixel.
                // Reject missing glyphs/ellipsis and any overflow beyond that rounding tolerance.
                val lineWidth = layout.getLineRight(0) - layout.getLineLeft(0)
                println("chip text=${layout.layoutInput.text}, lineWidth=$lineWidth, measuredWidth=${layout.size.width}")
                !layout.didOverflowHeight && !layout.isLineEllipsized(0) &&
                    lineWidth <= layout.size.width + 1f && layout.lineCount == 1 &&
                    layout.getLineEnd(0) == layout.layoutInput.text.length
            }) { "Text layout overflow: ${layouts.map { "${it.layoutInput.text}: size=${it.size}, width=${it.didOverflowWidth}, height=${it.didOverflowHeight}, lines=${it.lineCount}" }}" }
        }
        for (count in listOf("9999", "99", "0")) {
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onAllNodesWithText(count, useUnmergedTree = true)[0]
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            check(layouts.isNotEmpty() && layouts.all { layout ->
                // Compose can round measured text down by a fraction of one physical pixel.
                // Reject missing glyphs/ellipsis and any overflow beyond that rounding tolerance.
                val lineWidth = layout.getLineRight(0) - layout.getLineLeft(0)
                println("chip text=${layout.layoutInput.text}, lineWidth=$lineWidth, measuredWidth=${layout.size.width}")
                !layout.didOverflowHeight && !layout.isLineEllipsized(0) &&
                    lineWidth <= layout.size.width + 1f && layout.lineCount == 1 &&
                    layout.getLineEnd(0) == layout.layoutInput.text.length
            }) { "Text layout overflow: ${layouts.map { "${it.layoutInput.text}: size=${it.size}, width=${it.didOverflowWidth}, height=${it.didOverflowHeight}, lines=${it.lineCount}" }}" }
        }
        val error = compose.activity.getString(R.string.filter_error)
        compose.onNode(hasText(error) and hasText("0")).performClick().performScrollTo().assertIsSelected()
        compose.onNodeWithTag("status-scroll-next", useUnmergedTree = true).assertDoesNotExist()
        saveFrame("$prefix-end.png", minimumDrawnFraction = 1000)
        compose.onNode(hasText(all) and hasText("9999")).performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithTag("status-scroll-previous", useUnmergedTree = true).assertDoesNotExist()
        if (compose.activity.resources.configuration.screenWidthDp <= 320) {
            compose.onNodeWithTag("status-scroll-next", useUnmergedTree = true).assertExists()
        }
    }

    private fun render(dark: Boolean, fileName: String) {
        val ui = sampleUiState()
        compose.setContent {
            TransdroidTheme(darkTheme = dark) {
                // The real screen root is a Scaffold inside a Surface; a Surface is what gives rows
                // their content colour, so leaving it out would render the titles black.
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Scaffold(topBar = { PhoneTopBar(ui, true, true, {}, {}, {}, {}, {}) }) { padding ->
                        Box(Modifier.paddingExceptBottom(padding).fillMaxSize()) {
                            TorrentListContent(
                                ui = ui,
                                onOpenDetails = {},
                                onAddTorrent = {},
                                onRefresh = {},
                                onSetFilter = {},
                                onClearFilters = {},
                                onSetSort = {},
                                onToggleAltSpeed = {},
                                onStartSelection = {},
                                onToggleSelected = {},
                                onToggleStartPause = {},
                                onDismissSelectionHint = {},
                                searchAvailable = true,
                                rssAvailable = true,
                                showFilterChips = true,
                            )
                        }
                    }
                }
            }
        }
        compose.waitForIdle()

        saveFrame(fileName)
        compose.onNodeWithText("Linux").assertExists()
        compose.onNodeWithText("电影目录").assertExists()
    }

    private fun saveFrame(fileName: String, minimumDrawnFraction: Int = 20) {
        compose.waitForIdle()
        val view: View = compose.activity.window.decorView
        val metrics = compose.activity.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        if (view.width != width || view.height != height) {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, width, height)
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))

        // A blank or half-drawn frame would still be a valid PNG, so check the pixels themselves:
        // real content covers a good share of the canvas, and text antialiasing gives many shades.
        val pixels = IntArray(width * height).also { bitmap.getPixels(it, 0, width, 0, 0, width, height) }
        val background = pixels[0]
        val drawn = pixels.count { it != background }
        check(drawn > width * height / minimumDrawnFraction) { "Frame looks blank: only ${drawn} of ${width * height} pixels drawn" }
        check(pixels.toHashSet().size > 64) { "Frame looks like flat colour: ${pixels.toHashSet().size} distinct colours" }

        val out = File("build/reports/screenshots").apply { mkdirs() }.resolve(fileName)
        FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        check(out.length() > 0) { "Screenshot was not written: ${out.absolutePath}" }
        println("screenshot: ${out.absolutePath} (${width}x$height)")
    }
}

/**
 * A fixed slice of [TorrentsUiState] - the same kind of demo data the design mockup used - so the
 * picture is reproducible. Everything derived from it (visible rows, filter counters, summed rates)
 * is the production logic, not a column of hand-written strings.
 */
private fun sampleUiState(): TorrentsUiState {
    val profile = ServerProfile(
        id = "qb",
        name = "书房 NAS",
        type = DaemonType.QBITTORRENT,
        host = "nas.lan",
        port = 8080,
    )
    val torrents = listOf(
        Torrent(
            id = "1",
            name = "Ubuntu 26.04 Desktop amd64",
            status = TorrentStatus.DOWNLOADING,
            progress = 0.62f,
            downloadRate = 11_300_000,
            uploadRate = 480_000,
            etaSeconds = 3_840,
            sizeBytes = 5_368_709_120,
            downloadedBytes = 3_328_599_654,
            uploadedBytes = 1_073_741_824,
            ratio = 0.32f,
            peersConnected = 42,
            seedersConnected = 18,
            leechersConnected = 24,
            labels = listOf("系统"),
            downloadDir = "/volume1/downloads/Linux",
            addedTimestamp = 1760000002,
        ),
        Torrent(
            id = "2",
            name = "Dune.Part.Two.2024.2160p.UHD.BluRay.x265-TERMiNAL",
            status = TorrentStatus.SEEDING,
            progress = 1f,
            downloadRate = 0,
            uploadRate = 2_200_000,
            sizeBytes = 64_424_509_440,
            downloadedBytes = 64_424_509_440,
            uploadedBytes = 118_111_289_344,
            ratio = 1.83f,
            peersConnected = 9,
            seedersConnected = 9,
            labels = listOf("电影"),
            downloadDir = "/volume1/downloads/Movies",
            addedTimestamp = 1760000001,
        ),
        Torrent(
            id = "3",
            name = "Arch Linux 2026.11.01 x86_64",
            status = TorrentStatus.PAUSED,
            progress = 0.18f,
            downloadRate = 0,
            uploadRate = 0,
            sizeBytes = 1_181_116_006,
            downloadedBytes = 212_600_881,
            uploadedBytes = 0,
            ratio = 0f,
        ),
        Torrent(
            id = "4",
            name = "Debian 13.2 netinst arm64",
            status = TorrentStatus.ERROR,
            progress = 0.04f,
            downloadRate = 0,
            uploadRate = 0,
            sizeBytes = 658_505_728,
            downloadedBytes = 26_214_400,
            uploadedBytes = 0,
            ratio = 0f,
            error = "tracker unreachable",
        ),
        Torrent(
            id = "5",
            name = "Fedora-Workstation-Live-44",
            status = TorrentStatus.QUEUED,
            progress = 0f,
            downloadRate = 0,
            uploadRate = 0,
            sizeBytes = 2_254_857_830,
            downloadedBytes = 0,
            uploadedBytes = 0,
            ratio = 0f,
        ),
        Torrent(
            id = "6",
            name = "openSUSE-Tumbleweed-DVD",
            status = TorrentStatus.PAUSED,
            progress = 1f,
            downloadRate = 0,
            uploadRate = 0,
            sizeBytes = 4_724_464_025,
            downloadedBytes = 4_724_464_025,
            uploadedBytes = 12_307_693_568,
            ratio = 2.61f,
        ),
    )
    return TorrentsUiState(
        activeProfile = profile,
        allProfiles = listOf(profile),
        profilesLoaded = true,
        profileCount = 1,
        torrents = torrents,
        hasLoaded = true,
        altSpeedSupported = true,
        altSpeedEnabled = false,
        pathAliases = mapOf("/volume1/downloads/Movies" to "电影目录"),
    )
}
