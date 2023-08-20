package dev.andrew.prosto.repository

enum class MetroStation(
    val stationName: String
) {
    SPB_PETROGA("Петроградская"),
    SPB_VASKA("Василиостровская"),
    SPB_MOSKOVSKAY("Московская"),
    SPB_PARK_POBEDY("Парк победы"),
    SPB_ELEKTRA("Электросила"),
    SPB_PLOSCHAD_MUZHESTVA("Площадь мужества"),
    SPB_SADOVAYA("Садовая"),
}

data class Coworking(
    val id: Int,
    val fullName: String,
    val tumblrLink: String,
    val shortAddress: String,
    val fullAddress: String,
    val metroStation: MetroStation,
    val bitrixID: Int,
    val firmColor: Long,
    val licenseRead: String,
    val latitude: Double = .0,
    val longitude: Double = .0,
    val isSupportTurniket: Boolean = false,
    val isSupportTemporaryStorage: Boolean = false,
    val isSupportNotebook: Boolean = true,
    val isSupportLaminator: Boolean = false,
    val isSupportStaplerBindingMachine: Boolean = false,

)

interface CoworkingSource {
    fun getProsto(): List<Coworking>
}

class Cowork_LocalImpl : CoworkingSource {
    internal companion object {
        val ORIGINAL = Coworking(
            id = 0,
            fullName = "на Карповке",
            tumblrLink = "https://thumb.tildacdn.com/tild6239-6236-4431-b761-363462353965/-/format/webp/noroot.png",
            shortAddress = "наб. Карповки 5АК",
            fullAddress = "Санкт-Петербург, наб. Карповки 5АК, 5 этаж",
            latitude = 59.969835,
            longitude = 30.316299,
            metroStation = MetroStation.SPB_PETROGA,
            bitrixID = 1674,
            firmColor = 0xFFED0082,
            licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – на Карповке",
            isSupportTemporaryStorage = true
        )
        val PRODUCTION = Coworking(
            id = 1,
            fullName = "на Большом",
            tumblrLink = "https://thumb.tildacdn.com/tild3234-6462-4461-a538-653564626437/-/format/webp/DSC05481.jpg",
            shortAddress = "Большой пр. В.О. 83",
            fullAddress = "Санкт-Петербург, Большой пр. В.О. 83, этаж 2",
            latitude = 59.933855,
            longitude = 30.254968,
            metroStation = MetroStation.SPB_VASKA,
            bitrixID = 1675,
            firmColor = 0xFF6200EA,
            licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – на Большом",
            isSupportTurniket = true,
            isSupportTemporaryStorage = true
        )
        val TO_SMART = Coworking(
            id = 2,
            fullName = "на Новоизе",
            tumblrLink = "https://thumb.tildacdn.com/tild3338-3737-4165-b366-663466353262/-/format/webp/__2SMART_IMG_0854_1.jpg",
            shortAddress = "Новоизмайловский пр. 48",
            fullAddress = "Санкт-Петербург, Новоизмайловский пр. 48, этаж 2",
            latitude = 59.853565,
            longitude = 30.306242,
            metroStation = MetroStation.SPB_MOSKOVSKAY,
            bitrixID = 2708,
            firmColor = 0xFFFFC107,
            licenseRead = "филиал молодёжного пространства \n«ПРОСТО» – на Новоизе",
            isSupportLaminator = true,
            isSupportStaplerBindingMachine = true
        )
        val KALININSKIY = Coworking(
            id = 4,
            fullName = "ПРОСТО.Калининский",
            tumblrLink = "https://static.tildacdn.com/tild3438-3161-4561-b962-623634613836/___Open_space-15.jpg",
            shortAddress = "пр. Непокорённых 16к1Д",
            fullAddress = "Санкт-Петербург, пр. Непокорённых 16к1Д",
            latitude = 59.996203,
            longitude = 30.384769,
            metroStation = MetroStation.SPB_PLOSCHAD_MUZHESTVA,
            bitrixID = 2709,
            firmColor = 0xFFFFFFFF,
            licenseRead = "районный коворкинг \n«ПРОСТО.Калининский»",
            isSupportNotebook = false
        )
        val MOSCOW = Coworking(
            id = 3,
            fullName = "ПРОСТО.Московский",
            tumblrLink = "https://static.tildacdn.com/tild3739-3961-4963-a239-623634643237/00008.jpg",
            shortAddress = "Московский пр. 151А",
            fullAddress = "Московский просп., 151А, Санкт-Петербург",
            latitude = 59.873049,
            longitude = 30.317737,
            metroStation = MetroStation.SPB_ELEKTRA,
            bitrixID = 6219,
            firmColor = 0xFFFFFFFF,
            licenseRead = "районный коворкинг \n«ПРОСТО.Московский»"
        )
        val GRIBICH = Coworking(
            id = 5,
            fullName = "на Грибоедова",
            tumblrLink = "https://static.tildacdn.com/tild3665-6363-4663-a130-333933653036/00003.jpg",
            shortAddress = "наб. Грибоедова 105",
            fullAddress = "набережная канала Грибоедова, 105, Санкт-Петербург, 190000",
            latitude = 59.926142,
            longitude = 30.300011,
            metroStation = MetroStation.SPB_SADOVAYA,
            bitrixID = 6788,
            firmColor = 0xFFFFFFFF,
            licenseRead = "районный коворкинг \n«на Грибоедова»"
        )

        val ALL = listOf(ORIGINAL, MOSCOW, PRODUCTION, KALININSKIY, TO_SMART, GRIBICH)
    }

    override fun getProsto(): List<Coworking> {
        return ALL
    }
}