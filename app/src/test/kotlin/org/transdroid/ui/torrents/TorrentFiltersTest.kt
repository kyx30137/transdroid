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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.transdroid.protocol.Torrent
import org.transdroid.protocol.TorrentStatus

class TorrentFiltersTest {

    private fun torrent(id: String, labels: List<String> = emptyList(), trackers: List<String> = emptyList()) = Torrent(
        id = id,
        name = id,
        status = TorrentStatus.SEEDING,
        progress = 1f,
        downloadRate = 0,
        uploadRate = 0,
        sizeBytes = 1,
        downloadedBytes = 1,
        uploadedBytes = 0,
        ratio = 0f,
        labels = labels,
        trackers = trackers,
    )

    @Test
    fun `status counts use all loaded torrents and retain existing overlapping categories`() {
        val torrents = listOf(
            torrent("download").copy(status = TorrentStatus.DOWNLOADING, progress = 0.2f),
            torrent("queued").copy(status = TorrentStatus.QUEUED, progress = 0f),
            torrent("checking").copy(status = TorrentStatus.CHECKING, progress = 0.5f),
            torrent("seed"),
            torrent("paused").copy(status = TorrentStatus.PAUSED),
            torrent("error").copy(status = TorrentStatus.ERROR, progress = 0f),
        )
        val expectedCounts = listOf(6, 3, 1, 2, 1, 1)
        val expectedIds = listOf(
            setOf("download", "queued", "checking", "seed", "paused", "error"),
            setOf("download", "queued", "checking"), setOf("seed"),
            setOf("seed", "paused"), setOf("paused"), setOf("error"),
        )
        TorrentFilter.entries.forEachIndexed { index, selected ->
            val ui = TorrentsUiState(torrents = torrents, filter = selected)
            assertEquals(expectedCounts, TorrentFilter.entries.map(ui::countFor))
            assertEquals(expectedIds[index], ui.visibleTorrents.map { it.id }.toSet())
        }
    }

    @Test
    fun `search labels trackers and sorting filter rows without shrinking status counts`() {
        val ui = TorrentsUiState(
            torrents = listOf(torrent("zeta", listOf("tv"), listOf("t1")), torrent("alpha", listOf("tv"), listOf("t1")), torrent("other")),
            filter = TorrentFilter.SEEDING, nameQuery = "A", labelFilters = setOf("tv"),
            trackerFilters = setOf("t1"), sort = TorrentSort.NAME, sortDescending = false,
        )
        assertEquals(listOf("alpha", "zeta"), ui.visibleTorrents.map { it.id })
        assertEquals(3, ui.countFor(TorrentFilter.ALL))
        assertEquals(3, ui.countFor(TorrentFilter.SEEDING))
        assertEquals(listOf("zeta", "alpha"), ui.copy(sortDescending = true).visibleTorrents.map { it.id })
        assertTrue(ui.copy(nameQuery = "missing").visibleTorrents.isEmpty())
        assertEquals(3, ui.copy(nameQuery = "missing").countFor(TorrentFilter.SEEDING))
    }

    @Test
    fun `empty and refreshed task sets recompute every status count`() {
        val loaded = TorrentsUiState(torrents = listOf(torrent("old")))
        val empty = loaded.copy(torrents = emptyList(), hasLoaded = false)
        assertTrue(TorrentFilter.entries.all { empty.countFor(it) == 0 })
        val refreshed = empty.copy(torrents = listOf(torrent("new").copy(status = TorrentStatus.DOWNLOADING, progress = 0f)))
        assertEquals(1, refreshed.countFor(TorrentFilter.ALL))
        assertEquals(1, refreshed.countFor(TorrentFilter.DOWNLOADING))
        assertEquals(0, refreshed.countFor(TorrentFilter.SEEDING))
    }

    @Test
    fun `stored filter names fall back to all`() {
        assertEquals(TorrentFilter.COMPLETED, TorrentFilter.fromName("COMPLETED"))
        assertEquals(TorrentFilter.ALL, TorrentFilter.fromName("RENAMED"))
        assertEquals(TorrentFilter.ALL, TorrentFilter.fromName(null))
    }

    @Test
    fun `drawer lists match the torrents when nothing stale is selected`() {
        val ui = TorrentsUiState(
            torrents = listOf(torrent("a", labels = listOf("tv"), trackers = listOf("t1")), torrent("b")),
            labelFilters = setOf("tv"),
        )
        assertEquals(listOf("tv"), ui.drawerLabels)
        assertTrue(ui.drawerShowsLabels)
        assertEquals(listOf("t1"), ui.drawerTrackers)
    }

    @Test
    fun `restored filters no torrent matches anymore stay in the drawer so they can be cleared`() {
        val ui = TorrentsUiState(
            torrents = listOf(torrent("a", labels = listOf("tv"), trackers = listOf("t1"))),
            labelFilters = setOf("movies", NO_LABEL),
            trackerFilters = setOf("gone.example"),
        )
        assertTrue(ui.visibleTorrents.isEmpty())
        assertEquals(listOf("movies", "tv"), ui.drawerLabels)
        assertTrue(ui.drawerShowsLabels)
        assertEquals(listOf("gone.example", "t1"), ui.drawerTrackers)
        // The label picker still only offers labels that exist on the server
        assertEquals(listOf("tv"), ui.availableLabels)
        assertFalse(ui.hasUnlabeledTorrents)
    }

    @Test
    fun `the labels section, with No label, shows whenever the server uses labels`() {
        val allLabelled = TorrentsUiState(torrents = listOf(torrent("a", labels = listOf("tv"))))
        assertTrue(allLabelled.drawerShowsLabels)
        assertEquals(0, allLabelled.countForLabel(NO_LABEL))
        // No labels on the server at all: "No label" would just match everything
        assertFalse(TorrentsUiState(torrents = listOf(torrent("a"))).drawerShowsLabels)
    }
}
