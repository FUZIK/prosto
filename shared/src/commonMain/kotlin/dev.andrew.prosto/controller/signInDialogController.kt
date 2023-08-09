package dev.andrew.prosto.controller

import dev.andrew.prosto.navigation.ProstoDestination
import dev.andrew.prosto.navigation.ProstoNavigator
import dev.andrew.prosto.StateUIController
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.database.UserAuthLocalStore
import dev.andrew.prosto.repository.AuthCredits
import dev.andrew.prosto.updateState
import dev.andrew.prosto.usecase.SignInError
import dev.andrew.prosto.usecase.SignInUseCase
import dev.andrew.prosto.withState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

data class SignInDialogState(
    val email: String = "",
    val password: String = "",
    val emailInputError: String? = null,
    val passwordInputError: String? = null,
    val signInProgress: Boolean = false,
    val signInError: String? = null,
    val isActionButtonEnabled: Boolean = true,
)

sealed interface SignInDialogEvent {
    class OnEmailInput(val input: String): SignInDialogEvent
    class OnPasswordInput(val input: String): SignInDialogEvent
    class OnClickSignInAction(): SignInDialogEvent
    class OnSuccessSignIn(): SignInDialogEvent
    class OnDismiss(): SignInDialogEvent
}

class SignInDialogController(
    coroutineScope: CoroutineScope,
    private val navigator: ProstoNavigator = ToporObject.navigator,
    private val signInUseCase: SignInUseCase = ToporObject.signInUseCase,
    private val userAuthLocalStore: UserAuthLocalStore = ToporObject.userAuthLocalStore,
): StateUIController<SignInDialogState, SignInDialogEvent>(initial = SignInDialogState()) {
    companion object {
        fun isValidEmailFormat(email: String)
                = email.isNotEmpty() && run {
            email.indexOf('@').let { ai ->
                ai >= 1 && email.lastIndexOf('.').let { di ->
                    di > ai && di - ai > 1 && email.length - di > 1 }}}
        fun isValidPasswordFormat(password: String)
                = password.isNotEmpty() && password.length > 5
    }

    private val signInScope = coroutineScope + Job()

    init {
        userAuthLocalStore.savedCredits?.also { savedCredits ->
            emitEvent(SignInDialogEvent.OnEmailInput(savedCredits.email))
            emitEvent(SignInDialogEvent.OnPasswordInput(savedCredits.password))
        }
    }

    private fun signIn() {
        updateState {
            copy(
                signInProgress = true,
                isActionButtonEnabled = false
            )
        }
        signInScope.coroutineContext.cancelChildren()
        signInScope.launch {
            try {
                withState {
                    signInUseCase.signIn(
                        email = email,
                        password = password,
                        saveCredits = true)
                }
                emitEvent(SignInDialogEvent.OnSuccessSignIn())
            } catch (e: SignInError) {
                updateState {
                    copy(
                        signInError = e.message ?: "Unresolved error",
                        signInProgress = false,
                        isActionButtonEnabled = true
                    )
                }
                emitEvent(SignInDialogEvent.OnDismiss())
            }
        }
    }

    override fun reduce(state: SignInDialogState, event: SignInDialogEvent) {
        when (event) {
            is SignInDialogEvent.OnEmailInput -> {
                setState(state.copy(
                    email = event.input,
                    emailInputError = if (isValidEmailFormat(event.input)) null else "Email invalid"
                ))
            }
            is SignInDialogEvent.OnPasswordInput -> {
                event.input.let { password ->
                    val isValid = isValidPasswordFormat(event.input)
                    setState(state.copy(
                        password = password,
                        passwordInputError = if (isValid) null else "Password invalid"
                    ))
                }
            }
            is SignInDialogEvent.OnClickSignInAction -> {
                signIn()
            }
            is SignInDialogEvent.OnSuccessSignIn -> {
                setState(state.copy(
                    signInProgress = false))
                userAuthLocalStore.savedCredits = AuthCredits(email = state.email, password = state.password)
                navigator.navigateBack()
            }
            is SignInDialogEvent.OnDismiss -> {
                setState(state.copy(
                    signInProgress = false))
                navigator.navigateBack()
                navigator.navigateBack()
            }
        }
    }
}