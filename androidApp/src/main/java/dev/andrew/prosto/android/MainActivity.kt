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
import androidx.lifecycle.lifecycleScope
import com.alphicc.brick.AndroidAnimatedComponentsContainer
import com.alphicc.brick.Component
import com.alphicc.brick.TreeRouter
import dev.andrew.prosto.ProstoDestination
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.ToporObject
import dev.andrew.prosto.android.compose.CreateTicketScreen
import dev.andrew.prosto.android.compose.MainScreen
import dev.andrew.prosto.android.compose.SignInViewDialog
import dev.andrew.prosto.android.compose.TicketQRDialog
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.ProstoTicket
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private companion object {
        val router = TreeRouter.new()
        val componentMainScreen = Component<Unit>(
            key = "MainScreen",
            content = { _, _ ->
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
                return@Component args.get<ProstoTicket>()
            },
            content = { args, _ ->
                TicketQRDialog(ticket = args.get<ProstoTicket>(),
                    onDismissRequest = {
                        router.onBackClicked()
                    })
            }
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        lifecycleScope.launch {
            ToporObject.navigator.navState.collect { navState ->
                when (navState) {
                    is ProstoDestination.OnBackPressed -> {
                        router.onBackClicked()
                    }
                    is ProstoDestination.MainScreen -> {
                        router.addComponent(componentMainScreen)
                    }
                    is ProstoDestination.AuthDialog -> {
                        router.addChild(childAuthDialog)
                    }
                    is ProstoDestination.CreateTicketScreen -> {
                        router.addComponent(componentCreateTicket, navState.coworking)
                    }
                    is ProstoDestination.TicketQRDialog -> {
                        router.addChild(componentTicketQRDialog, navState.ticket)
                    }
                }
            }
        }
    }
}

@Composable
fun rememberProstoLogoPainter(): VectorPainter {
    val mainVector = ImageVector.vectorResource(id = R.drawable.black_logo)
    val mainPainter = rememberVectorPainter(image = mainVector)
    return mainPainter
}

@Composable
fun rememberSPBMetroLogoPainter(): VectorPainter {
    val mainVector = ImageVector.vectorResource(id = R.drawable.spb_metro_logo)
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

