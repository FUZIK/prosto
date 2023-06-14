package dev.andrew.prosto.controller

import dev.andrew.prosto.ProstoDestination
import dev.andrew.prosto.ProstoNavigator
import dev.andrew.prosto.StateUIController
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStore
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.CoworkingSource
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.updateState
import dev.andrew.prosto.usecase.GetTicketListUseCase
import dev.andrew.prosto.usecase.IsSignInRequiredUseCase
import dev.andrew.prosto.usecase.TicketListWithTodayIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

data class MainScreenState(
    val coworking: Coworking?, // какой пидорас это сделал? залупnull
    val coworkingIndex: Int,
    val coworkingList: List<Coworking>,
    val ticketListIsLoading: Boolean,
    val ticketListWithTodayIndex: TicketListWithTodayIndex,
    val isCoworkingListLoading: Boolean
) {
    constructor(): this(
        coworking = null,
        coworkingIndex = 0,
        coworkingList = emptyList(),
        ticketListIsLoading = false,
        ticketListWithTodayIndex = TicketListWithTodayIndex(),
        isCoworkingListLoading = true)
}

sealed interface MainScreenEvent {
    class OnCoworkingSelect(val coworking: Coworking): MainScreenEvent
    class OnClickTicketCreate: MainScreenEvent
    class OnClickTicket(val ticket: ProstoTicket): MainScreenEvent
}

class MainScreenController(
    private val coroutineScope: CoroutineScope,
    private val navigator: ProstoNavigator = ToporObject.navigator,
    coworkingSource: CoworkingSource = ToporObject.coworkingSource,
    private val getTicketListUseCase: GetTicketListUseCase = ToporObject.getTicketListUseCase,
    private val isSignInRequiredUseCase: IsSignInRequiredUseCase = ToporObject.isSignInRequiredUseCase,
    private val userSelectedCoworkingLocalStore: UserSelectedCoworkingLocalStore = ToporObject.userSelectedCoworkingLocalStore,
): StateUIController<MainScreenState, MainScreenEvent>(initial = MainScreenState()) {
    private val loadTicketListScope = coroutineScope + Job()
    private val localStoreScope = coroutineScope + Job()

    private fun loadTicketList() {
        loadTicketListScope.coroutineContext.cancelChildren()
        setState(state.value.copy(ticketListIsLoading = true))
        loadTicketListScope.launch {
            getTicketListUseCase.getTicketListWithTodayIndex(state.value.coworking!!).also { ticketListWithTodayIndex ->
                setState(state.value.copy(
                    ticketListWithTodayIndex = ticketListWithTodayIndex,
                    ticketListIsLoading = false
                ))
            }
        }
    }

    private fun saveSelectedCoworking(coworking: Coworking) {
        localStoreScope.launch {
            userSelectedCoworkingLocalStore.setCoworking(coworking)
        }
    }

    init {
        coroutineScope.launch {
            coworkingSource.getProsto().also { coworkingList ->
                val coworking = userSelectedCoworkingLocalStore.getCoworking() ?: coworkingList[0]
                val coworkingIndex = coworkingList.indexOfFirst { it.id == coworking.id }.let {
                    if (it == -1) {
                        0
                    } else {
                        it
                    }
                }
                updateState {
                    copy(
                        coworking = coworking,
                        coworkingIndex = coworkingIndex,
                        coworkingList = coworkingList,
                        isCoworkingListLoading = false)
                }
            }
        }
    }

    override fun reduce(state: MainScreenState, event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnCoworkingSelect -> {
                saveSelectedCoworking(event.coworking)
                updateState {
                    copy(coworking = event.coworking)
                }
                loadTicketList()
            }
            is MainScreenEvent.OnClickTicketCreate -> {
//                coroutineScope.launch {
//                    if (isSignInRequiredUseCase.isSignInRequired()) {
//                        navigator.navigateTo(
//                            ProstoDestination.AuthDialog(
//                                ProstoDestination.CreateTicketScreen(state.coworking!!)))
//                    } else {
//                        navigator.navigateTo(
//                            ProstoDestination.CreateTicketScreen(state.coworking!!))
//                    }
//                }
                navigator.navigateTo(
                    ProstoDestination.CreateTicketScreen(state.coworking!!))
            }
            is MainScreenEvent.OnClickTicket -> {
                navigator.navigateTo(ProstoDestination.TicketQRDialog(ticket = event.ticket))
            }
        }
    }
}
