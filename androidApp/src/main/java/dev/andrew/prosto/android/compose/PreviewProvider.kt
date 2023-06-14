package dev.andrew.prosto.android.compose

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.MetroStation
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.repository.TicketInfo
import dev.andrew.prosto.repository.TicketParams
import dev.andrew.prosto.repository.VisitTicket
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private val NOW = Clock.System.now()
private val NOW_DATE = NOW.toLocalDateTime(TimeZone.UTC).date

private val S_COWORKING = Coworking(
    id = 9,
    fullName = "ПРОСТО.ORIGINAL",
    tumblrLink = "https://thumb.tildacdn.com/tild6239-6236-4431-b761-363462353965/-/format/webp/noroot.png",
    shortAddress = "наб. Карповки 5АК, 5 этаж",
    fullAddress = "Санкт-Петербург, наб. Карповки 5АК, 5 этаж",
    metroStation = MetroStation.SPB_PETROGA,
    bitrixID = 16749,
    firmColor = 0xFFED0082,
    licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – ПРОСТО.ORIGINAL"
)

private val SS_COWORKING = Coworking(
    id = 99,
    fullName = "ПРОСТО.ORIGINAL",
    tumblrLink = "https://thumb.tildacdn.com/tild6239-6236-4431-b761-363462353965/-/format/webp/noroot.png",
    shortAddress = "наб. Карповки 5АК, 5 этаж",
    fullAddress = "Санкт-Петербург, наб. Карповки 5АК, 5 этаж",
    metroStation= MetroStation.SPB_PETROGA,
    bitrixID = 167499,
    firmColor = 0xFFED0082,
    licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – ПРОСТО.ORIGINAL"
)

val SSS_COWORKING = Coworking(
    id = 999,
    fullName = "ПРОСТО.ORIGINAL",
    tumblrLink = "https://thumb.tildacdn.com/tild6239-6236-4431-b761-363462353965/-/format/webp/noroot.png",
    shortAddress = "наб. Карповки 5АК, 5 этаж",
    fullAddress = "Санкт-Петербург, наб. Карповки 5АК, 5 этаж",
    metroStation = MetroStation.SPB_PETROGA,
    bitrixID = 1674999,
    firmColor = 0xFFED0082,
    licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – ПРОСТО.ORIGINAL"
)


class CoworkingProvider: PreviewParameterProvider<Coworking> {
    override val values = sequenceOf(S_COWORKING)
}

class CoworkingListProvider: PreviewParameterProvider<List<Coworking>> {
    override val values = sequenceOf(listOf(S_COWORKING, SS_COWORKING, SSS_COWORKING))
}

class TicketListProvider: PreviewParameterProvider<List<ProstoTicket>> {
    override val values = sequenceOf(List(10) { i ->
        val date = NOW_DATE.plus(i, DateTimeUnit.DAY)
        VisitTicket(
            id = Long.MAX_VALUE,
            dataForQR = "https://www.youtube.com/watch?v=tOzjasE9Fuw",
            info = TicketInfo(
                date = date,
                times = emptyList(),
                params = TicketParams()
            )
        )
    })
}

class TicketEmptyListProvider: PreviewParameterProvider<List<ProstoTicket>> {
    override val values = sequenceOf(emptyList<ProstoTicket>())
}