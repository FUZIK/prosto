package dev.andrew.prosto.navigation

import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket

sealed interface ProstoDestination {
    class MainScreen() : ProstoDestination
    class AuthDialog() : ProstoDestination
    class CreateTicketScreen(val coworking: Coworking) : ProstoDestination
    class TicketQRDialog(val coworking: Coworking, val ticket: ProstoTicket) : ProstoDestination
    class OnBackPressed() : ProstoDestination
}
