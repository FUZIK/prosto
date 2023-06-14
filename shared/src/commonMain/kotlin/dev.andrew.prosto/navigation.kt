package dev.andrew.prosto

import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface Navigator<T> {
    val navState: Flow<T>
    fun navigateTo(state: T)
    @Deprecated("")
    fun navigateBack()
}

sealed interface ProstoDestination {
    class OnBackPressed(): ProstoDestination
    class MainScreen(): ProstoDestination
    class AuthDialog(
        val success: ProstoDestination? = null,
        val dismiss: ProstoDestination? = null): ProstoDestination
    class CreateTicketScreen(val coworking: Coworking): ProstoDestination
    class TicketQRDialog(val ticket: ProstoTicket): ProstoDestination
}

class ProstoNavigator(initial: ProstoDestination = ProstoDestination.MainScreen()):
    Navigator<ProstoDestination> {
    private val mutableNavState: MutableStateFlow<ProstoDestination> = MutableStateFlow(initial)
    override val navState = mutableNavState

    override fun navigateTo(state: ProstoDestination) {
        mutableNavState.tryEmit(state)

    }

    override fun navigateBack() {
        navigateTo(ProstoDestination.OnBackPressed())
    }
}
