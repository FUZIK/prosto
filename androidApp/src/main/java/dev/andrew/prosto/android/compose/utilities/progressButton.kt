package dev.andrew.prosto.android.compose.utilities

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ProgressButton(onClick: () -> Unit,
                   modifier: Modifier = Modifier,
                   enabled: Boolean = true,
                   inProgress: Boolean,
                   shape: Shape = ButtonDefaults.shape,
                   colors: ButtonColors = ButtonDefaults.buttonColors(),
                   elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
                   border: BorderStroke? = null,
                   contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
                   interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
                   content: @Composable RowScope.() -> Unit) {
    Button(onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = if (inProgress) PaddingValues() else contentPadding,
        border = border,
        interactionSource = interactionSource) {
        AnimatedVisibility(visible = inProgress) {
            CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
        }
        AnimatedVisibility(visible = !inProgress) {
            this@Button.content()
        }
    }
}
