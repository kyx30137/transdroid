package org.transdroid.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.transdroid.protocol.DownloadLocationInfo

/** Local display preferences only. Never a torrent tag or a command to move data. */
@Serializable
data class PathAliases(val names: Map<String, String> = emptyMap())

enum class PathAliasError { INVALID_PATH, INVALID_NAME, DUPLICATE_NAME, STORAGE }

object DownloadPaths {
    private val json = Json { ignoreUnknownKeys = true }

    /** Keep Linux case and internal separators intact; / and drive roots remain roots. */
    fun normalize(path: String): String {
        val value = path
        if (value == "/" || value.matches(Regex("^[A-Za-z]:[/\\\\]$"))) return value
        return value.trimEnd('/').ifEmpty { if (value.startsWith('/')) "/" else "" }
    }

    fun isAbsolute(path: String): Boolean = path.startsWith('/') ||
        path.matches(Regex("^[A-Za-z]:[/\\\\].*")) || path.startsWith("\\\\")

    fun isValid(path: String): Boolean = path.isNotBlank() && isAbsolute(path) && path.none { it.isISOControl() }

    fun label(path: String, aliases: Map<String, String>): String {
        val key = normalize(path)
        return aliases[key] ?: key.substringAfterLast('/').substringAfterLast('\\').ifEmpty { key }
    }

    fun validateAlias(path: String, name: String, aliases: Map<String, String>): PathAliasError? {
        if (!isValid(normalize(path))) return PathAliasError.INVALID_PATH
        val title = name.trim()
        if (title.length > 40 || title.any { it.isISOControl() }) return PathAliasError.INVALID_NAME
        if (title.isNotEmpty() && aliases.any { (key, value) ->
                key != normalize(path) && value.equals(title, ignoreCase = true)
            }) return PathAliasError.DUPLICATE_NAME
        return null
    }

    fun decode(encoded: String?): Map<String, String> = try {
        encoded?.let { json.decodeFromString<PathAliases>(it).names } ?: emptyMap()
    } catch (_: Exception) { emptyMap() }

    fun encode(aliases: Map<String, String>): String = json.encodeToString(PathAliases(aliases))

    fun submissionPath(explicit: String?): String? = explicit?.let(::normalize)?.takeIf { it.isNotBlank() }

    /** No content_path, guessed parents, or relative category paths. Preserve first-source order. */
    fun candidates(info: DownloadLocationInfo, recent: List<String>, known: List<String>, aliases: Map<String, String>): List<String> =
        (listOfNotNull(info.defaultPath) + recent + aliases.keys + known + info.categoryPaths.values)
            .map(::normalize).filter(::isValid).distinct()
}

/** A visible default is informational until the user chooses a path. */
data class DownloadPathSelection(val text: String = "", val explicit: Boolean = false) {
    fun requestPath(): String? = if (explicit) DownloadPaths.normalize(text).ifBlank { null } else null
    fun displayPath(defaultPath: String?): String = if (explicit) text else defaultPath.orEmpty()
    fun valid(): Boolean = !explicit || text.isBlank() || DownloadPaths.isValid(DownloadPaths.normalize(text))
}
