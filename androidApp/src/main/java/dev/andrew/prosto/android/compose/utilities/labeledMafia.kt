package dev.andrew.prosto.android.compose.utilities

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

fun RequiresLabelSpan(label: String): AnnotatedString {
    return buildAnnotatedString {
        append(label)
        pushStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold))
        append(" *")
    }
}

@Composable
fun LabeledColumn(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 2.dp),
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        content()
    }
}

@Composable
fun LabeledColumn(label: AnnotatedString, content: @Composable () -> Unit) {
    Column {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 2.dp),
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        content()
    }
}

@Composable
fun LabeledCheckBox(
    checked: Boolean,
    enabled: Boolean = true,
    label: String,
    onValueChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                enabled = enabled,
                value = checked,
                onValueChange = onValueChange,
                role = Role.Checkbox
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            enabled = enabled,
            checked = checked,
            onCheckedChange = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
            color = if (enabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.38f
            )
        )
    }
}
