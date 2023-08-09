package dev.andrew.prosto

import dev.andrew.prosto.database.DriverFactory
import dev.andrew.prosto.database.TicketStore
import dev.andrew.prosto.database.TicketStoreImpl
import dev.andrew.prosto.database.UserAuthLocalStore
import dev.andrew.prosto.database.UserAuthLocalStoreImpl
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStore
import dev.andrew.prosto.database.UserSelectedCoworkingLocalStoreLocalStoreImpl
import dev.andrew.prosto.database.createDatabase
import dev.andrew.prosto.navigation.ProstoDestination
import dev.andrew.prosto.navigation.ProstoNavigator
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
import dev.andrew.prosto.usecase.TicketTurniketKeyUseCase
import dev.andrew.prosto.usecase.TicketTurniketKeyUseCaseImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.cookie
import io.ktor.client.request.header
import kotlinx.coroutines.delay

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

    val ticketTurniketKeyUseCase: TicketTurniketKeyUseCase by lazy {
        TicketTurniketKeyUseCaseImpl(ticketStore, ticketSource)
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
                if (builder.attributes.getOrNull(ProstoAuth_WebImpl.IS_AUTH_REQUEST) != true
                    && ToporObject.isSignInRequiredUseCase.isSignInRequired()) {
                    ToporObject.navigator.navigateTo(
                        ProstoDestination.AuthDialog()
                    )

                    // TODO okhttp3.internal.http2.Http2Stream$StreamTimeout.newTimeoutException
                    //     samsung m31 (Galaxy M31)
                    //    Android 12 (SDK 31)
                    //    Exception o4.b:
                    //    at io.ktor.client.plugins.HttpTimeoutKt.SocketTimeoutException (HttpTimeout.kt)
                    //    at io.ktor.client.engine.okhttp.OkUtilsKt.mapOkHttpException (OkUtils.kt)
                    //    at io.ktor.client.engine.okhttp.OkUtilsKt.access$mapOkHttpException (OkUtils.kt)
                    //    at io.ktor.client.engine.okhttp.OkHttpCallback.onFailure (OkHttpCallback.java)
                    //    at okhttp3.internal.connection.RealCall$AsyncCall.run (RealCall.java)
                    //    at java.util.concurrent.ThreadPoolExecutor.runWorker (ThreadPoolExecutor.java:1137)
                    //    at java.util.concurrent.ThreadPoolExecutor$Worker.run (ThreadPoolExecutor.java:637)
                    //    at java.lang.Thread.run (Thread.java:1012)
                    //    Caused by java.net.SocketTimeoutException: timeout
                    //    at okhttp3.internal.http2.Http2Stream$StreamTimeout.newTimeoutException (Http2Stream.java)
                    //    at okhttp3.internal.http2.Http2Stream$StreamTimeout.exitAndThrowIfTimedOut (Http2Stream.java)
                    //    at okhttp3.internal.http2.Http2Stream.takeHeaders (Http2Stream.java)
                    //    at okhttp3.internal.http2.Http2ExchangeCodec.readResponseHeaders (Http2ExchangeCodec.java)
                    //    at okhttp3.internal.connection.Exchange.readResponseHeaders (Exchange.java)
                    //    at okhttp3.internal.http.CallServerInterceptor.intercept (CallServerInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.connection.ConnectInterceptor.intercept (ConnectInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.cache.CacheInterceptor.intercept (CacheInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.http.BridgeInterceptor.intercept (BridgeInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept (RetryAndFollowUpInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp (RealCall.java)
                    //    at okhttp3.internal.connection.RealCall$AsyncCall.run (RealCall.java)


                    // TODO okhttp3.internal.platform.Platform.connectSocket
                    //     samsung m31 (Galaxy M31)
                    //    Android 12 (SDK 31)
                    //    Exception o4.a:
                    //    at io.ktor.client.plugins.HttpTimeoutKt.ConnectTimeoutException (HttpTimeout.kt)
                    //    at io.ktor.client.engine.okhttp.OkUtilsKt.mapOkHttpException (OkUtils.kt)
                    //    at io.ktor.client.engine.okhttp.OkUtilsKt.access$mapOkHttpException (OkUtils.kt)
                    //    at io.ktor.client.engine.okhttp.OkHttpCallback.onFailure (OkHttpCallback.java)
                    //    at okhttp3.internal.connection.RealCall$AsyncCall.run (RealCall.java)
                    //    at java.util.concurrent.ThreadPoolExecutor.runWorker (ThreadPoolExecutor.java:1137)
                    //    at java.util.concurrent.ThreadPoolExecutor$Worker.run (ThreadPoolExecutor.java:637)
                    //    at java.lang.Thread.run (Thread.java:1012)
                    //    Caused by java.net.SocketTimeoutException:
                    //    at libcore.io.IoBridge.connectErrno (IoBridge.java:235)
                    //    at libcore.io.IoBridge.connect (IoBridge.java:179)
                    //    at java.net.PlainSocketImpl.socketConnect (PlainSocketImpl.java:142)
                    //    at java.net.AbstractPlainSocketImpl.doConnect (AbstractPlainSocketImpl.java:390)
                    //    at java.net.AbstractPlainSocketImpl.connectToAddress (AbstractPlainSocketImpl.java:230)
                    //    at java.net.AbstractPlainSocketImpl.connect (AbstractPlainSocketImpl.java:212)
                    //    at java.net.SocksSocketImpl.connect (SocksSocketImpl.java:436)
                    //    at java.net.Socket.connect (Socket.java:646)
                    //    at okhttp3.internal.platform.Platform.connectSocket (Platform.java)
                    //    at okhttp3.internal.connection.RealConnection.connectSocket (RealConnection.java)
                    //    at okhttp3.internal.connection.RealConnection.connect (RealConnection.java)
                    //    at okhttp3.internal.connection.ExchangeFinder.findConnection (ExchangeFinder.java)
                    //    at okhttp3.internal.connection.ExchangeFinder.findHealthyConnection (ExchangeFinder.java)
                    //    at okhttp3.internal.connection.ExchangeFinder.find (ExchangeFinder.java)
                    //    at okhttp3.internal.connection.RealCall.initExchange$okhttp (RealCall.java)
                    //    at okhttp3.internal.connection.ConnectInterceptor.intercept (ConnectInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.cache.CacheInterceptor.intercept (CacheInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.http.BridgeInterceptor.intercept (BridgeInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.http.RetryAndFollowUpInterceptor.intercept (RetryAndFollowUpInterceptor.java)
                    //    at okhttp3.internal.http.RealInterceptorChain.proceed (RealInterceptorChain.java)
                    //    at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp (RealCall.java)
                    //    at okhttp3.internal.connection.RealCall$AsyncCall.run (RealCall.java)

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