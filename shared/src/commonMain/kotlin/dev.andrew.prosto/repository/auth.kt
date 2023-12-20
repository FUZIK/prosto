package dev.andrew.prosto.repository

import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.utilities.isWebAuthResponseResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthResult(
    val isAuthSuccess: Boolean,
    val errorMessage: String?,
    val authSession: AuthSession?
)

class AuthCredits(
    val email: String,
    val password: String
)

class AuthSession(
    val secret: String
) {
    companion object {
        val EMPTY = AuthSession("")
    }
}

interface AuthSource {
    val isUserAuth: Flow<Boolean>
    suspend fun signIn(info: AuthCredits): AuthResult
    suspend fun isExpired(session: AuthSession): Boolean
}

class ProstoAuth_WebImpl(
    private val httpClient: HttpClient = ToporObject.prostoAuthHttpClient
) : AuthSource {
    companion object {
        val IS_AUTH_REQUEST = AttributeKey<Boolean>("isAuthRequest")
        val PHPSESSID_REGEX = Regex("PHPSESSID=(.*?)(?:;|)")
    }

    private val _isUserAuth = MutableStateFlow(false)
    override val isUserAuth = _isUserAuth.asStateFlow()

    override suspend fun signIn(info: AuthCredits): AuthResult {
        val response = httpClient.post("https://xn--90azaccdibh.xn--p1ai/auth/") {
            parameter("USER_LOGIN", info.email)
            parameter("USER_PASSWORD", info.password)
            parameter("AUTH_ACTION", "Войти")

            setAttributes {
                put(IS_AUTH_REQUEST, true)
            }
        }

        return isWebAuthResponseResult(response).also {
            _isUserAuth.emit(it.isAuthSuccess)
        }
    }

    override suspend fun isExpired(session: AuthSession): Boolean {
        val response = httpClient.get("https://xn--90azaccdibh.xn--p1ai/auth/") {
            setAttributes {
                put(IS_AUTH_REQUEST, true)
            }
        }
        return when (response.status) {
            HttpStatusCode.OK -> !response.bodyAsText().contains("Вы зарегистрированы и успешно авторизовались")
            HttpStatusCode.Found -> false
            else -> true
        } .also { isExpired ->_isUserAuth.emit(!isExpired) }
    }
}
