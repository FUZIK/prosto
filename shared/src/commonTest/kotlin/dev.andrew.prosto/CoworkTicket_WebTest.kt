package dev.andrew.prosto

import dev.andrew.prosto.repository.CoworkTicketResult
import dev.andrew.prosto.repository.TicketInfo
import dev.andrew.prosto.repository.TicketParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import kotlin.test.*
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class CoworkTicket_WebTest {
    private val coworkingRepository = ToporObject.coworkingSource
    private val authRepository = ToporObject.authSource
    private val ticketRepository = ToporObject.ticketSource

    private val testCoworking = coworkingRepository.getProsto()[0]

    private val yesterdayDate = Clock.System.now().minus(1.days)
        .toLocalDateTime(timeZone = TimeZone.UTC).date
    private val todayDate = Clock.System.now()
        .toLocalDateTime(timeZone = TimeZone.UTC).date
    private val nextDayDate = Clock.System.now().plus(1.days)
        .toLocalDateTime(timeZone = TimeZone.UTC).date

    private suspend fun makeSuccessTicket(date: LocalDate, time: List<LocalTime>): CoworkTicketResult {
        return ticketRepository.createTicket(
            testCoworking, TicketInfo(
            date = date,
            times = time,
            params = TicketParams(
                isIndependentWork = true,
                isOrganizationRecreation = false,
                isIndependentProjectWork = false,
                needKomp = false,
                needMFUPrinter = false,
                needFlipchart = false,
                needLaminator = false,
                needStaplerBindingMachine = false,
                needOfficeSupplies = false,
                noNeedAnyMachines = true,
                needTemporaryStorage = false,
            )
        )
        )
    }

    @BeforeTest
    fun provideAuth() = runTest {
        authRepository.signIn(ProstoAuth_WebTest.VALID_AUTH_CREDITS).also { result ->
            result.authSession.also { session ->
                assertNotNull(session)
            }
        }
    }
    @Test
    fun testAvailableDatesRun() = runTest {
//        val dates = ticketRepository.getAvailableDates(Cowork_LocalImpl.MOSCOW)
//        assertFalse(dates.isEmpty())
    }
    @Test
    fun testAvailableTimesRun() = runTest {
//        val times = ticketRepository.getAvailableTimes(Cowork_LocalImpl.MOSCOW, todayDate)
//        assertFalse(times.isEmpty())
    }
    @Test
    fun testSuccessTicket() = runTest {
        val ticket = makeSuccessTicket(
            date = todayDate,
            time = arrayListOf(LocalTime(hour = 20, minute = 0)))
        assertNull(ticket.error)
        assertNotNull(ticket.ticket)
    }
    @Test
    fun testUpdateTicket() = runTest {
        val ticket = makeSuccessTicket(
            date = todayDate,
            time = listOf(LocalTime(21, 0))
        )
        assertNull(ticket.error)
        assertNotNull(ticket.ticket)
    }
    @Test
    fun testCheckAvailableTimesByTicketRun() = runTest {
//        val times = ticketRepository.getAvailableTimes(Cowork_LocalImpl.MOSCOW, todayDate)
//        val actualTimes = times.filter { it.isAvailable }.map { it.time }
//        val ticket = makeSuccessTicket(date = todayDate, time = actualTimes)
//        assertNull(ticket.error, ticket.error)
//        assertFalse(times.isEmpty())
//        assertNotNull(ticket.ticket)
    }
    @Test
    fun testFailTicket() = runTest {
//        val ticket = ticketRepository.createTicket(
//            Cowork_LocalImpl.MOSCOW, TicketInfo(
//            date = yesterdayDate,
//            times = listOf(
//                LocalTime(21, 0) /* Invalid time */
//            ),
//            params = TicketParams(
//                isIndependentWork = true,
//                isOrganizationRecreation = true,
//                isIndependentProjectWork = true,
//                needKomp = true,
//                needMFUPrinter = true,
//                needFlipchart = true,
//                needLaminator = true,
//                needStaplerBindingMachine = true,
//                needOfficeSupplies = true,
//                noNeedAnyMachines = true,
//                needTemporaryStorage = true,
//            )
//        )
//        )
//        assertNotNull(ticket.error)
//        assertNull(ticket.ticket)
    }
    @Test
    fun testGettingTickets() = runTest {
        val ticketList = ticketRepository.fetchTickets()
        println("Tickets: $ticketList")
        assertFalse(ticketList.isEmpty())
    }
}
