package com.marcroldan.rimemba.ui.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.marcroldan.rimemba.R
import com.marcroldan.rimemba.domain.model.Item
import com.marcroldan.rimemba.domain.model.TipoItem
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val fechaFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    .withLocale(Locale("es", "ES"))

@Composable
fun ItemRow(
    item: Item,
    onToggleCompletado: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val completadoDescription = stringResource(R.string.mark_completed_content_description)
        Checkbox(
            checked = item.completado,
            onCheckedChange = onToggleCompletado,
            modifier = Modifier.semantics { contentDescription = completadoDescription }
        )

        Icon(
            imageVector = if (item.tipo == TipoItem.RECORDATORIO) Icons.Filled.Notifications else Icons.Filled.EditNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.texto,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.completado) TextDecoration.LineThrough else null
            )
            item.fechaHora?.let { fecha ->
                Text(
                    text = fecha.format(fechaFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete_item_content_description)
            )
        }
    }
}
