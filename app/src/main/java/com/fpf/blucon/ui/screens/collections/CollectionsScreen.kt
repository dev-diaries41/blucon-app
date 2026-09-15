package com.fpf.blucon.ui.screens.collections

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.fpf.blucon.ui.screens.collections.CollectionsViewModel.Companion.TOP_N
import kotlinx.coroutines.FlowPreview
import androidx.compose.ui.res.stringResource
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.device.DeviceCollection.Companion.UNLABELLED_COLLECTION
import com.fpf.blucon.events.CollectionEventType
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.action.ActionConfig
import com.fpf.blucon.ui.components.collections.DeviceCollectionsList
import com.fpf.blucon.ui.components.common.ActionBar
import com.fpf.blucon.ui.components.common.SelectionHeaderRow
import com.fpf.blucon.ui.components.modals.SelectorModal
import com.fpf.blucon.ui.components.modals.TextInputModal
import com.fpf.smartscan.ui.components.common.SlideRevealBox
import org.koin.compose.viewmodel.koinViewModel


@OptIn(FlowPreview::class)
@Composable
fun CollectionsScreen(
    onTopBarChange: (TopBarState) -> Unit,
    onViewCollection: (DeviceCollection) -> Unit,
    viewModel: CollectionsViewModel = koinViewModel(),
    ) {

    val actionBarHeight = 70

    val state by viewModel.state.collectAsState()
    val collections by viewModel.clusterCollections.collectAsState()

    val isCollectionVisible = collections.isNotEmpty()

    val context = LocalContext.current

    // actions
    var showMenu by remember { mutableStateOf(false) }
    var isRenamingCollection by remember { mutableStateOf(false) }
    var isMergingCollections by remember { mutableStateOf(false) }
    var isDeletingCollection by remember { mutableStateOf(false) }
    val isActionBarVisible = state.selection.isSelecting && state.selection.selectedCount > 0

    val actionBarActions: List<ActionConfig> = listOf(
        ActionConfig(label = stringResource(R.string.merge), { isMergingCollections = true }, enabled = !state.loading, icon = Icons.Filled.Merge),
        ActionConfig( label = stringResource(R.string.rename), { isRenamingCollection = true }, enabled = state.selection.selectedItems.size == 1, icon = Icons.Filled.DriveFileRenameOutline),
//        ActionConfig(label = stringResource(R.string.delete), { isDeletingCollection = true }, enabled = state.collectionType == CollectionType.TAG, icon = Icons.Filled.Delete)
    )

    val spaceNotAllowedMessage = stringResource(R.string.msg_space_not_allowed)

    var offset by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxCollapsablePx = with(density) { 70.dp.toPx() }.toInt()

    LaunchedEffect(state.collectToView) {
        state.collectToView?.let{
            onViewCollection(it)
            viewModel.onAction(CollectionAction.SetCollectionToView(null))
        }
    }

    // Handle action result via events
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when(event.type){
                CollectionEventType.MERGE -> {
                    event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
                }
                CollectionEventType.RENAME -> {
                    event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
                }
                CollectionEventType.DELETE -> {
                    event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
                }
            }
        }
    }

    val screenTitle = stringResource(R.string.title_collection)

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
            )
        )
    }

    BackHandler(enabled = state.selection.isSelecting) {
        viewModel.onAction(CollectionAction.ResetSelection)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {

            SlideRevealBox(
                isVisible = state.selection.isSelecting,
                reverse = true,
                offsetPx = offset,
                modifier = Modifier
                    .zIndex(1f)
                    .heightIn(max = maxCollapsablePx.dp)
                    .padding(bottom = 8.dp)
            ) {
                SelectionHeaderRow (
                    selectedCount = state.selection.selectedCount,
                    checked = (state.selection.selectAll && state.selection.excludedItems.isEmpty()) || (state.selection.selectedItems.size == state.totalCollections),
                    onSelectAllChange = {viewModel.onAction(CollectionAction.SetSelectAll(it))}
                )
            }


            if(state.totalCollections > TOP_N) {
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = {viewModel.onAction(CollectionAction.ToggleViewAllCollections)}
                ) {
                    Text(
                        text = if (state.showAllCollections) "Show less" else "Show all",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }else{
                Spacer(modifier = Modifier.height(16.dp))
            }

            DeviceCollectionsList(
                isVisible = isCollectionVisible,
                numGridColumns = 2,
                items = collections,
                isSelecting = state.selection.isSelecting,
                isChecked = { it in state.selection.selectedItems || (state.selection.selectAll && it !in state.selection.excludedItems)},
                onItemClick = {
                    if(state.selection.isSelecting){
                        viewModel.onAction(CollectionAction.ToggleSelectedCollection(it))
                    }else{
                        viewModel.onAction(CollectionAction.SetCollectionToView(it))
                    }
                              },
                onItemLongClick = {
                    viewModel.onAction(CollectionAction.ToggleSelectionMode)
                    viewModel.onAction(CollectionAction.ToggleSelectedCollection(it))
                    offset = 0
                },
                onOffsetChange = {  offset = it },
                maxCollapsePx = maxCollapsablePx,
            )

            EmptyCollectionScreen(
                isVisible = !isCollectionVisible,
            )
        }


        SlideRevealBox(
            isVisible = isActionBarVisible,
            offsetPx = offset,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .then(
                    if (offset != 0)
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                    else Modifier
                )
        ) {

            ActionBar(
                actions = actionBarActions,
                modifier = Modifier.height(actionBarHeight.dp),
            )
        }
    }

    TextInputModal(
        isVisible = isRenamingCollection,
        title=stringResource(R.string.rename),
        placeholder = stringResource(R.string.placeholders_collection_name),
        onClose = { isRenamingCollection = false },
        onConfirm = {
                newName -> viewModel.onAction(CollectionAction.RenameCollection(newName))
            isRenamingCollection = false
        },
        leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = "Tag", tint = MaterialTheme.colorScheme.primary) },
        onValueChange = {
            if (!it.text.contains(" ")) {
                true
            } else {
                Toast.makeText(context, spaceNotAllowedMessage, Toast.LENGTH_SHORT).show()
                false
            }
        }
    )

    if (isMergingCollections) {
        val labelledCollections = state.selection.selectedItems.sortedByDescending { it.size}.map { it.name to it }.filterNot { it.first == UNLABELLED_COLLECTION }
        var useSelectorInput by remember { mutableStateOf(labelledCollections.isNotEmpty()) }

        if (useSelectorInput) {
            SelectorModal(
                isVisible = labelledCollections.isNotEmpty(),
                initialOption = labelledCollections.first().second,
                title = stringResource(R.string.merge),
                label = stringResource(R.string.collections_primary_collection_label),
                options = labelledCollections,
                onConfirm = {
                    viewModel.onAction(CollectionAction.MergeCollections(it.name))
                    isMergingCollections = false
                },
                onClose = { isMergingCollections = false }
            )
        }else{
            TextInputModal(
                isVisible = true,
                title = stringResource(R.string.merge),
                placeholder = stringResource(R.string.placeholders_collection_name),
                onClose = { isMergingCollections = false },
                onConfirm =  { newName ->
                    viewModel.onAction(CollectionAction.MergeCollections(newName, isNewMergedLabel = true))
                    isMergingCollections = false
                },
                leadingIcon = { Icon(Icons.Filled.Tag, contentDescription = "Tag", tint = MaterialTheme.colorScheme.primary) },
                onValueChange = {
                    if (!it.text.contains(" ")) {
                        true
                    } else {
                        Toast.makeText(context, spaceNotAllowedMessage, Toast.LENGTH_SHORT).show()
                        false
                    }
                }
            )
        }
    }

    if ( isDeletingCollection) {
        val count = state.selection.selectedCount
        val alertTitle = stringResource(R.string.collections_delete_collections_alert_title)
        val alertDescription = stringResource(
            R.string.collections_delete_collections_alert_description,
            count,
            pluralStringResource(R.plurals.collection_count, count)
        )
        AlertDialog(
            onDismissRequest = { },
            title = { Text(alertTitle) },
            text = { Text(alertDescription) },
            dismissButton = {
                TextButton(onClick = { isDeletingCollection = false })
                { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onAction(CollectionAction.DeleteCollections)
                    isDeletingCollection = false
                })
                { Text(stringResource(R.string.confirm)) }
            }
        )
    }
}