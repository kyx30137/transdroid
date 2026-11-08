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
package org.transdroid.ui

import android.app.Application
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.transdroid.R

/**
 * Guards the Simplified Chinese translation: with the device locale set to zh-CN the app must
 * resolve the values-zh-rCN copies, placeholders included. A renamed folder, a dropped key or a
 * lost `%1$s` shows up here rather than in a screenshot.
 *
 * Key-by-key coverage is checked separately by tools/check_zh_strings.py; this test proves the
 * strings survive packaging and that the agreed terminology is what actually ships.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application: the real one starts WorkManager and notifications, which a resource
// lookup does not need (and Robolectric refuses outside an instrumented environment).
@Config(sdk = [35], qualifiers = "zh-rCN", application = Application::class)
class ChineseStringsTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    @Test
    fun torrentListUsesTheAgreedVocabulary() {
        assertEquals("任务", context.getString(R.string.torrents_title))
        assertEquals("添加任务", context.getString(R.string.torrents_add))
        assertEquals("做种中", context.getString(R.string.filter_seeding))
        assertEquals("已暂停", context.getString(R.string.filter_paused))
        assertEquals("出错", context.getString(R.string.filter_error))
        assertEquals("分享率", context.getString(R.string.sort_ratio))
        assertEquals("设置下载位置", context.getString(R.string.details_set_location))
        assertEquals("Tracker (3)", context.getString(R.string.details_tab_trackers, 3))
    }

    @Test
    fun placeholdersAndPluralsStillFormat() {
        assertEquals("已连接 · 3 个任务", context.getString(R.string.drawer_connected_count, 3))
        assertEquals("2 个任务已完成", context.resources.getQuantityString(R.plurals.notification_finished_title, 2, 2))
        assertEquals("用于“电影”", context.getString(R.string.details_set_location_subtitle, "电影"))
        assertEquals("62%", context.getString(R.string.details_peer_progress, 62))
        assertEquals("1.4 GB / 2.0 GB · 62%", context.getString(R.string.torrent_row_progress, "1.4 GB", "2.0 GB", 62))
    }

    @Test
    fun theOtherScreensAreTranslatedToo() {
        assertEquals("设置", context.getString(R.string.settings_title))
        assertEquals("RSS 订阅", context.getString(R.string.rss_title))
        assertEquals("搜索", context.getString(R.string.search_title))
        assertEquals("新变化", context.getString(R.string.changelog_title))
        assertEquals("2026 年 11 月", context.getString(R.string.changelog_date_2026_11))
        assertEquals("服务器的 HTTPS 证书不受信任", context.getString(R.string.error_ssl))
    }
}
