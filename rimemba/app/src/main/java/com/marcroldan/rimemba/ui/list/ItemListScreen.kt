package com.marcroldan.rimemba.ui.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.ui.list.components.ConfirmDeleteDialog
import com.marcroldan.rimemba.ui.list.components.EmptyState
import com.marcroldan.rimemba.ui.list.components.ItemRow

@Composable
fun ItemListScreen(
    viewModel: ItemListViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var itemPendienteDeBorrar by remember { mutableStateOf<Item?>(null) }

    if (items.isEmpty()) {
        EmptyState(modifier = modifier)
    } else {
        LazyColumn(modifier = modifier) {
            items(items, key = { it.id }) { item ->
                ItemRow(
                    item = item,
                    onToggleCompletado = { completado -> viewModel.setCompletado(item, completado) },
                    onDelete = { itemPendienteDeBorrar = item }
                )
                Divider()
            }
        }
    }

    itemPendienteDeBorrar?.let { item ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.delete(item)
                itemPendienteDeBorrar = null
            },
            onDismiss = { itemPendienteDeBorrar = null }
        )
    }
}
