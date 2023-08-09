package dev.andrew.prosto.controller

import dev.andrew.prosto.navigation.ProstoDestination
import dev.andrew.prosto.navigation.ProstoNavigator
import dev.andrew.prosto.StateUIController
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStore
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.CoworkingSource
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.updateState
import dev.andrew.prosto.usecase.GetTicketListUseCase
import dev.andrew.prosto.usecase.TicketListWithTodayIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

data class MainScreenState(
    val coworking: Coworking?, // TODO какой пидорас это сделал? залупnull
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
        isCoworkingListLoading = false)
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
    private val userSelectedCoworkingLocalStore: UserSelectedCoworkingLocalStore = ToporObject.userSelectedCoworkingLocalStore,
): StateUIController<MainScreenState, MainScreenEvent>(initial = MainScreenState()) {
    private val loadTicketListScope = coroutineScope + Job()
    private val localStoreScope = coroutineScope + Job()

    private fun loadTicketList() {
        loadTicketListScope.coroutineContext.cancelChildren()
        updateState {
            copy(ticketListIsLoading = true)
        }
        println("updateState ticketListIsLoading = true")
        loadTicketListScope.launch {
            getTicketListUseCase.getTicketListWithTodayIndex(state.value.coworking!!).also { ticketListWithTodayIndex ->
                updateState {
                    copy(
                        ticketListWithTodayIndex = ticketListWithTodayIndex,
                        ticketListIsLoading = false
                    )
                }
                println("updateState ticketListIsLoading = false")
            }
        }
    }

    private fun saveSelectedCoworking(coworking: Coworking) {
        localStoreScope.launch {
            userSelectedCoworkingLocalStore.setCoworking(coworking)
        }
    }

    init {
        println("Init {}")
        updateState {
            copy(isCoworkingListLoading = true)
        }
        println("updateState isCoworkingListLoading = true")
        coroutineScope.launch {
            coworkingSource.getProsto().also { list ->
                val selected = userSelectedCoworkingLocalStore.getCoworking() ?: list[0]
                val indexOrZero = list.indexOfFirst { it.id == selected.id }.let {
                    if (it == -1) {
                        0
                    } else {
                        it
                    }
                }
                updateState {
                    copy(
                        coworking = selected,
                        coworkingIndex = indexOrZero,
                        coworkingList = list,
                        isCoworkingListLoading = false)
                }
                loadTicketList()
                println("updateState isCoworkingListLoading = false")
            }
        }
    }

    override fun reduce(state: MainScreenState, event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnCoworkingSelect -> {
                state.coworking?.let { selectedCow ->
                    if (event.coworking.id != selectedCow.id) {
                        saveSelectedCoworking(event.coworking)
                        updateState {
                            copy(coworking = event.coworking)
                        }
                        loadTicketList()
                    }
                }
            }
            is MainScreenEvent.OnClickTicketCreate -> {
                navigator.navigateTo(
                    ProstoDestination.CreateTicketScreen(state.coworking!!))
            }
            is MainScreenEvent.OnClickTicket -> {
                navigator.navigateTo(ProstoDestination.TicketQRDialog(coworking = state.coworking!!, ticket = event.ticket))
            }
        }
    }
}
