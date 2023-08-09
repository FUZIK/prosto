package dev.andrew.prosto.controller

import dev.andrew.prosto.StateUIController
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.navigation.ProstoNavigator
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.repository.ProstoTicketSource
import dev.andrew.prosto.updateState
import dev.andrew.prosto.usecase.TicketTurniketKeyUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

enum class SelectedQRType {
    PROSTO,
    TURNIKET
}

data class QRDialogState(
    val qrData: String,
    val isTurniketQREnabled: Boolean,
    val selectedQRType: SelectedQRType,
    val qrIsLoading: Boolean,
    val isTurnpikeQRUnavailableNow: Boolean
) {
    constructor(coworking: Coworking, ticket: ProstoTicket) : this(
        qrData = ticket.qrDataProsto,
        selectedQRType = SelectedQRType.PROSTO,
        isTurniketQREnabled = coworking.isSupportTurniket,
        qrIsLoading = false,
        isTurnpikeQRUnavailableNow = false,
    )
}

sealed interface QRDialogEvent {
    class OnProstoQRSelect : QRDialogEvent
    class OnTurniketQRSelect : QRDialogEvent
    class OnBackPressed : QRDialogEvent
}

class QrCodeDialogContoller(
    coroutine: CoroutineScope,
    private val coworking: Coworking,
    private val ticket: ProstoTicket,
    private val navigator: ProstoNavigator = ToporObject.navigator,
    private val ticketTurniketKeyUseCase: TicketTurniketKeyUseCase = ToporObject.ticketTurniketKeyUseCase
) : StateUIController<QRDialogState, QRDialogEvent>(QRDialogState(coworking, ticket)) {
    private val ticketLoadScope = coroutine + Job()

    private fun cancelLoadTunrniketQR() {
        ticketLoadScope.coroutineContext.cancelChildren()
    }

    private fun loadTurniketQR() {
        cancelLoadTunrniketQR()
        ticketLoadScope.launch {
            val universalTurniketKey = ticketTurniketKeyUseCase.getTurniketKey(coworking, ticket)
            if (universalTurniketKey == null) {
                updateState {
                    copy(
                        qrIsLoading = true,
                        isTurnpikeQRUnavailableNow = true
                    )
                }
            } else {
                updateState {
                    copy(
                        qrData = universalTurniketKey,
                        qrIsLoading = false,
                        isTurnpikeQRUnavailableNow = false
                    )
                }
            }
        }
    }

    override fun reduce(state: QRDialogState, event: QRDialogEvent) {
        cancelLoadTunrniketQR()
        when (event) {
            is QRDialogEvent.OnProstoQRSelect -> {
                if (state.selectedQRType != SelectedQRType.PROSTO) {
                    updateState {
                        copy(
                            qrData = ticket.qrDataProsto,
                            selectedQRType = SelectedQRType.PROSTO,
                            isTurnpikeQRUnavailableNow = false,
                        )
                    }
                }
            }
            is QRDialogEvent.OnTurniketQRSelect -> {
                if (state.selectedQRType != SelectedQRType.TURNIKET) {
                    updateState {
                        copy(
                            qrData = "",
                            selectedQRType = SelectedQRType.TURNIKET,
                            qrIsLoading = true
                        )
                    }
                   loadTurniketQR()
                }
            }
            is QRDialogEvent.OnBackPressed -> navigator.navigateBack()
        }
    }
}