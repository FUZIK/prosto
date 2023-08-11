package dev.andrew.prosto

import dev.andrew.prosto.repository.AuthCredits
import dev.andrew.prosto.repository.AuthSession
import dev.andrew.prosto.repository.ProstoAuth_WebImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProstoAuth_WebTest {
    companion object {
        val VALID_AUTH_CREDITS = AuthCredits(
            email = "rembozebest@gmail.com",
            password = "s89ZwDnNPaBMWFf"
        )
        val EMPTY_AUTH_CREDITS = AuthCredits(
            email = "",
            password = ""
        )
    }

    private val authService = ProstoAuth_WebImpl()

    @Test
    fun testSuccessAuth() = runTest {
        val authResult = authService.signIn(VALID_AUTH_CREDITS)
        assertTrue(authResult.isAuthSuccess)
        assertNotNull(authResult.authSession)
    }

    @Test
    fun testFailAuth() = runTest {
        val authResult = authService.signIn(EMPTY_AUTH_CREDITS)
        assertFalse(authResult.isAuthSuccess)
        assertNull(authResult.authSession)
    }

    @Test
    fun testCheckExpiredSession() = runTest {
        val sessionExpired = authService.isExpired(AuthSession(""))
        assertTrue(sessionExpired)
    }

    @Test
    fun testCheckValidSession() = runTest {
        val authResult = authService.signIn(VALID_AUTH_CREDITS)
        val sessionExpired = authService.isExpired(authResult.authSession!!)
        assertTrue(authResult.isAuthSuccess)
        assertNotNull(authResult.authSession)
        assertFalse(sessionExpired)
    }
}