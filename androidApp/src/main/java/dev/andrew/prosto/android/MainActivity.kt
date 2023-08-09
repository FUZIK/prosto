package dev.andrew.prosto.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.alphicc.brick.AndroidAnimatedComponentsContainer
import com.alphicc.brick.Component
import com.alphicc.brick.TreeRouter
import dev.andrew.prosto.navigation.ProstoDestination
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.android.compose.CreateTicketScreen
import dev.andrew.prosto.android.compose.MainScreen
import dev.andrew.prosto.android.compose.SignInViewDialog
import dev.andrew.prosto.android.compose.TicketQRDialog
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket

class MainActivity : ComponentActivity() {
    private companion object {
        val router = TreeRouter.new()
        val componentMainScreen = Component<Unit>(
            key = "MainScreen",
            content = { _, _ ->
                println("componentMainScreen")
                MainScreen()
            }
        )
        val childAuthDialog = Component<Unit>(
            key = "AuthDialog",
            content = { _, _ ->
                SignInViewDialog()
            }
        )
        val componentCreateTicket = Component(
            key = "TicketCreateScreen",
            onCreate = { _, args ->
                return@Component args.get<Coworking>()
            },
            content = { args, _ ->
                CreateTicketScreen(coworking = args.get<Coworking>())
            }
        )
        val componentTicketQRDialog = Component(
            key = "TicketQRDialog",
            onCreate = { _, args ->
                return@Component args.get<ProstoDestination.TicketQRDialog>()
            },
            content = { args, _ ->
                val dest: ProstoDestination.TicketQRDialog = args.get()
                TicketQRDialog(
                    coworking = dest.coworking,
                    ticket = dest.ticket)
            }
        )
        init {
            ToporObject.navigator.onDestinationChanged = { destination ->
                when (destination) {
                    is ProstoDestination.MainScreen -> {
                        router.addComponent(componentMainScreen)
                    }
                    is ProstoDestination.AuthDialog -> {
                        router.addChild(childAuthDialog)
                    }
                    is ProstoDestination.CreateTicketScreen -> {
                        router.addComponent(componentCreateTicket, destination.coworking)
                    }
                    is ProstoDestination.TicketQRDialog -> {
                        router.addChild(componentTicketQRDialog, destination)
                    }
                    else -> {}
                }
            }
            ToporObject.navigator.onBackPressed = {
                println("onBackClicked")
                router.onBackClicked()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate savedInstanceState == null / ${savedInstanceState == null}")
        setContent {
            ProstoTheme {
                Surface {
                    AndroidAnimatedComponentsContainer(
                        containerConnector = router,
                        onRouterEmpty = { finish() },
                        enterTransition = scaleIn(
                            initialScale = .90f,
                            animationSpec = tween(easing = LinearEasing)
                        ) + fadeIn(),
                        exitTransition = scaleOut(
                            targetScale = .90f,
                            animationSpec = tween(easing = LinearEasing)
                        ) + fadeOut())
                }
            }
        }
        if (savedInstanceState == null) {
            ToporObject.navigator.navigateTo(ProstoDestination.MainScreen())
        }
    }
}

@Composable
fun rememberProstoLogoPainter(): VectorPainter {
    val mainVector = ImageVector.vectorResource(id = R.drawable.prosto_icon)
    val mainPainter = rememberVectorPainter(image = mainVector)
    return mainPainter
}

@Composable
fun rememberSPBMetroLogoPainter(): VectorPainter {
    val mainVector = ImageVector.vectorResource(id = R.drawable.spb_metro_icon)
    val mainPainter = rememberVectorPainter(image = mainVector)
    return mainPainter
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarView() {
    val prostoPainter = rememberProstoLogoPainter()
    TopAppBar(
        title = {
            Icon(
                modifier = Modifier.width(222.dp),
                painter = prostoPainter,
                contentDescription = "ПРОСТО"
            )
        },
    )
}
