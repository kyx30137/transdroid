package org.transdroid.data

import android.app.Application
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DownloadPathsPersistenceTest {
    @Test fun serverIsolationRepositoryReloadDuplicateRenameAndRemoval() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val repo = SettingsRepository(context)
        val first = "server-a"
        val second = "server-b"
        assertNull(repo.setDownloadPathAlias(first, "/media/Movies/", "电影"))
        assertNull(repo.setDownloadPathAlias(second, "/media/Movies", "纪录片"))
        assertEquals(PathAliasError.DUPLICATE_NAME, repo.setDownloadPathAlias(first, "/other", "电影"))
        repo.addRecentDownloadLocation(first, "/media/Movies/")
        repo.addRecentDownloadLocation(first, "/media/Movies")
        repo.addRecentDownloadLocation(second, "/Other")
        val reloaded = SettingsRepository(context)
        assertEquals(mapOf("/media/Movies" to "电影"), reloaded.downloadPathAliases(first).first())
        assertEquals(mapOf("/media/Movies" to "纪录片"), reloaded.downloadPathAliases(second).first())
        assertEquals(listOf("/media/Movies"), reloaded.recentDownloadLocations(first).first())
        assertEquals(listOf("/Other"), reloaded.recentDownloadLocations(second).first())
        assertNull(reloaded.setDownloadPathAlias(first, "/media/Movies", "影片"))
        assertEquals("影片", repo.downloadPathAliases(first).first()["/media/Movies"])
        assertNull(reloaded.setDownloadPathAlias(first, "/media/Movies", ""))
        assertTrue(repo.downloadPathAliases(first).first().isEmpty())
        assertEquals("纪录片", repo.downloadPathAliases(second).first()["/media/Movies"])
    }
}
