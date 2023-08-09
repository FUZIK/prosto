package dev.andrew.prosto.android.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.andrew.prosto.android.R
import dev.andrew.prosto.controller.QRDialogEvent
import dev.andrew.prosto.controller.QrCodeDialogContoller
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket

@Composable
fun TicketQRDialog(
    coworking: Coworking,
    ticket: ProstoTicket
) {
    val coroutine = rememberCoroutineScope()
    val controller = remember(coroutine) { QrCodeDialogContoller(coroutine, coworking, ticket) }
    val state by controller.state.collectAsState()

    val localDensity = LocalDensity.current
    var prostoQRWidthDp: Dp? by remember {
        mutableStateOf(null)
    }
    var qrImageModifier: Modifier? by remember {
        mutableStateOf(null)
    }
    if (qrImageModifier == null) {
        prostoQRWidthDp?.let { dp ->
            qrImageModifier = Modifier.size(dp)
        }
    }

    Dialog(onDismissRequest = {
        controller.emitEvent(QRDialogEvent.OnBackPressed())
    }) {
        Card(
            Modifier, shape = RoundedCornerShape(12.dp, 12.dp, 26.dp, 26.dp)
        ) {
            val qrPadding = PaddingValues(10.dp, 10.dp, 10.dp, Dp.Hairline)
            if (state.qrIsLoading) {
                Column(
                    Modifier
                        .then(qrImageModifier ?: Modifier)
                        .align(Alignment.CenterHorizontally)
                        .padding(qrPadding)) {
                    if (state.isTurnpikeQRUnavailableNow) {
                        Box(Modifier.fillMaxSize()) {
                            Text(modifier = Modifier
                                .align(Alignment.Center),
                                text = "QR код будет доступен в интервале выбраного времени. Для новых броней время синхронизации может занять до 10 минут")
                        }
                    } else {
                        CircularProgressIndicator(
                            Modifier
                                .fillMaxSize())
                    }
                }
            } else {
                QRImage(
                    Modifier
                        .onGloballyPositioned { coordinates ->
                            prostoQRWidthDp = with(localDensity) {
                                coordinates.size.width.toDp() - qrPadding.calculateBottomPadding() - qrPadding.calculateTopPadding()
                            }
                        }
                        .then(qrImageModifier ?: Modifier)
                        .align(Alignment.CenterHorizontally)
                        .padding(qrPadding), data = state.qrData)
            }
            Row(
                Modifier.padding(9.dp)
            ) {
                Button(modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth(.5f),
                    enabled = true,
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(2.dp, 2.dp, 2.dp, 22.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(),
                    onClick = {
                        controller.emitEvent(QRDialogEvent.OnProstoQRSelect())
                    }) {
                    val mainVector = ImageVector.vectorResource(id = R.drawable.prst_icon)
                    val mainPainter = rememberVectorPainter(image = mainVector)
                    Icon(
                        modifier = Modifier.width(40.dp),
                        painter = mainPainter,
                        contentDescription = "QR для регистрации"
                    )
                }
                Spacer(modifier = Modifier.width(1.dp))
                Button(modifier = Modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth(1f),
                    enabled = state.isTurniketQREnabled,
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(2.dp, 2.dp, 22.dp, 2.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(),
                    onClick = {
                        controller.emitEvent(QRDialogEvent.OnTurniketQRSelect())
                    }) {
                    val mainVector = ImageVector.vectorResource(id = R.drawable.turniket_icon)
                    val mainPainter = rememberVectorPainter(image = mainVector)
                    Icon(
                        modifier = Modifier.width(40.dp),
                        painter = mainPainter,
                        contentDescription = "QR для турникета"
                    )
                }
            }
        }
    }
}