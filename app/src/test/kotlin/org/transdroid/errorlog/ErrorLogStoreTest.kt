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
package org.transdroid.errorlog

import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ErrorLogStoreTest {

    private lateinit var file: File

    @Before
    fun setUp() {
        file = File.createTempFile("errorlog", ".txt")
        file.delete()
    }

    @After
    fun tearDown() {
        file.setReadable(true)
        file.delete()
    }

    @Test
    fun keepsEntriesInOrderUnderTheCap() {
        val store = ErrorLogStore(file, maxEntries = 5)
        store.append("one")
        store.append("two")
        store.append("three")

        assertEquals(listOf("one", "two", "three"), store.entries())
    }

    @Test
    fun evictsTheOldestEntryFirstOnceOverTheCap() {
        val store = ErrorLogStore(file, maxEntries = 3)
        store.append("one")
        store.append("two")
        store.append("three")
        store.append("four")

        assertEquals(listOf("two", "three", "four"), store.entries())
    }

    @Test
    fun aMultiLineCrashDumpCountsAsASingleEntry() {
        val store = ErrorLogStore(file, maxEntries = 2)
        store.append("one")
        store.append("crash header\n  at Foo.bar\n  at Baz.qux")

        assertEquals(listOf("one", "crash header\n  at Foo.bar\n  at Baz.qux"), store.entries())
    }

    @Test
    fun persistsAcrossReconstructionSurvivingASimulatedProcessRestart() {
        val first = ErrorLogStore(file, maxEntries = 5)
        first.append("one")
        first.append("two")

        val second = ErrorLogStore(file, maxEntries = 5)

        assertEquals(listOf("one", "two"), second.entries())
    }

    @Test
    fun aMissingFileStartsEmptyWithoutThrowing() {
        val store = ErrorLogStore(file, maxEntries = 5)

        assertTrue(store.entries().isEmpty())
    }

    @Test
    fun anUnreadableFileStartsEmptyWithoutThrowing() {
        file.writeText("one" + "\n\n" + "two")
        file.setReadable(false)

        // setReadable(false) returning true does NOT mean the read actually fails: as root, and
        // under some Windows ACL setups, the bit flips while the file stays readable. Probe the
        // real outcome rather than trusting the return value, otherwise this asserts the opposite
        // of what the platform does.
        if (runCatching { file.readText() }.isFailure) {
            val store = ErrorLogStore(file, maxEntries = 5)
            assertTrue(store.entries().isEmpty())
        }
        // Environments that cannot deny read access at all (root) fall through to the
        // unconditional case below, which covers the same store code path everywhere.
    }

    @Test
    fun aPathThatCannotBeReadStartsEmptyWithoutThrowing() {
        // Anything that exists but cannot be read as text drives the store's corrupt-log path.
        // A directory is the one such path every JVM - root included - agrees on, so this
        // asserts unconditionally instead of depending on permission bits.
        assertTrue("could not create the not-a-file fixture", file.mkdirs())

        val store = ErrorLogStore(file, maxEntries = 5)

        assertTrue(store.entries().isEmpty())
        // Writing to an unusable path must stay silent too: logging may never take the app down.
        store.append("one")
        assertEquals(listOf("one"), store.entries())
    }
}
