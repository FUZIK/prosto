package dev.andrew.prosto.usecase

import dev.andrew.prosto.database.TicketStore
import dev.andrew.prosto.repository.CoworkTicketResult
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicketSource
import dev.andrew.prosto.repository.TicketInfo

interface CreateTicketUseCase {
    suspend fun createTicket(coworking: Coworking, ticketInfo: TicketInfo): CoworkTicketResult
}

class CreateTicketUseCaseImpl(
    private val ticketStore: TicketStore,
    private val ticketSource: ProstoTicketSource
): CreateTicketUseCase {
    override suspend fun createTicket(
        coworking: Coworking,
        ticketInfo: TicketInfo
    ): CoworkTicketResult {
        val result = ticketSource.createTicket(coworking = coworking, ticketInfo = ticketInfo)
        if (result.isSuccess && result.ticket != null) {
            ticketStore.addOrUpdate(coworking, result.ticket)
        }
        return result
    }
}
