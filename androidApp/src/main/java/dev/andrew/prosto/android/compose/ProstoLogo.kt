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

// source: https://prostospb.team/brandbook
private val COLORS = listOf(
    Color.Black,
    Color(0xFFED0082),
    Color(0xFF6200EA),
    Color(0xFFDAEF14),
    Color(0xFF02D2EE),
    Color(0xFFE6194C),
    Color.White,
)

@Composable
fun ProstoLogo(modifier: Modifier = Modifier) {
    val painter = rememberProstoLogoPainter()
    var current by remember { mutableStateOf(0) }
    Icon(
        modifier = modifier
            .norippleClickable {
                current = ++current % COLORS.size
            },
        painter = painter,
        contentDescription = "ПРОСТО",
        tint = COLORS[current]
    )
}