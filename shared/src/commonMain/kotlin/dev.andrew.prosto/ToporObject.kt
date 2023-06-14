package dev.andrew.prosto

import dev.andrew.prosto.database.DriverFactory
import dev.andrew.prosto.database.TicketStore
import dev.andrew.prosto.database.TicketStoreImpl
import dev.andrew.prosto.database.UserAuthLocalStore
import dev.andrew.prosto.database.UserAuthLocalStoreImpl
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStore
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStoreLocalStoreImpl
import dev.andrew.prosto.database.createDatabase
import dev.andrew.prosto.repository.AuthSession
import dev.andrew.prosto.repository.AuthSource
import dev.andrew.prosto.repository.CoworkTicket_WebImpl
import dev.andrew.prosto.repository.Cowork_LocalImpl
import dev.andrew.prosto.repository.CoworkingSource
import dev.andrew.prosto.repository.ProstoAuth_WebImpl
import dev.andrew.prosto.repository.ProstoTicketSource
import dev.andrew.prosto.usecase.CreateTicketUseCase
import dev.andrew.prosto.usecase.CreateTicketUseCaseImpl
import dev.andrew.prosto.usecase.GetCoworkingTimesUseCase
import dev.andrew.prosto.usecase.GetCoworkingTimesUseCaseImpl
import dev.andrew.prosto.usecase.GetTicketListUseCase
import dev.andrew.prosto.usecase.GetTicketListUseCaseImpl
import dev.andrew.prosto.usecase.IsSignInRequiredUseCase
import dev.andrew.prosto.usecase.IsSignInRequiredUseCaseImpl
import dev.andrew.prosto.usecase.SignInUseCase
import dev.andrew.prosto.usecase.SignInUseCaseImpl
import dev.andrew.prosto.utilities.getSessionIdFromHeaders
import dev.andrew.prosto.utilities.isWebAuthResponseResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.cookies
import io.ktor.util.AttributeKey
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext

object ToporObject {
    val navigator: ProstoNavigator by lazy(LazyThreadSafetyMode.NONE) {
        ProstoNavigator() }



    val coworkingSource: CoworkingSource by lazy(LazyThreadSafetyMode.NONE) {
        Cowork_LocalImpl() }

    val ticketSource: ProstoTicketSource by lazy {
        CoworkTicket_WebImpl() }

    val authSource: AuthSource by lazy {
        ProstoAuth_WebImpl() }



    val userAuthLocalStore: UserAuthLocalStore by lazy {
        UserAuthLocalStoreImpl(appDatabase.userSavesCanTableQueries) }

    val userSelectedCoworkingLocalStore: UserSelectedCoworkingLocalStore by lazy {
        UserSelectedCoworkingLocalStoreLocalStoreImpl(
            coworkingSource,
            appDatabase.userSavesCanTableQueries)
    }


    val ticketStore: TicketStore by lazy {
        TicketStoreImpl(appDatabase.ticketTableQueries) }



    val signInUseCase: SignInUseCase by lazy {
        SignInUseCaseImpl(authSource, userAuthLocalStore) }

    val createTicketUseCase: CreateTicketUseCase by lazy {
        CreateTicketUseCaseImpl(ticketStore, ticketSource)
    }

    val getCoworkingTimesUseCase: GetCoworkingTimesUseCase by lazy {
        GetCoworkingTimesUseCaseImpl(ticketSource)
    }

    val getTicketListUseCase: GetTicketListUseCase by lazy {
        GetTicketListUseCaseImpl(ticketStore)
    }

    val isSignInRequiredUseCase: IsSignInRequiredUseCase by lazy {
        IsSignInRequiredUseCaseImpl(authSource)
    }



    private var sqlDriverFactory: DriverFactory? = null

    fun provideSqlDriver(driverFactory: DriverFactory) {
        sqlDriverFactory = driverFactory
    }

    private val appDatabase: AppDatabase by lazy {
        createDatabase(sqlDriverFactory!!)
    }



    val prostoAuthHttpClient: HttpClient by lazy {
        val cookieStorage = AcceptAllCookiesStorage()
        HttpClient {
            install(HttpCookies) {
                storage = cookieStorage
            }
            install(Logging) {
                level = LogLevel.HEADERS
                logger = object : Logger {
                    override fun log(message: String) {
                        print(message)
                    }
                }
            }
            followRedirects = true
            defaultRequest {
                header("Accept-Encoding", "identity")
            }
        }.also { client ->
            client.plugin(HttpSend).intercept { builder ->
                val call = execute(builder)
                if (builder.attributes.getOrNull(AttributeKey("isAuthRequest")) != true
                    && ToporObject.isSignInRequiredUseCase.isSignInRequired()) {
                    ToporObject.navigator.navigateTo(
                        ProstoDestination.AuthDialog(
                            dismiss = ProstoDestination.OnBackPressed(),
                        ))

                    delay(300)
                    while (ToporObject.isSignInRequiredUseCase.isSignInRequired()) {
                        delay(300)
                    }

                    cookieStorage.get(call.request.url).forEach {
                        builder.cookie(name = it.name,
                            value = it.value,
                            maxAge = it.maxAge,
                            expires = it.expires,
                            domain = it.domain,
                            path = it.path,
                            secure = it.secure,
                            httpOnly = it.httpOnly,
                            extensions = it.extensions)
                    }
                    return@intercept execute(builder)
                }
                return@intercept call
            }
        }
    }
}