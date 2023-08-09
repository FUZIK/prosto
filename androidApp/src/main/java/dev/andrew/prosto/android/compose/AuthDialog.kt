package dev.andrew.prosto.android.compose

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.android.compose.utilities.ProgressButton
import dev.andrew.prosto.controller.SignInDialogController
import dev.andrew.prosto.controller.SignInDialogEvent
import kotlinx.coroutines.android.awaitFrame

private val AUTH_FORM_WIDTH = 262.dp
private val AUTH_FORM_EL_SPACING = 15.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignInViewDialog() {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) {
        SignInDialogController(coroutineScope = coroutineScope) }
    val state by controller.state.collectAsState()
    val loginFocusRequester = remember { FocusRequester() }

    Dialog(
        onDismissRequest = {
           controller.emitEvent(SignInDialogEvent.OnDismiss())
        },
        properties = DialogProperties(securePolicy = SecureFlagPolicy.SecureOn)
    ) {
        Card(Modifier.wrapContentSize(unbounded = true)) {
            Box(Modifier.padding(20.dp)) {
                Column(
                    Modifier
                        .align(Alignment.Center)) {
                    Column(Modifier.width(AUTH_FORM_WIDTH)) {
                        ProstoLogo(modifier = Modifier
                            .width(220.dp)
                            .padding(bottom = AUTH_FORM_EL_SPACING * 1.5f))
                        OutlinedTextField(
                            modifier = Modifier
                                .focusRequester(loginFocusRequester)
                                .padding(bottom = AUTH_FORM_EL_SPACING),
                            value = state.email,
                            placeholder = {
                                Text("e-mail")
                            },
                            isError = state.emailInputError != null,
                            supportingText = {
                                state.emailInputError?.let { error ->
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            singleLine = true,
                            onValueChange = {
                                controller.emitEvent(SignInDialogEvent.OnEmailInput(it))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions()
                        )
                        var trailingIconVisible by rememberSaveable { mutableStateOf(false) }
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(bottom = AUTH_FORM_EL_SPACING * 2),
                            value = state.password,
                            placeholder = {
                                Text("пароль")
                            },
                            isError = state.passwordInputError != null,
                            supportingText = {
                                state.passwordInputError?.let { error ->
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = error,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            trailingIcon = {
                                AnimatedVisibility(visible = state.password.isNotEmpty()) {
                                    val image = if (trailingIconVisible)
                                        Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff

                                    val description = if (trailingIconVisible) "Hide password" else "Show password"
                                    IconButton(onClick = {
                                        trailingIconVisible = !trailingIconVisible
                                    }){
                                        Icon(imageVector  = image, description)
                                    }
                                }
                            },
                            singleLine = true,
                            onValueChange = {
                                controller.emitEvent(SignInDialogEvent.OnPasswordInput(it))
                            },
                            visualTransformation = if (trailingIconVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(onGo = {
                                controller.emitEvent(SignInDialogEvent.OnClickSignInAction())
                            })
                        )
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = state.signInError ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(Modifier.fillMaxWidth()) {
                            ProgressButton(modifier = Modifier
                                .weight(1f)
                                .padding(end = 15.dp),
                                enabled = state.isActionButtonEnabled,
                                inProgress = state.signInProgress,
                                onClick = {
                                    controller.emitEvent(SignInDialogEvent.OnClickSignInAction())
                                }) {
                                Text(text = "далее")
                            }
                            val uriHandler = LocalUriHandler.current
                            TextButton(modifier = Modifier
                                .weight(1f),
                                onClick = {
                                    uriHandler.openUri("https://простоспб.рф/auth/")
                                }) {
                                Text(modifier = Modifier
                                    .align(Alignment.CenterVertically),
                                    color = Color(0xFF1A73E8),
                                    textAlign = TextAlign.End,
                                    softWrap = false,
                                    text = "забыли пароль?")
                            }
                        }
                    }
                }
            }
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(loginFocusRequester) {
        // TODO замечено падение на samsung beyond1 (Galaxy S10) Android 11 (SDK 30)
        //        Exception java.lang.IllegalStateException:
        //        FocusRequester is not initialized. Here are some possible fixes:
        //        1. Remember the FocusRequester: val focusRequester = remember { FocusRequester() }
        //        2. Did you forget to add a Modifier.focusRequester() ?
        //        3. Are you attempting to request focus during composition? Focus requests should be made in
        //        response to some event. Eg Modifier.clickable { focusRequester.requestFocus() }
        loginFocusRequester.requestFocus()
        awaitFrame()
        keyboardController?.show()
    }
}

@Composable
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun SignInPreviewLight() {
    ProstoTheme {
        SignInViewDialog()
    }
}