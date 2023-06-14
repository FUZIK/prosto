package dev.andrew.prosto.android.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.android.getMetroColor
import dev.andrew.prosto.android.rememberSPBMetroLogoPainter
import dev.andrew.prosto.controller.MainScreenController
import dev.andrew.prosto.controller.MainScreenEvent
import dev.andrew.prosto.repository.Coworking


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen() {
    val coroutineScope = rememberCoroutineScope()
    val controller = remember(coroutineScope) { MainScreenController(coroutineScope) }
    val state by controller.state.collectAsState()

    if (state.isCoworkingListLoading) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center))
        }
    } else {
        val coworking = state.coworking!!
        val metroColor = getMetroColor(coworking)

        val pagerState = rememberPagerState(initialPage = state.coworkingIndex)
        val flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        val pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
            Orientation.Horizontal
        )

        val scrollableModifier = Modifier.scrollable(
            state = pagerState,
            orientation = Orientation.Horizontal,
            flingBehavior = flingBehavior,
            reverseDirection = true)

        Scaffold(
            topBar = {
                CoworkingHorizontalListView(
                    list = state.coworkingList,
                    pagerState = pagerState,
                    flingBehavior = flingBehavior,
                    pageNestedScrollConnection = pageNestedScrollConnection) {
                    controller.emitEvent(MainScreenEvent.OnCoworkingSelect(coworking = it))
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.then(scrollableModifier),
                    onClick = {
                        controller.emitEvent(MainScreenEvent.OnClickTicketCreate())
                    },
                    containerColor = Color(coworking.firmColor)
                ) {
                    Text(text = "РЕ\nГА",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                }
            },
            content = {
                Box(modifier = Modifier
                    .padding(it)
                    .then(scrollableModifier)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val uriHandler = LocalUriHandler.current
                        CoworkingAddressCard(Modifier.padding(15.dp), {
                            try {
                                uriHandler.openUri("geo:0,0?q=${coworking.fullAddress}")
                            } catch (_: Throwable) {
                            }
                         }, coworking, metroColor)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)) {
                            if (state.ticketListIsLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else {
                                state.ticketListWithTodayIndex.let { ticketListWithTodayIndex ->
                                    if (ticketListWithTodayIndex.tickets.isEmpty()) {
                                        EmptyTicketItem(modifier = Modifier
                                            .fillMaxHeight(0.8f)
                                            .align(Alignment.Center))
                                    } else {
                                        TicketListView(
                                            modifier = Modifier
                                                .fillMaxHeight(0.8f)
                                                .align(Alignment.Center),
                                            tickets = ticketListWithTodayIndex.tickets,
                                            initialFirstVisibleItemIndex = ticketListWithTodayIndex.indexOfTodayItem,
                                            onClick = { ticket ->
                                                controller.emitEvent(MainScreenEvent.OnClickTicket(ticket = ticket))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = { Box {
                    Text(modifier = Modifier
                        .fillMaxWidth()
                        .then(scrollableModifier),
                        text = coworking.licenseRead,
                        textAlign = TextAlign.Center,
                        color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun CoworkingAddressCard(
    modifier: Modifier = Modifier,
    onTargetClick: () -> Unit,
    coworking: Coworking,
    metroColor: Color
) {
    Card(modifier = Modifier
        .padding(top = 10.dp)
        .wrapContentHeight()
        .then(modifier)
        .clickable {
            onTargetClick()
        }) {
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 7.dp, bottom = 7.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .padding(9.dp)
                        .weight(1f),
                    text = coworking.shortAddress,
                    maxLines = 1,
                    fontSize = 19.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    imageVector = Icons.Filled.OpenInBrowser,
                    contentDescription = null
                )
            }
            Divider(color = Color.White, thickness = 1.dp)
            Row(modifier = Modifier.padding(9.dp)) {
                Icon(
                    modifier = Modifier.size(27.dp),
                    painter = rememberSPBMetroLogoPainter(),
                    contentDescription = "",
                    tint = metroColor
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 10.dp),
                    text = coworking.metroStation.stationName,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    ProstoTheme {
        MainScreen()
    }
}