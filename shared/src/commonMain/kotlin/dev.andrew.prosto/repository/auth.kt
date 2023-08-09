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
    suspend fun signIn(info: AuthCredits): AuthResult
    suspend fun isExpired(session: AuthSession): Boolean
}

class ProstoAuth_WebImpl(
    private val httpClient: HttpClient = ToporObject.prostoAuthHttpClient
): AuthSource {
    companion object {
        val IS_AUTH_REQUEST = AttributeKey<Boolean>("isAuthRequest")
        val PHPSESSID_REGEX = Regex("PHPSESSID=(.*?)(?:;|)")
    }

    override suspend fun signIn(info: AuthCredits): AuthResult {
        val response = httpClient.post("https://xn--90azaccdibh.xn--p1ai/auth/") {
            parameter("USER_LOGIN", info.email)
            parameter("USER_PASSWORD", info.password)
            parameter("AUTH_ACTION", "Войти")

            setAttributes {
                put(IS_AUTH_REQUEST, true)
            }
        }

        return isWebAuthResponseResult(response)
    }

    override suspend fun isExpired(session: AuthSession): Boolean {
        val response = httpClient.get("https://xn--90azaccdibh.xn--p1ai/auth/") {
            setAttributes {
                put(IS_AUTH_REQUEST, true)
            }
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val html = response.bodyAsText()
                if (html.contains("Вы зарегистрированы и успешно авторизовались")) {
                    /* Session is valid */
                    return false
                }
            }
            HttpStatusCode.Found -> {
                return false
            }
        }
        return true
    }
}
