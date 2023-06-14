package dev.andrew.prosto.usecase

import dev.andrew.prosto.repository.AuthSession
import dev.andrew.prosto.repository.AuthSource

interface IsSignInRequiredUseCase {
    suspend fun isSignInRequired(): Boolean
}

class IsSignInRequiredUseCaseImpl(
    private val authSource: AuthSource,
): IsSignInRequiredUseCase {
    override suspend fun isSignInRequired(): Boolean {
        return authSource.isExpired(AuthSession.EMPTY)
    }
}