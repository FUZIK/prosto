package dev.andrew.prosto.usecase

import dev.andrew.prosto.repository.AvailableTime
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicketSource
import kotlinx.datetime.LocalDate

interface GetCoworkingTimesUseCase {
    suspend fun getAvailableTimes(coworking: Coworking, date: LocalDate): List<AvailableTime>
}

class GetCoworkingTimesUseCaseImpl(
    private val ticketSource: ProstoTicketSource
) : GetCoworkingTimesUseCase {
    override suspend fun getAvailableTimes(
        coworking: Coworking,
        date: LocalDate
    ): List<AvailableTime> {
        return ticketSource.getAvailableTimes(coworking = coworking, date = date)
    }
}
