package dev.andrew.prosto.usecase

import dev.andrew.prosto.database.TicketStore
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TicketListWithTodayIndex(
    val tickets: List<ProstoTicket>,
    val indexOfTodayItem: Int
) {
    constructor() : this(tickets = emptyList(), indexOfTodayItem = 0)
}

interface GetTicketListUseCase {
    suspend fun getTicketList(coworking: Coworking): List<ProstoTicket>
    suspend fun getTicketListWithTodayIndex(coworking: Coworking): TicketListWithTodayIndex
}

class GetTicketListUseCaseImpl(
    private val ticketStore: TicketStore
) : GetTicketListUseCase {
    override suspend fun getTicketList(coworking: Coworking): List<ProstoTicket> {
        return ticketStore.getTickets(coworking)
    }

    override suspend fun getTicketListWithTodayIndex(coworking: Coworking): TicketListWithTodayIndex {
        val ticketList = getTicketList(coworking)
        if (ticketList.size > 1) {
            ticketList.sortedByDescending { it.date.toEpochDays() }.let { sortedTicketList ->
                val nowUTCDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
                val todayTicket = sortedTicketList.indexOfFirst { it.date == nowUTCDate }
                if (todayTicket != -1) {
                    return TicketListWithTodayIndex(
                        tickets = sortedTicketList,
                        indexOfTodayItem = todayTicket
                    )
                }
            }
        }

        return TicketListWithTodayIndex(tickets = ticketList, indexOfTodayItem = 0)
    }
}