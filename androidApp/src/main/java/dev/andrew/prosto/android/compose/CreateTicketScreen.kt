package dev.andrew.prosto.android.compose

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.android.compose.utilities.LabeledCheckBox
import dev.andrew.prosto.android.compose.utilities.LabeledColumn
import dev.andrew.prosto.android.compose.utilities.ProgressButton
import dev.andrew.prosto.controller.CreateTicketScreenController
import dev.andrew.prosto.controller.TicketScreenDate
import dev.andrew.prosto.controller.TicketScreenEvent
import dev.andrew.prosto.controller.TicketScreenState
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.TicketParams
import kotlinx.datetime.LocalTime
import shimmerBackground

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun CreateTicketScreen(coworking: Coworking) {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) { CreateTicketScreenController(coworking, coroutineScope) }
    val state by controller.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("регистрация в коворкинг")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        controller.emitEvent(TicketScreenEvent.BackPressed())
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack,
                            contentDescription = "navigate to back")
                }})
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 7.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                    val visibleError = state.isTicketError != null
                    AnimatedVisibility(visible = visibleError) {
                        state.isTicketError?.also { isTicketError ->
                            Text(modifier = Modifier.fillMaxWidth(),
                                text = isTicketError,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.error)
                        }
                    }

                    ProgressButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(43.dp),
                        enabled = state.isTicketButtonEnabled,
                        inProgress = state.isTicketInProgress,
//                        colors = ButtonDefaults.buttonColors(containerColor = Color(coworking.firmColor)),
                        onClick = {
                            controller.emitEvent(TicketScreenEvent.OnRegisterClick())
                        }) {
                        Text(text = "Зарегаться", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            TabRow(selectedTabIndex = state.ticketDate.ordinal) {
                Tab(
                    text = { Text("cегодня") },
                    selected = state.ticketDate == TicketScreenDate.TODAY,
                    onClick = {
                        controller.emitEvent(TicketScreenEvent.OnTicketDateChanged(TicketScreenDate.TODAY))
                    }
                )
                Tab(
                    text = { Text("завтра") },
                    selected = state.ticketDate == TicketScreenDate.TOMORROW,
                    onClick = {
                        controller.emitEvent(TicketScreenEvent.OnTicketDateChanged(TicketScreenDate.TOMORROW))
                    }
                )
            }

//                .clickable {  }
//                .toggleable()
//                .draggable()

            Box(Modifier.padding(start = 10.dp, end = 10.dp)) {
                TicketCreateView(
                    state,
                    onParamChanged = { ticketParams ->
                        controller.emitEvent(TicketScreenEvent.OnTicketParamsChanged(ticketParams))
                    },
                    onSelectTime = { localTime, selected ->
                        controller.emitEvent(TicketScreenEvent.OnTicketTimeChanged(localTime, selected))
                    })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCreateView(state: TicketScreenState, onParamChanged: (TicketParams) -> Unit, onSelectTime: (LocalTime, Boolean) -> Unit) {
    val scrollState = rememberScrollState()
    val availableTimes = state.availableTimes
    val selectedTimes = state.selectedTimes
    Column(Modifier.verticalScroll(state = scrollState)) {
        LabeledColumn(label = "время") {
            FlowRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                mainAxisSpacing = 8.dp,
            ) {
                if (state.isTimesLoading) {
                    for (i in 0..11) {
                        Box(
                            Modifier
                                .size(
                                    width = (68 + 1).dp,
                                    height = (32 + 16).dp
                                )
                                .padding(vertical = 8.dp)
                                .shimmerBackground(FilterChipDefaults.shape))
                    }
                } else {
                    availableTimes.forEach { availableTime ->
                        val time = availableTime.time
                        val selected = selectedTimes.contains(time)
                        FilterChip(
                            selected = selected,
                            modifier = Modifier,
                            enabled = availableTime.isAvailable,
                            onClick = {
                                onSelectTime(time, !selected)
                            },
                            label = {
                                Text(text = time.toString())
                            })
                    }
                }
            }
        }
        val selectedParams = state.selectedParams
        LabeledColumn(label = "цели") {
            LabeledCheckBox(checked = selectedParams.isIndependentWork, label = "самостоятельная работа", onValueChange = {
                onParamChanged(selectedParams.copy(isIndependentWork = it))
            })
            LabeledCheckBox(checked = selectedParams.isOrganizationRecreation, label = "организация собственного досуга (отдых)", onValueChange = {
                onParamChanged(selectedParams.copy(isOrganizationRecreation = it))
            })
            LabeledCheckBox(checked = selectedParams.isIndependentProjectWork, label = "самостоятельная работа в рамках Проектной деятельности", onValueChange = {
                onParamChanged(selectedParams.copy(isIndependentProjectWork = it))
            })
        }
        LabeledColumn(label = "техника") {
            LabeledCheckBox(checked = selectedParams.noNeedAnyMachines, label = "не нужна орг. техника", onValueChange = {
                onParamChanged(selectedParams.copy(noNeedAnyMachines = it))
            })
            LabeledCheckBox(checked = selectedParams.needKomp, enabled = !selectedParams.noNeedAnyMachines, label = "ноутбук (не доступно в ПРОСТО.Калиниский)", onValueChange = {
                onParamChanged(selectedParams.copy(needKomp = it))
            })
            LabeledCheckBox(checked = selectedParams.needMFUPrinter, enabled = !selectedParams.noNeedAnyMachines, label = "принтер (МФУ)(не доступно в ПРОСТО.Калининский)", onValueChange = {
                onParamChanged(selectedParams.copy(needMFUPrinter = it))
            })
            LabeledCheckBox(checked = selectedParams.needFlipchart, enabled = !selectedParams.noNeedAnyMachines, label = "флипчарт", onValueChange = {
                onParamChanged(selectedParams.copy(needFlipchart = it))
            })
            LabeledCheckBox(checked = selectedParams.needLaminator, enabled = !selectedParams.noNeedAnyMachines, label = "ламинатор (доступно только в ПРОСТО.2SMART)", onValueChange = {
                onParamChanged(selectedParams.copy(needLaminator = it))
            })
            LabeledCheckBox(checked = selectedParams.needStaplerBindingMachine, enabled = !selectedParams.noNeedAnyMachines, label = "брошюратор (доступно только в ПРОСТО.2SMART)", onValueChange = {
                onParamChanged(selectedParams.copy(needStaplerBindingMachine = it))
            })
            LabeledCheckBox(checked = selectedParams.needOfficeSupplies, enabled = !selectedParams.noNeedAnyMachines, label = "расходные материалы (маркеры, ручки, бумага)", onValueChange = {
                onParamChanged(selectedParams.copy(needOfficeSupplies = it))
            })
        }
        LabeledColumn("дополнительно") {
            LabeledCheckBox(checked = selectedParams.needTemporaryStorage,  label ="шкафчик для временного хранения") {
                onParamChanged(selectedParams.copy(needTemporaryStorage = it))
            }
        }
    }

}

@Composable
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun CoTicketFormPreviewLight(@PreviewParameter(CoworkingProvider::class) coworking: Coworking) {
    ProstoTheme {
        CreateTicketScreen(coworking)
    }
}
