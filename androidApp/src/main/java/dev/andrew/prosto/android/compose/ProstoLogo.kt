package dev.andrew.prosto.android.compose

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.andrew.prosto.android.compose.utilities.norippleClickable
import dev.andrew.prosto.android.rememberProstoLogoPainter

@Composable
fun ProstoLogo(modifier: Modifier = Modifier) {
    val prostoPainter = rememberProstoLogoPainter()
    var tintIsWhite by remember { mutableStateOf(false) }

    Icon(
        modifier = modifier
            .norippleClickable {
                tintIsWhite = !tintIsWhite
            },
        painter = prostoPainter,
        contentDescription = "ПРОСТО",
        tint = if (tintIsWhite) Color.White else Color.Black
    )
}