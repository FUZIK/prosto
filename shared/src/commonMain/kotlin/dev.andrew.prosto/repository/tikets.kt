package dev.andrew.prosto.repository

import dev.andrew.prosto.ToporObject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

enum class QrDataType {
    API_URL,
    DATA_RG_KEY
}

sealed interface ProstoTicket {
    val id: Long
    val date: LocalDate
    val qrDataProsto: String
    @Deprecated("All times null. Use getUniversalTurniketKey()")
    val qrDataTurniket: String?
    fun isActual(): Boolean =
        Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC).let { now ->
            date >= now.date
        }
    fun isToday(): Boolean =
        Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC).let { now ->
            now.date == date
        }
}

class TicketInfo(
    @Deprecated("Moved to ProstoTicket") val date: LocalDate,
    val times: List<LocalTime>,
    val params: TicketParams
)

data class TicketParams(
    val isIndependentWork: Boolean = false,
    val isOrganizationRecreation: Boolean = false,
    val isIndependentProjectWork: Boolean = false,
    val needKomp: Boolean = false,
    val needMFUPrinter: Boolean = false,
    val needFlipchart: Boolean = false,
    val needLaminator: Boolean = false,
    val needStaplerBindingMachine: Boolean = false,
    val needOfficeSupplies: Boolean = false,
    val noNeedAnyMachines: Boolean = false,
    val needTemporaryStorage: Boolean = false
)

data class VisitTicket(
    override val id: Long,
    val info: TicketInfo,
    override val date: LocalDate = info.date,
    override val qrDataProsto: String,
    override val qrDataTurniket: String?
): ProstoTicket

class CoworkTicketResult(
    val ticket: VisitTicket?,
    val error: String?,
    val isSuccess: Boolean
)

class AvailableTime(
    val time: LocalTime,
    val isAvailable: Boolean
)

interface ProstoTicketSource {
    suspend fun createTicket(coworking: Coworking, ticketInfo: TicketInfo): CoworkTicketResult
    suspend fun getAvailableDates(coworking: Coworking): List<LocalDate>
    suspend fun getAvailableTimes(coworking: Coworking, date: LocalDate): List<AvailableTime>
    suspend fun getUniversalTurniketKey(): String?
}

class CoworkTicket_WebImpl(
    private val httpClient: HttpClient = ToporObject.prostoAuthHttpClient
): ProstoTicketSource {
    companion object {
        private val ACTIVE_DATA_ATTR_REGEX = "data-active-events='(.*?)(?:'|\r\n)".toRegex()
        private val DATA_RG_KEY_ATTR_REGEX = "data-rg-key=[\"']([^'\"]*)".toRegex()
        private val INPUT_ELEMENT_REGEX = Regex("<input([\\s\\S]*?)/?>")
        private val VALUE_ATTRIBUTE_REGEX = Regex("value=[\"']([^'\"]*)")

        /* TARGET group */
        private const val ATTRIBUTE_INDEPEND_WORK = "35"
        private const val ATTRIBUTE_LEISURE_TIME = "36"
        private const val ATTRIBUTE_PROJECT_ACTIVITY = "37"

        /* EQUIPMENT group */
        private const val ATTRIBUTE_NOTEBOOK = "38"
        private const val ATTRIBUTE_PRINTER = "39"
        private const val ATTRIBUTE_FLIPCHART = "40"
        private const val ATTRIBUTE_LAMINATOR = "41"
        private const val ATTRIBUTE_BINDING_MACHINE = "42"
        private const val ATTRIBUTE_OFFICE_SUPPLIES = "43"

        /* Requirements */
        private const val ATTRIBUTE_USER_ID = "USER_ID"
        private const val ATTRIBUTE_EVENT_ID = "EVENT_ID"

        private const val ATTRIBUTE_NO_MACHINES = "44"
        private const val GROUP0 = "CLICK[]"
        private const val GROUP1 = "TARGET[]"
        private const val GROUP2 = "EQUIPMENT[]"
        private const val GROUP_TEMP_STORAGE = "LOKER"

        const val COWORKING_LINK = "https://xn--90azaccdibh.xn--p1ai/coworking/"

        private fun getQrDataType(coworking: Coworking): QrDataType {
            when (coworking.bitrixID) {
                Cowork_LocalImpl.PRODUCTION.bitrixID -> {
                    return QrDataType.DATA_RG_KEY
                }
            }
            return QrDataType.API_URL
        }

        /*
        * OnPage date to LocalDate mapper
        * String: DD.MM.YYYY
        */
        private fun toBitrixDate(date: String): LocalDate {
            val units = date.split(".")
            val day = units.component1().toInt()
            val month = units.component2().toInt()
            val year = units.component3().toInt()
            return LocalDate(year, month, day)
        }
        private fun fromLocalDate(date: LocalDate): String {
            val dayStr = date.dayOfMonth.run {
                if (this > 9) this.toString() else "0$this" }
            val monthStr = date.monthNumber.run {
                if (this > 9) this.toString() else "0$this" }
            val year = date.year
            return "$dayStr.$monthStr.$year"
        }
    }
    private inner class BitrixCoworking(
        val bitrixID: Int,
        val dateList: List<BitrixCoworkDate>
    )
    private inner class BitrixCoworkDate(
        val rawDate: String,
        val pathFragment: String
    )
    private inner class TicketPageTime(
        val time: LocalTime,
        val isAvailable: Boolean
    )
    private inner class TicketPageForm(
        val userID: String,
        val eventID: String,
        val times: List<TicketPageTime>
    )
    private suspend fun requestCoworkingPage(): String {
        val response = httpClient.get(COWORKING_LINK)
        return response.bodyAsText()
    }
    private fun extractBitrixCoworkingList(coworkingPage: String): List<BitrixCoworking> {
        val match = ACTIVE_DATA_ATTR_REGEX.find(coworkingPage)
        val firstGroup = match?.groupValues?.getOrNull(1)
        val coworkingData = HashMap<Int, HashMap<String, String>>()
        firstGroup?.let { activeDataMatch ->
            Json.parseToJsonElement(activeDataMatch).jsonObject.let { root ->
                root.keys.forEach { rawId ->
                    val id = rawId.toInt()
                    coworkingData[id] = HashMap(333)
                    root[rawId]?.jsonObject?.let { linkByDate ->
                        linkByDate.keys.forEach { date ->
                            linkByDate[date]?.jsonArray?.getOrNull(0)?.toString()?.let { path ->
                                val clearedPath = path.replace("\"", "")
                                coworkingData[id]?.set(date, clearedPath)
                            }
                        }
                    }
                }
            }
        }
        if (coworkingData.isNotEmpty()) {
            return coworkingData.map { entry ->
                BitrixCoworking(
                    bitrixID = entry.key,
                    dateList = entry.value.map { BitrixCoworkDate(rawDate = it.key, pathFragment = it.value) }
                )
            }
        }
        return emptyList()
    }
    private suspend fun requestBitrixPage(date: BitrixCoworkDate): String {
        val response = httpClient.get("https://xn--90azaccdibh.xn--p1ai/${date.pathFragment}")
        return response.bodyAsText()
    }
    private fun extractAvailableTimes(bitrixPage: String): List<Pair<AvailableTime, String>> {
        val result = ArrayList<Pair<AvailableTime, String>>(12)
        var clickNameIndex = 10 /* 10ый час - время открытия коворкинга */
        INPUT_ELEMENT_REGEX.findAll(bitrixPage).forEach { match ->
            val matchValue = match.value
            if (matchValue.contains(GROUP0)
                && matchValue.contains("checkbox")) {
                VALUE_ATTRIBUTE_REGEX.find(matchValue)?.groupValues?.getOrNull(1)?.also { inputValue ->
                    LocalTime(hour = clickNameIndex, minute = 0).also { time ->
                        val isAvailable = !matchValue.contains("disable")
                        result.add(AvailableTime(time, isAvailable) to inputValue)
                    }
                    clickNameIndex++
                }
            }
        }
        return result
    }
    private fun isSupportAttribute(bitrixPage: String, attribute: String): Boolean {
        return bitrixPage.contains("value=\"$attribute\"")
    }
    private suspend fun postTicketForm(coworking: Coworking, bitrixPage: String, date: BitrixCoworkDate, info: TicketInfo): CoworkTicketResult {
        var userID: String? = null
        var eventID: String? = null

        INPUT_ELEMENT_REGEX.findAll(bitrixPage).forEach { match ->
            val matchValue = match.value
            if (matchValue.contains(ATTRIBUTE_USER_ID)) {
                VALUE_ATTRIBUTE_REGEX.find(matchValue)?.groupValues?.getOrNull(1)
                    ?.also { inputValue ->
                        userID = inputValue
                    }
            } else if (matchValue.contains(ATTRIBUTE_EVENT_ID)) {
                VALUE_ATTRIBUTE_REGEX.find(matchValue)?.groupValues?.getOrNull(1)
                    ?.also { inputValue ->
                        eventID = inputValue
                    }
            }
        }

        if (userID == null) {
            return CoworkTicketResult(ticket = null, error = "Unknown userId", isSuccess = false)
        }
        if (eventID == null) {
            return CoworkTicketResult(ticket = null, error = "Unknown eventID", isSuccess = false)
        }

        val availableTimes = extractAvailableTimes(bitrixPage)

        if (availableTimes.isEmpty()) {
            return CoworkTicketResult(ticket = null, error = "Unknown server times", isSuccess = false)
        }

        val hoursGroup = availableTimes.mapNotNull { time ->
            info.times.firstOrNull {
                it == time.first.time
            }?.run { time.second }
        }
        val ticketParams = info.params
        val targetGroup = ArrayList<String>(3).run {
            if (ticketParams.isIndependentWork)
                add(ATTRIBUTE_INDEPEND_WORK)
            if (ticketParams.isOrganizationRecreation)
                add(ATTRIBUTE_LEISURE_TIME)
            if (ticketParams.isIndependentProjectWork)
                add(ATTRIBUTE_PROJECT_ACTIVITY)
            return@run this
        }
        val targetMachines = ArrayList<String>(7).run {
            if (ticketParams.needKomp)
                add(ATTRIBUTE_NOTEBOOK)
            if (ticketParams.needMFUPrinter)
                add(ATTRIBUTE_PRINTER)
            if (ticketParams.needFlipchart)
                add(ATTRIBUTE_FLIPCHART)
            if (ticketParams.needLaminator)
                add(ATTRIBUTE_LAMINATOR)
            if (ticketParams.needStaplerBindingMachine)
                add(ATTRIBUTE_BINDING_MACHINE)
            if (ticketParams.needOfficeSupplies)
                add(ATTRIBUTE_OFFICE_SUPPLIES)
            if (ticketParams.noNeedAnyMachines)
                add(ATTRIBUTE_NO_MACHINES)
            return@run this
        }
        val formParams = Parameters.build {
            append(ATTRIBUTE_USER_ID, userID!!)
            append(ATTRIBUTE_EVENT_ID, eventID!!)
            hoursGroup.forEach {
                append(GROUP0, it)
            }
            targetGroup.forEach {
                append(GROUP1, it)
            }
            targetMachines.forEach {
                append(GROUP2, it)
            }
            if (ticketParams.needTemporaryStorage)
                append(GROUP_TEMP_STORAGE, "true")
        }
        val response = httpClient.submitForm(
            "https://xn--90azaccdibh.xn--p1ai/ajax/add_user_click.php",
            formParameters = formParams
        )
        val html = response.bodyAsText()
        if (response.status == HttpStatusCode.OK) {
            return if (html.contains("Неверные данные для записи в коворкинг")) {
                CoworkTicketResult(
                    ticket = null,
                    error = "Неверные данные для записи в коворкинг",
                    isSuccess = false
                )
            } else {
                val qrDataType = getQrDataType(coworking)
                CoworkTicketResult(
                    ticket = VisitTicket(
                        id = eventID!!.toLong(),
                        info = info,
                        qrDataProsto = "http://xn--90azaccdibh.xn--p1ai/api/form_participation.php?user_id=${userID}&event_id=${eventID}",
                        qrDataTurniket = if (qrDataType == QrDataType.DATA_RG_KEY) getUniversalTurniketKey() else null,
                    ),
                    error = null,
                    isSuccess = true
                )
            }
        }

        return CoworkTicketResult(ticket = null, error = "Unknown error", isSuccess = false)
    }

    private suspend fun fetchRgKey(): String? {
        val response = httpClient.get("https://xn--90azaccdibh.xn--p1ai/auth/personal.php#user_div_recorded_coworking")
        val html = response.bodyAsText()
        DATA_RG_KEY_ATTR_REGEX.find(html)?.also { result ->
            return result.groupValues.getOrNull(1)
        }
        return null
    }

    override suspend fun createTicket(
        coworking: Coworking,
        ticketInfo: TicketInfo
    ): CoworkTicketResult {
        requestCoworkingPage().also { coworkingPage ->
            extractBitrixCoworkingList(coworkingPage).also { coworkingList->
                val coworkingByID = coworkingList.firstOrNull { it.bitrixID == coworking.bitrixID }
                if (coworkingByID != null && coworkingByID.dateList.isNotEmpty()) {
                    val strDate = fromLocalDate(ticketInfo.date)
                    val firstDate = coworkingByID.dateList.firstOrNull { it.rawDate == strDate }
                    if (firstDate != null) {
                        requestBitrixPage(firstDate).also { bitrixPage ->
                            return postTicketForm(coworking, bitrixPage, firstDate, ticketInfo)
                        }
                    }
                }
            }
        }
        return CoworkTicketResult(ticket = null, error = "Unknown error", isSuccess = false)
    }
    override suspend fun getAvailableDates(coworking: Coworking): List<LocalDate> {
        requestCoworkingPage().also { coworkingPage ->
            extractBitrixCoworkingList(coworkingPage).also { coworkingList ->
                val coworkingByID = coworkingList.firstOrNull { it.bitrixID == coworking.bitrixID }
                if (coworkingByID != null && coworkingByID.dateList.isNotEmpty()) {
                    return coworkingByID.dateList.map { toBitrixDate(it.rawDate) }
                }
            }
        }
        return emptyList()
    }
    override suspend fun getAvailableTimes(coworking: Coworking, date: LocalDate): List<AvailableTime> {
        requestCoworkingPage().also { coworkingPage ->
            extractBitrixCoworkingList(coworkingPage).also { coworkingList->
                val coworkingByID = coworkingList.firstOrNull { it.bitrixID == coworking.bitrixID }
                if (coworkingByID != null && coworkingByID.dateList.isNotEmpty()) {
                    val strDate = fromLocalDate(date)
                    val firstDate = coworkingByID.dateList.firstOrNull { it.rawDate == strDate }
                    if (firstDate != null) {
                        requestBitrixPage(firstDate).also { bitrixPage ->
                            extractAvailableTimes(bitrixPage)?.also { times ->
                                return times.map { it.first }
                            }
                        }
                    }
                }
            }
        }
        return emptyList()
    }

    private var cachedUniversalRgKey: String? = null
    override suspend fun getUniversalTurniketKey(): String? {
        return if (cachedUniversalRgKey == null) {
            val rgKey = fetchRgKey()
            rgKey?.also { cachedUniversalRgKey = it }
        } else {
            cachedUniversalRgKey
        }

    }
}
