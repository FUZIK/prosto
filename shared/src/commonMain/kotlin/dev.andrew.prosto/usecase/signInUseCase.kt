package dev.andrew.prosto.usecase

import dev.andrew.prosto.database.UserAuthLocalStore
import dev.andrew.prosto.repository.AuthCredits
import dev.andrew.prosto.repository.AuthResult
import dev.andrew.prosto.repository.AuthSource
import kotlin.coroutines.cancellation.CancellationException

class SignInError(message: String): Throwable(message)

interface SignInUseCase {
    @Throws(SignInError::class, CancellationException::class)
    suspend fun signIn(email: String, password: String, saveCredits: Boolean): AuthResult

}

class SignInUseCaseImpl(
    private val authSoure: AuthSource,
    private val userAuthLocalStore: UserAuthLocalStore
): SignInUseCase {
    override suspend fun signIn(email: String, password: String, saveCredits: Boolean): AuthResult {
        val credits = AuthCredits(email, password)
        val authResult = authSoure.signIn(info = credits)
        if (authResult.errorMessage != null) {
            throw SignInError(authResult.errorMessage)
        }
        if (authResult.authSession == null) {
            throw SignInError("Session not provided")
        } else {
            return authResult
        }
    }
}