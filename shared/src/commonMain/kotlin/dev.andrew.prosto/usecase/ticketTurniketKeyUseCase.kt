package dev.andrew.prosto.usecase

import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.database.TicketStore
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.repository.ProstoTicketSource
import dev.andrew.prosto.repository.VisitTicket

interface TicketTurniketKeyUseCase {
    suspend fun getTurniketKey(coworking: Coworking, ticket: ProstoTicket): String?
}

class TicketTurniketKeyUseCaseImpl(
    private val ticketStore: TicketStore,
    private val ticketSource: ProstoTicketSource
): TicketTurniketKeyUseCase {
    override suspend fun getTurniketKey(coworking: Coworking, ticket: ProstoTicket): String? {
        if (ticket.qrDataTurniket == null) {
            ticketSource.getUniversalTurniketKey()?.let { key ->
                if (ticket is VisitTicket) {
                    ticketStore.addOrUpdate(coworking, ticket.copy(
                        qrDataTurniket = key
                    ))
                    return key
                } else {
                    // TODO VisitTicket нужно установить на все методы работающие с билетами для пропуска в коворкинг
                }
            }
            return null
        } else {
            // TODO Если это понадобится, добавить обновление из сети при наличии сети и авторизации
            return ticket.qrDataTurniket
        }
    }

}