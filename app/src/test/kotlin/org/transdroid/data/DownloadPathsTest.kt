package org.transdroid.data

import org.junit.Assert.*
import org.junit.Test
import org.transdroid.protocol.DownloadLocationInfo

class DownloadPathsTest {
    @Test fun defaultsAreNotExplicitOverrides() {
        assertNull(DownloadPaths.submissionPath(null))
        assertEquals("/media/Movies", DownloadPaths.submissionPath("/media/Movies/"))
        assertEquals("/media/default", DownloadPathSelection().displayPath("/media/default"))
        assertNull(DownloadPathSelection().requestPath())
    }
    @Test fun candidatesUseOnlySupportedAbsoluteSourcesAndDeduplicate() {
        val info = DownloadLocationInfo(defaultPath = "/downloads/", categoryPaths = mapOf("movie" to "/movies", "relative" to "subdir", "empty" to ""))
        assertEquals(listOf("/downloads", "/recent", "/Named", "/known", "/movies"),
            DownloadPaths.candidates(info, listOf("/recent/", "/downloads"), listOf("/known", "/known/"), mapOf("/Named" to "本地")))
    }
    @Test fun unavailableDefaultKeepsKnownRecentAndNamedPaths() {
        assertEquals(listOf("/recent", "/named", "/known"), DownloadPaths.candidates(DownloadLocationInfo(), listOf("/recent"), listOf("/known"), mapOf("/named" to "电影")))
    }
    @Test fun rootTrailingSlashAndCaseStayDistinct() {
        assertEquals("/", DownloadPaths.normalize("///"))
        assertEquals("/Movies", DownloadPaths.normalize("/Movies///"))
        assertEquals(listOf("/Movies", "/movies"), DownloadPaths.candidates(DownloadLocationInfo(), listOf("/Movies/", "/movies/"), emptyList(), emptyMap()))
        assertEquals("/", DownloadPaths.label("/", emptyMap()))
    }
    @Test fun aliasesTakePriorityAndMatchTrailingSlashOnly() {
        assertEquals("电影", DownloadPaths.label("/mnt/Movies/", mapOf("/mnt/Movies" to "电影")))
        assertEquals("movies", DownloadPaths.label("/mnt/movies", mapOf("/mnt/Movies" to "电影")))
        assertEquals("电视剧", DownloadPaths.label("/下载/电视剧/", emptyMap()))
    }
    @Test fun duplicateNamesRejectedOnlyForOtherPaths() {
        val names = mapOf("/Movies" to "Movies")
        assertEquals(PathAliasError.DUPLICATE_NAME, DownloadPaths.validateAlias("/Else", "movies", names))
        assertNull(DownloadPaths.validateAlias("/Movies/", "Movies", names))
        assertNull(DownloadPaths.validateAlias("/Movies", "", names))
        assertEquals(PathAliasError.INVALID_NAME, DownloadPaths.validateAlias("/Movies", "x".repeat(41), names))
        assertEquals(PathAliasError.INVALID_PATH, DownloadPaths.validateAlias("relative", "电影", names))
    }
    @Test fun encodedAliasesRoundTripAndCorruptDataFallsBack() {
        val names = mapOf("/中文/电影" to "电影", "/" to "根目录")
        assertEquals(names, DownloadPaths.decode(DownloadPaths.encode(names)))
        assertTrue(DownloadPaths.decode("invalid-json").isEmpty())
    }
    @Test fun serverDirectoryWhitespaceIsPartOfItsIdentity() {
        val directory = "/downloads/Movies "
        assertEquals(directory, DownloadPaths.normalize(directory))
        assertEquals(directory, DownloadPaths.submissionPath(directory))
        assertEquals(listOf(directory, "/downloads/Movies"), DownloadPaths.candidates(
            DownloadLocationInfo(defaultPath = directory), emptyList(), listOf("/downloads/Movies"), emptyMap()))
        assertEquals("尾空格目录", DownloadPaths.label(directory, mapOf(directory to "尾空格目录")))
        assertEquals("Movies", DownloadPaths.label("/downloads/Movies", mapOf(directory to "尾空格目录")))
    }
    @Test fun rejectsControlCharactersAndRelativeDirectories() {
        assertFalse(DownloadPaths.isValid("/bad\npath"))
        assertFalse(DownloadPaths.isValid("relative/dir"))
        assertTrue(DownloadPaths.isValid("/media/Movies"))
        assertTrue(DownloadPaths.isValid("C:\\Downloads"))
    }
}
