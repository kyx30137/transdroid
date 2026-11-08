package org.transdroid.protocol.qbittorrent

import java.io.IOException
import java.net.URLDecoder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.transdroid.protocol.DaemonConfig
import org.transdroid.protocol.DaemonType
import org.transdroid.protocol.DownloadLocationInfo
import org.transdroid.protocol.transmission.TransmissionAdapter

class DownloadLocationContractTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() { server = MockWebServer().apply { start() } }
    @After fun tearDown() { server.shutdown() }

    private fun config() = DaemonConfig(DaemonType.QBITTORRENT, server.hostName, server.port)
    private fun adapter(client: OkHttpClient = OkHttpClient()) = QbittorrentAdapter(config(), client)
    private fun response(body: String) = MockResponse().setBody(body)

    @Test fun `preferences and category paths use official field casing and shared session`() = runTest {
        server.enqueue(response("Ok.").setHeader("Set-Cookie", "SID=test; path=/"))
        server.enqueue(response("""{"save_path":"/downloads/default/","auto_tmm_enabled":true,"temp_path":"/incomplete"}"""))
        server.enqueue(response("""{"Video":{"name":"Video","savePath":"/downloads/影片/"},"Books":{"savePath":"/books"},"Empty":{"savePath":""}}"""))
        val info = QbittorrentAdapter(config().copy(username = "test", password = "test"), OkHttpClient()).downloadLocationInfo()
        assertEquals(DownloadLocationInfo("/downloads/default/", mapOf("Video" to "/downloads/影片/", "Books" to "/books"), true, true, true), info)
        assertEquals("/api/v2/auth/login", server.takeRequest().path)
        for (path in listOf("/api/v2/app/preferences", "/api/v2/torrents/categories")) {
            val request = server.takeRequest()
            assertEquals(path, request.path)
            assertEquals("GET", request.method)
            assertEquals("SID=test", request.getHeader("Cookie"))
        }
    }

    @Test fun `disabled automatic management is distinct from unknown`() = runTest {
        server.enqueue(response("""{"save_path":"/default","auto_tmm_enabled":false}"""))
        server.enqueue(response("{}"))
        assertEquals(false, adapter().downloadLocationInfo().automaticManagement)
    }

    @Test fun `successful empty sources remain available with unknown automatic management`() = runTest {
        server.enqueue(response("{}"))
        server.enqueue(response("{}"))
        assertEquals(DownloadLocationInfo(defaultAvailable = true, categoriesAvailable = true), adapter().downloadLocationInfo())
    }

    @Test fun `blank paths omitted without resolving or trimming reported paths`() = runTest {
        server.enqueue(response("""{"save_path":"  "}"""))
        server.enqueue(response("""{"blank":{"savePath":"  "},"missing":{},"relative":{"savePath":"relative/path"},"spaces":{"savePath":"/has spaces/ "}}"""))
        val info = adapter().downloadLocationInfo()
        assertNull(info.defaultPath)
        assertEquals(mapOf("relative" to "relative/path", "spaces" to "/has spaces/ "), info.categoryPaths)
        assertTrue(info.defaultAvailable && info.categoriesAvailable)
    }

    @Test fun `preferences HTTP failure preserves categories`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(response("""{"Video":{"savePath":"/video"}}"""))
        assertEquals(DownloadLocationInfo(categoryPaths = mapOf("Video" to "/video"), categoriesAvailable = true), adapter().downloadLocationInfo())
        assertEquals(2, server.requestCount)
    }

    @Test fun `categories HTTP failure preserves preferences and automatic management`() = runTest {
        server.enqueue(response("""{"save_path":"/default","auto_tmm_enabled":true}"""))
        server.enqueue(MockResponse().setResponseCode(404))
        assertEquals(DownloadLocationInfo(defaultPath = "/default", automaticManagement = true, defaultAvailable = true), adapter().downloadLocationInfo())
    }

    @Test fun `malformed preferences preserve categories`() = runTest {
        server.enqueue(response("not json"))
        server.enqueue(response("""{"Books":{"savePath":"/books"}}"""))
        assertEquals(DownloadLocationInfo(categoryPaths = mapOf("Books" to "/books"), categoriesAvailable = true), adapter().downloadLocationInfo())
    }

    @Test fun `malformed categories preserve preferences`() = runTest {
        server.enqueue(response("""{"save_path":"/default"}"""))
        server.enqueue(response("[]"))
        assertEquals(DownloadLocationInfo(defaultPath = "/default", defaultAvailable = true), adapter().downloadLocationInfo())
    }

    @Test fun `both failed sources return unknown flags`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(500))
        assertEquals(DownloadLocationInfo(), adapter().downloadLocationInfo())
    }

    @Test fun `network failure of preferences still fetches categories`() = runTest {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            if (chain.request().url.encodedPath.endsWith("/preferences")) throw IOException("test network failure")
            chain.proceed(chain.request())
        }.build()
        server.enqueue(response("""{"Books":{"savePath":"/books"}}"""))
        assertEquals(DownloadLocationInfo(categoryPaths = mapOf("Books" to "/books"), categoriesAvailable = true), adapter(client).downloadLocationInfo())
        assertEquals("/api/v2/torrents/categories", server.takeRequest().path)
    }

    @Test fun `cancellation propagates without fetching another source`() = runTest {
        var attempts = 0
        val client = OkHttpClient.Builder().addInterceptor {
            attempts++
            throw CancellationException("test cancellation")
        }.build()
        try {
            adapter(client).downloadLocationInfo()
            fail("Expected cancellation")
        } catch (expected: CancellationException) {
            assertEquals("test cancellation", expected.message)
        }
        assertEquals(1, attempts)
    }

    @Test fun `unsupported adapter returns unknown sources without network calls`() = runTest {
        val adapter = TransmissionAdapter(config().copy(type = DaemonType.TRANSMISSION), OkHttpClient())
        assertEquals(DownloadLocationInfo(), adapter.downloadLocationInfo())
        assertEquals(0, server.requestCount)
    }

    @Test fun `torrent download directory only uses save_path never content_path`() = runTest {
        server.enqueue(response("""[
          {"hash":"multi","save_path":"/downloads","content_path":"/downloads/folder"},
          {"hash":"single","save_path":"/downloads","content_path":"/downloads/file.iso"},
          {"hash":"missing","content_path":"/downloads/file.iso"},
          {"hash":"null","save_path":null,"content_path":"/downloads/file.iso"},
          {"hash":"blank","save_path":"","content_path":"/downloads/file.iso"}
        ]"""))
        assertEquals(listOf("/downloads", "/downloads", null, null, ""), adapter().listTorrents().map { it.downloadDir })
    }

    @Test fun `URL add omits savepath and autoTMM for null empty and whitespace paths`() = runTest {
        val adapter = adapter()
        for (path in listOf(null, "", " \t\n")) {
            server.enqueue(response("Ok."))
            adapter.addByUrl("magnet:?xt=urn:btih:test", downloadLocation = path)
            val request = server.takeRequest()
            assertEquals("/api/v2/torrents/add", request.path)
            val fields = request.body.readUtf8().split('&').map { it.substringBefore('=') }
            assertFalse(fields.contains("savepath"))
            assertFalse(fields.contains("autoTMM"))
        }
    }

    @Test fun `file add omits savepath and autoTMM for null empty and whitespace paths`() = runTest {
        val adapter = adapter()
        for (path in listOf(null, "", " \t\n")) {
            server.enqueue(response("Ok."))
            adapter.addByFile("test.torrent", byteArrayOf(1, 2, 3), downloadLocation = path)
            val request = server.takeRequest()
            assertEquals("/api/v2/torrents/add", request.path)
            assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
            val body = request.body.readUtf8()
            assertTrue(body.contains("name=\"torrents\"; filename=\"test.torrent\""))
            assertFalse(body.contains("name=\"savepath\""))
            assertFalse(body.contains("name=\"autoTMM\""))
        }
    }

    @Test fun `explicit URL and file paths disable automatic management even when server default is true`() = runTest {
        val adapter = adapter()
        server.enqueue(response("""{"save_path":"/server/default","auto_tmm_enabled":true}"""))
        server.enqueue(response("{}"))
        assertEquals(true, adapter.downloadLocationInfo().automaticManagement)
        server.takeRequest()
        server.takeRequest()
        val path = "/downloads/影片 and books/"
        server.enqueue(response("Ok."))
        adapter.addByUrl("magnet:?xt=urn:btih:test", downloadLocation = path)
        val fields = server.takeRequest().body.readUtf8().split('&').associate {
            it.substringBefore('=') to URLDecoder.decode(it.substringAfter('='), "UTF-8")
        }
        assertEquals(path, fields["savepath"])
        assertEquals("false", fields["autoTMM"])
        server.enqueue(response("Ok."))
        adapter.addByFile("test.torrent", byteArrayOf(1, 2, 3), downloadLocation = path)
        val request = server.takeRequest()
        val boundary = request.getHeader("Content-Type")!!.substringAfter("boundary=")
        val parts = request.body.readUtf8().split("--$boundary").filter { it.contains("\r\n\r\n") }
        fun partValue(name: String): String = parts.single {
            it.substringBefore("\r\n\r\n").contains("name=\"$name\"")
        }.substringAfter("\r\n\r\n").removeSuffix("\r\n")
        assertEquals(path, partValue("savepath"))
        assertEquals("false", partValue("autoTMM"))
    }
}
