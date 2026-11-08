package org.transdroid.ui.torrents

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.transdroid.R
import org.transdroid.data.DownloadPaths
import org.transdroid.protocol.DownloadLocationInfo
import org.transdroid.ui.components.*
import org.transdroid.ui.theme.TransdroidTheme
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "zh-rCN-w390dp-h844dp-mdpi", application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PathUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val path = "/data/Movies"
    private fun options() = DownloadPathOptions(
        profileId = "server-a",
        info = DownloadLocationInfo(defaultPath = "/server/default", defaultAvailable = true),
        knownPaths = listOf(path), aliases = mapOf(path to "电影"),
    )

    @Test fun displayedDefaultDoesNotOverrideServerManagementAndAliasSelectsRealPath() {
        val selected = mutableStateOf<String?>(null)
        compose.setContent {
            TransdroidTheme {
                Surface { Column(Modifier.verticalScroll(rememberScrollState())) {
                    DownloadPathPicker(selected.value, { selected.value = it }, options())
                } }
            }
        }
        compose.onNodeWithTag("path-manual-input").assertTextContains("/server/default")
        check(DownloadPaths.submissionPath(selected.value) == null)
        compose.onNodeWithText("电影").performScrollTo().performClick()
        compose.runOnIdle { check(DownloadPaths.submissionPath(selected.value) == path) }
        compose.onNodeWithTag("path-server-default").performScrollTo().performClick()
        compose.runOnIdle { check(selected.value == null) }
    }

    @Test fun listChipShowsAliasAndOpensCompletePath() {
        compose.setContent { TransdroidTheme { Surface { TorrentPathChip(path, mapOf(path to "电影")) } } }
        compose.onNodeWithText("电影").assertExists()
        compose.onNodeWithTag("torrent-path-chip").performClick()
        compose.onNodeWithText(path).assertIsDisplayed()
    }

    // Robolectric #8460: Dialog + TextField can remeasure forever at custom widths above 320dp.
    // Keep the full interaction at 320dp; the other rendering cases retain their wider viewport.
    @Test @Config(qualifiers = "zh-rCN-w320dp-h844dp-mdpi")
    fun aliasSaveReceivesDirectoryAndLocalName() {
        var saved: Pair<String, String>? = null
        val opt = options().copy(saveAlias = { directory, name, done -> saved = directory to name; done(null) })
        compose.setContent { TransdroidTheme {
            Surface { Column(Modifier.verticalScroll(rememberScrollState())) {
                DownloadPathPicker(null, {}, opt)
            } }
        } }
        compose.onAllNodesWithText(compose.activity.getString(R.string.path_alias_edit))[1]
            .performScrollTo().performClick()
        compose.onNodeWithTag("path-alias-input").performTextReplacement("纪录片")
        compose.onNodeWithText(compose.activity.getString(R.string.details_set_location_save)).performClick()
        compose.runOnIdle { check(saved == path to "纪录片") }
    }

    @Test fun pathPickerChineseLight() = render(false, 1f, "paths-light-zh.png")
    @Test fun pathPickerChineseDark() = render(true, 1f, "paths-dark-zh.png")
    @Test @Config(qualifiers = "en-rUS-w390dp-h844dp-mdpi")
    fun pathPickerEnglishLight() = render(false, 1f, "paths-light-en.png")
    @Test @Config(qualifiers = "zh-rCN-w320dp-h844dp-mdpi")
    fun pathPickerNarrowLargeFont() = render(true, 1.3f, "paths-narrow-large-zh.png")

    @Test fun torrentDetailsChineseDark() = renderSecondary(true, 1f, "details-dark-zh.png", true)
    @Test @Config(qualifiers = "zh-rCN-w320dp-h844dp-mdpi")
    fun torrentDetailsNarrowLargeFont() = renderSecondary(false, 1.3f, "details-narrow-large-zh.png", true)
    @Test fun changelogChineseLight() = renderSecondary(false, 1f, "changelog-light-zh.png", false)

    private fun renderSecondary(dark: Boolean, scale: Float, name: String, details: Boolean) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                TransdroidTheme(darkTheme = dark) {
                    Surface(Modifier.fillMaxSize()) {
                        if (details) {
                            val viewModel = remember { TorrentsViewModel(org.transdroid.AppContainer(compose.activity)) }
                            val torrent = org.transdroid.protocol.Torrent(
                                id = "demo", name = "Ubuntu 26.04 桌面系统", status = org.transdroid.protocol.TorrentStatus.DOWNLOADING,
                                progress = 0.6f, downloadRate = 12000000, uploadRate = 300000,
                                sizeBytes = 5000000000, downloadedBytes = 3000000000, uploadedBytes = 1000000000,
                                ratio = 0.33f, downloadDir = path,
                            )
                            TorrentDetailsContent(viewModel, torrent)
                        } else org.transdroid.ui.settings.ChangelogScreen({})
                    }
                }
            }
        }
        saveFrame(name)
        if (details) compose.onNodeWithText("Ubuntu 26.04 桌面系统").assertExists()
    }

    private fun render(dark: Boolean, scale: Float, name: String) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, scale)) {
                TransdroidTheme(darkTheme = dark) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                            Text("添加下载 · Download", style = MaterialTheme.typography.headlineSmall)
                            DownloadPathPicker(null, {}, options())
                        }
                    }
                }
            }
        }
        saveFrame(name)
        compose.onNodeWithTag("path-manual-input").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("电影").performScrollTo().assertIsDisplayed()
    }

    private fun saveFrame(name: String) {
        compose.waitForIdle()
        val root = compose.activity.findViewById<View>(android.R.id.content)
        compose.runOnUiThread {
            val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
            root.draw(Canvas(bitmap))
            val output = File("build/reports/screenshots/$name")
            output.parentFile?.mkdirs()
            output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            bitmap.recycle()
        }
    }
}
