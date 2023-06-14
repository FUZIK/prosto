package dev.andrew.prosto.utilities

import dev.andrew.prosto.repository.AuthResult
import dev.andrew.prosto.repository.AuthSession
import dev.andrew.prosto.repository.ProstoAuth_WebImpl
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Cookie
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.parseClientCookiesHeader

fun getSessionIdFromHeaders(response: HttpResponse): String? {
    var sessionId: String? = null

//    if (response.headers.contains(HttpHeaders.SetCookie)) {
//        sessionId = response.headers.getAll(HttpHeaders.SetCookie)?.let { cookieHeaders ->
//            cookieHeaders.firstOrNull { cookieHeader ->
//                cookieHeader.contains("PHPSESSID")
//            }?.let { phpSessionHeader ->
//                ProstoAuth_WebImpl.PHPSESSID_REGEX.find(phpSessionHeader)?.groupValues?.getOrNull(1)
//            }
//        }
//    }
//
//    if (sessionId == null) {
//        val headers = response.request.headers
//        if (headers.contains(HttpHeaders.Cookie)) {
//            sessionId = headers.getAll(HttpHeaders.Cookie)?.let { cookieHeaders ->
//                cookieHeaders.firstOrNull { cookieHeader ->
//                    cookieHeader.contains("PHPSESSID")
//                }?.let { phpSessionHeader ->
//                    ProstoAuth_WebImpl.PHPSESSID_REGEX.find(phpSessionHeader)?.groupValues?.getOrNull(1)
//                }
//            }
//        }
//    }

    if (response.headers.contains(HttpHeaders.SetCookie)) {
        sessionId = response.headers.getAll(HttpHeaders.SetCookie)?.let { cookieHeaders ->
            cookieHeaders.firstOrNull { cookieHeader ->
                cookieHeader.contains("PHPSESSID")
            }?.let { phpSessionHeader ->
                ProstoAuth_WebImpl.PHPSESSID_REGEX.find(phpSessionHeader)?.groupValues?.getOrNull(1)
            }
        }
    }

    if (sessionId == null) {
        val headers = response.request.headers
        if (headers.contains(HttpHeaders.Cookie)) {
            sessionId = headers.getAll(HttpHeaders.Cookie)?.let { cookieHeaders ->
                cookieHeaders.firstOrNull { cookieHeader ->
                    cookieHeader.contains("PHPSESSID")
                }?.let { phpSessionHeader ->
                    ProstoAuth_WebImpl.PHPSESSID_REGEX.find(phpSessionHeader)?.groupValues?.getOrNull(1)
                }
            }
        }
    }

    if (sessionId != null && sessionId.isEmpty()) {
        return null
    }

    return sessionId
}

suspend fun isWebAuthResponseResult(response: HttpResponse): AuthResult {
    fun getAuthSession(): AuthSession {
        val sessionId = getSessionIdFromHeaders(response)
        return if (sessionId != null) AuthSession(sessionId) else AuthSession.EMPTY
    }

    when (response.status) {
        HttpStatusCode.OK -> {
            val html = response.bodyAsText()
            if (html.contains("<div class=\"alert alert-danger\">")) {
                /* Invalid params */
                return AuthResult(
                    isAuthSuccess = false,
                    errorMessage = "Неверный логин или пароль",
                    authSession = null
                )
            } else if (html.contains("Вы зарегистрированы и успешно авторизовались")) {
                /* Session is valid */
                return AuthResult(
                    isAuthSuccess = true,
                    errorMessage = null,
                    authSession = getAuthSession()
                )
            }
        }
        HttpStatusCode.Found -> {
            /* First auth */
            return AuthResult(
                isAuthSuccess = true,
                errorMessage = null,
                authSession = getAuthSession()
            )
        }
    }
    return AuthResult(
        isAuthSuccess = false,
        errorMessage = "Response not handled: ${response.status}",
        authSession = null
    )
}
