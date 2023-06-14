package dev.andrew.prosto.database

import dev.andrew.prosto.repository.AuthCredits
import dev.andrew.prosto.repository.AuthSession
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.CoworkingSource
import dev.andrew.prosto.repository.TicketInfo
import dev.andrew.prosto.repository.TicketParams
import dev.andrew.prosto.repository.VisitTicket
import devandrewprosto.TicketTableQueries
import devandrewprosto.UserSavesCanTableQueries
import kotlinx.datetime.LocalDate

interface TicketStore {
    suspend fun getTickets(coworking: Coworking): List<VisitTicket>
    suspend fun addTicket(coworking: Coworking, ticket: VisitTicket)
}

interface UserAuthLocalStore {
    @Deprecated("Rewrite to suspend set/get functions")
    var savedCredits: AuthCredits?
}

interface UserSelectedCoworkingLocalStore {
    suspend fun getCoworking(): Coworking?
    suspend fun setCoworking(coworking: Coworking)
}

class UserSelectedCoworkingLocalStoreLocalStoreImpl(
    private val coworkingSource: CoworkingSource,
    private val userSavesCanTableQueries: UserSavesCanTableQueries
): UserSelectedCoworkingLocalStore {
    override suspend fun getCoworking(): Coworking? {
        return userSavesCanTableQueries.selectCoworkingId().executeAsOneOrNull()?.run {
            selectedCoworkingId?.toInt()?.let { selectedCoworkingId ->
                coworkingSource.getProsto().firstOrNull { it.id == selectedCoworkingId }
            }
        }
    }

    override suspend fun setCoworking(coworking: Coworking) {
        userSavesCanTableQueries.updateCoworkingId(coworking.id.toLong())
    }

}

class UserAuthLocalStoreImpl(
    private val userSavesCanTableQueries: UserSavesCanTableQueries
): UserAuthLocalStore {
    override var savedCredits: AuthCredits?
        get() {
            return userSavesCanTableQueries.selectAuthCredits().executeAsOneOrNull()?.run {
                if (savedEmail != null && savedPassword != null) {
                    AuthCredits(email = savedEmail, password = savedPassword)
                } else null
            }
        }
        set(value) {
            userSavesCanTableQueries.updateAuthCredits(value?.email, value?.password)
        }
}

class TicketStoreImpl(
    private val ticketTableQueries: TicketTableQueries
): TicketStore {
    override suspend fun getTickets(coworking: Coworking): List<VisitTicket> {
        return ticketTableQueries.selectAllById(coworkingId = coworking.id.toLong()) { id, _, epochDays, qrData ->
            val info = TicketInfo(
                date = LocalDate.fromEpochDays(epochDays.toInt()),
                times = emptyList(),
                params = TicketParams()
            )
            VisitTicket(id = id, info = info, dataForQR = qrData)
        }.executeAsList()
    }

    override suspend fun addTicket(coworking: Coworking, ticket: VisitTicket) {
        ticketTableQueries.insertOrUpdate(
            id = ticket.id,
            coworkingId = coworking.id.toLong(),
            epochDays = ticket.date.toEpochDays().toLong(),
            qrData = ticket.dataForQR)
    }
}


