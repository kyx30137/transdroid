package org.transdroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.transdroid.R
import org.transdroid.data.DownloadPaths
import org.transdroid.data.PathAliasError
import org.transdroid.protocol.DownloadLocationInfo

data class DownloadPathOptions(
    val profileId: String? = null,
    val info: DownloadLocationInfo = DownloadLocationInfo(),
    val knownPaths: List<String> = emptyList(),
    val recentPaths: List<String> = emptyList(),
    val aliases: Map<String, String> = emptyMap(),
    val loading: Boolean = false,
    val refresh: () -> Unit = {},
    val saveAlias: (String, String, (PathAliasError?) -> Unit) -> Unit = { _, _, done -> done(PathAliasError.STORAGE) },
)
val LocalDownloadPaths = staticCompositionLocalOf { DownloadPathOptions() }

/** null is the server-managed default; a displayed default is never an explicit override. */
@Composable
fun DownloadPathPicker(
    location: String?,
    onLocationChange: (String?) -> Unit,
    options: DownloadPathOptions = LocalDownloadPaths.current,
) {
    var editing by remember(options.profileId) { mutableStateOf<String?>(null) }
    val candidates = remember(options.info, options.knownPaths, options.recentPaths, options.aliases) {
        DownloadPaths.candidates(options.info, options.recentPaths, options.knownPaths, options.aliases)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.path_choose), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = options.refresh, enabled = !options.loading) { Text(stringResource(R.string.path_refresh)) }
        }
        if (options.loading) Text(stringResource(R.string.path_loading), style = MaterialTheme.typography.bodySmall)
        else if (!options.info.defaultAvailable || options.info.defaultPath.isNullOrBlank()) {
            Text(stringResource(R.string.path_unavailable), style = MaterialTheme.typography.bodySmall)
        }
        Surface(
            onClick = { onLocationChange(null) },
            color = if (location == null) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().testTag("path-server-default"),
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(stringResource(R.string.path_server_default), style = MaterialTheme.typography.titleSmall)
                Text(options.info.defaultPath ?: stringResource(R.string.path_default_unknown), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.path_keep_management), style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedTextField(
            value = location ?: options.info.defaultPath.orEmpty(),
            onValueChange = { onLocationChange(it) },
            label = { Text(stringResource(R.string.add_location_label)) },
            textStyle = MaterialTheme.typography.bodyLarge,
            isError = location != null && !DownloadPaths.isValid(location),
            minLines = 1, maxLines = 4,
            modifier = Modifier.fillMaxWidth().testTag("path-manual-input"),
        )
        if (location != null) Text(
            stringResource(if (DownloadPaths.isValid(location)) R.string.path_explicit_note else R.string.path_invalid),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(stringResource(R.string.path_candidates), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(if (options.info.categoriesAvailable) R.string.path_candidates_note else R.string.path_candidates_unavailable), style = MaterialTheme.typography.bodySmall)
        if (candidates.isNotEmpty()) Column(
            Modifier.fillMaxWidth().heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            candidates.forEach { path ->
                Surface(
                    color = if (location != null && DownloadPaths.normalize(location) == path) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column(
                            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { onLocationChange(path) }.padding(vertical = 8.dp),
                        ) {
                            Text(DownloadPaths.label(path, options.aliases), style = MaterialTheme.typography.titleMedium)
                            Text(path, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        if (options.profileId != null) TextButton(onClick = { editing = path }) {
                            Text(stringResource(R.string.path_alias_edit))
                        }
                    }
                }
            }
        }
        if (location != null && DownloadPaths.isValid(location) && options.profileId != null) {
            TextButton(onClick = { editing = DownloadPaths.normalize(location) }) { Text(stringResource(R.string.path_alias_edit)) }
        }
    }
    editing?.let { path -> PathAliasEditor(path, options) { editing = null } }
}

@Composable
private fun PathAliasEditor(path: String, options: DownloadPathOptions, onDismiss: () -> Unit) {
    var name by remember(path) { mutableStateOf(options.aliases[DownloadPaths.normalize(path)].orEmpty()) }
    var error by remember { mutableStateOf<PathAliasError?>(null) }
    var saving by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.path_alias_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SelectionContainer { Text(path, style = MaterialTheme.typography.bodySmall) }
                OutlinedTextField(value = name, onValueChange = { name = it; error = null },
                    label = { Text(stringResource(R.string.path_alias_name)) },
                    singleLine = true, modifier = Modifier.testTag("path-alias-input"))
                Text(stringResource(R.string.path_alias_local), style = MaterialTheme.typography.bodySmall)
                error?.let { issue -> Text(stringResource(when (issue) {
                    PathAliasError.INVALID_PATH -> R.string.path_invalid
                    PathAliasError.INVALID_NAME -> R.string.path_alias_long
                    PathAliasError.DUPLICATE_NAME -> R.string.path_alias_duplicate
                    PathAliasError.STORAGE -> R.string.path_alias_storage
                }), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                error = DownloadPaths.validateAlias(path, name, options.aliases)
                if (error == null) {
                    saving = true
                    options.saveAlias(path, name) { result -> saving = false; error = result; if (result == null) onDismiss() }
                }
            }) { Text(stringResource(R.string.details_set_location_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.details_cancel)) } },
    )
}

@Composable
fun TorrentPathChip(path: String?, aliases: Map<String, String> = LocalDownloadPaths.current.aliases) {
    val options = LocalDownloadPaths.current
    var expanded by remember(path, options.profileId) { mutableStateOf(false) }
    var editing by remember(path, options.profileId) { mutableStateOf(false) }
    val known = !path.isNullOrBlank()
    Surface(onClick = { if (known) expanded = true }, enabled = known,
        color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(top = 4.dp).heightIn(min = 48.dp).testTag("torrent-path-chip")) {
        Text(if (known) DownloadPaths.label(path!!, aliases) else stringResource(R.string.path_unknown),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp))
    }
    if (expanded && known) AlertDialog(
        onDismissRequest = { expanded = false }, title = { Text(stringResource(R.string.path_full_title)) },
        text = { SelectionContainer { Text(path!!, style = MaterialTheme.typography.bodyMedium) } },
        confirmButton = { TextButton(onClick = { expanded = false }) { Text(stringResource(R.string.details_cancel)) } },
        dismissButton = { if (options.profileId != null) TextButton(onClick = { expanded = false; editing = true }) { Text(stringResource(R.string.path_alias_edit)) } },
    )
    if (editing && known) PathAliasEditor(path!!, options) { editing = false }
}
