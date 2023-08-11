package dev.andrew.prosto.controller

import dev.andrew.prosto.StateUIController
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.navigation.ProstoNavigator
import dev.andrew.prosto.repository.AvailableTime
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.TicketInfo
import dev.andrew.prosto.repository.TicketParams
import dev.andrew.prosto.usecase.CreateTicketUseCase
import dev.andrew.prosto.usecase.GetCoworkingTimesUseCase
import dev.andrew.prosto.utilities.MSK_ZONE
import dev.andrew.prosto.utilities.PROSTO_ZONE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class TicketScreenState(
    val coworking: Coworking,
    val isTimesLoading: Boolean,
    val isParamsLoading: Boolean,
    val isTicketInProgress: Boolean,
    val isTicketButtonEnabled: Boolean,
    val isTicketError: String?,
    val ticketDate: TicketScreenDate,
    val availableTimes: List<AvailableTime>,
    val selectedTimes: List<LocalTime>,
    val selectedParams: TicketParams
) {
    constructor(coworking: Coworking) : this(
        coworking = coworking,
        isTimesLoading = true,
        isParamsLoading = false,
        isTicketInProgress = false,
        isTicketButtonEnabled = false,
        isTicketError = null,
        ticketDate = TicketScreenDate.TODAY,
        availableTimes = emptyList(),
        selectedTimes = emptyList(),
        selectedParams = TicketParams()
    )
}

sealed interface TicketScreenEvent {
    class OnTicketDateChanged(val ticketDate: TicketScreenDate) : TicketScreenEvent
    class OnTicketTimeChanged(val time: LocalTime, val selected: Boolean) : TicketScreenEvent
    class OnTicketParamsChanged(val selectedParams: TicketParams) : TicketScreenEvent
    class OnRegisterClick : TicketScreenEvent
    class OnBackPressed : TicketScreenEvent
}

enum class TicketScreenDate {
    TODAY,
    TOMORROW,
    AFTER_TOMORROW
}

class CreateTicketScreenController(
    coworking: Coworking,
    private val coroutineScope: CoroutineScope,
    private val getCoworkingTimesUseCase: GetCoworkingTimesUseCase = ToporObject.getCoworkingTimesUseCase,
    private val createTicketUseCase: CreateTicketUseCase = ToporObject.createTicketUseCase,
    private val navigator: ProstoNavigator = ToporObject.navigator
) : StateUIController<TicketScreenState, TicketScreenEvent>(TicketScreenState(coworking = coworking)) {
    private val loadAvailableTimesScope = coroutineScope + Job()

    private fun reloadAvailableTimes() {
        loadAvailableTimesScope.coroutineContext.cancelChildren()
        setState(state.value.copy(isTimesLoading = true))
        loadAvailableTimesScope.launch {
            state.value.also { aState ->
                getCoworkingTimesUseCase.getAvailableTimes(
                    coworking = aState.coworking,
                    date = ticketDateDescriptor(aState.ticketDate)
                ).also {
                    setState(state.value.copy(availableTimes = it, isTimesLoading = false))
                }
            }
        }
    }

    init {
        reloadAvailableTimes()
    }

    // Це шо? На полпятого.
    // TODO: Delegate TicketScreenDate to TicketSource
    private fun ticketDateDescriptor(ticketScreenDate: TicketScreenDate): LocalDate {
        val now = Clock.System.now().toLocalDateTime(timeZone = PROSTO_ZONE).date
        return when (ticketScreenDate) {
            TicketScreenDate.TODAY -> now
            TicketScreenDate.TOMORROW -> now.plus(1, DateTimeUnit.DAY)
            TicketScreenDate.AFTER_TOMORROW -> now.plus(2, DateTimeUnit.DAY)
        }
    }

    private fun registerTicket() {
        state.value.also { aState ->
            setState(
                aState.copy(
                    isTicketError = null,
                    isTicketInProgress = true,
                    isTicketButtonEnabled = false
                )
            )
            val validTimes = aState.availableTimes
                .filter { it.isAvailable }
                .map { it.time }
                .run {
                    aState.selectedTimes.filter { this.contains(it) }
                }
            val ticketInfo = TicketInfo(
                date = ticketDateDescriptor(aState.ticketDate),
                times = validTimes,
                params = aState.selectedParams
            )
            coroutineScope.launch {
                val result = createTicketUseCase.createTicket(
                    coworking = aState.coworking, ticketInfo = ticketInfo
                )
                state.value.also { aState ->
                    if (result.isSuccess) {
                        setState(
                            aState.copy(
                                isTicketInProgress = false,
                                isTicketButtonEnabled = true
                            )
                        )
                        navigator.navigateBack()
                    } else {
                        setState(
                            aState.copy(
                                isTicketError = result.error,
                                isTicketInProgress = false,
                                isTicketButtonEnabled = true
                            )
                        )
                    }
                }
            }
        }
    }

    private fun updateTicketButtonState() {
        state.value.also { aState ->
            setState(
                aState.copy(
                    isTicketButtonEnabled =
                    aState.selectedTimes.isNotEmpty()
                            && aState.availableTimes.isNotEmpty()
                )
            )
        }
    }

    override fun reduce(state: TicketScreenState, event: TicketScreenEvent) {
        when (event) {
            is TicketScreenEvent.OnTicketDateChanged -> {
                if (state.ticketDate != event.ticketDate) {
                    setState(state.copy(ticketDate = event.ticketDate))
                    reloadAvailableTimes()
                }
                updateTicketButtonState()
            }

            is TicketScreenEvent.OnTicketParamsChanged -> {
                setState(state.copy(selectedParams = event.selectedParams))
                updateTicketButtonState()
            }

            is TicketScreenEvent.OnTicketTimeChanged -> {
                val selectedTimes = state.selectedTimes
                if (selectedTimes.contains(event.time)) {
                    if (!event.selected) {
                        setState(
                            state.copy(
                                selectedTimes = selectedTimes.filter { it != event.time })
                        )
                    }
                } else {
                    if (event.selected) {
                        setState(
                            state.copy(
                                selectedTimes = selectedTimes.plus(event.time)
                            )
                        )
                    }
                }
                updateTicketButtonState()
            }

            is TicketScreenEvent.OnRegisterClick -> {
                registerTicket()
            }

            is TicketScreenEvent.OnBackPressed -> {
                navigator.navigateBack()
            }
        }
    }
}