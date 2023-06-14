package dev.andrew.prosto.android.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.andrew.prosto.repository.ProstoTicket

@Composable
fun TicketQRDialog(ticket: ProstoTicket, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            QRImage(Modifier.padding(10.dp), data = ticket.dataForQR)
        }
    }
}