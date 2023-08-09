package dev.andrew.prosto.android.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import dev.andrew.prosto.*
import dev.andrew.prosto.repository.ProstoTicket
import dev.andrew.prosto.utilities.localFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TicketListView(modifier: Modifier = Modifier,
                   tickets: List<ProstoTicket>,
                   initialFirstVisibleItemIndex: Int = 0,
                   onClick: (ProstoTicket) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initialFirstVisibleItemIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    LazyRow(modifier = modifier.fillMaxSize(),
        state = state,
        flingBehavior = flingBehavior) {
        itemsIndexed(items = tickets) { i, ticket ->
            Spacer(modifier = Modifier.fillParentMaxWidth(0.05f))

            val cardContent: @Composable ColumnScope.() -> Unit = {
                Box(
                    Modifier
                        .weight(1f)
                        .wrapContentSize()) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = "талон на",
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center)
                        Text(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            text = ticket.date.localFormat(),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center)
                        if (ticket.isToday()) {
                            Icon(modifier = Modifier
                                .fillParentMaxSize()
                                .align(Alignment.CenterHorizontally),
                                imageVector = Icons.Filled.QrCodeScanner, contentDescription = "")
                        }
                    }
                }
            }

            OutlinedCard(
                Modifier
                    .fillParentMaxWidth(0.9f)
                    .fillParentMaxHeight(1f)
                    .clickable {
                        onClick(ticket)
                    },
                content = cardContent)

            if (i + 1 == tickets.size)
                Spacer(modifier = Modifier.fillParentMaxWidth(0.05f))
        }
    }

    LaunchedEffect(key1 = initialFirstVisibleItemIndex) {
        state.scrollToItem(initialFirstVisibleItemIndex)
    }
}

@Composable
fun EmptyTicketItem(modifier: Modifier = Modifier) {
    Card(modifier = modifier
        .fillMaxWidth(0.9f)) {
            Box(Modifier.fillMaxSize()) {
                Column(Modifier
                    .align(Alignment.Center)) {
                    Row(Modifier
                        .align(Alignment.CenterHorizontally)) {
                        val qrModifier = Modifier.size(88.dp)
                        Image(modifier = qrModifier
                            .offset(x = 15.dp, y = 15.dp)
                            .rotate(-30f),
                            colorFilter = ColorFilter.tint(Color(0xFF6F797A)),
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "")
                        Image(modifier = qrModifier,
                            colorFilter = ColorFilter.tint(Color(0xFF6F797A)),
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "")
                        Image(modifier = qrModifier
                            .offset(x = -15.dp, y = 15.dp)
                            .rotate(30f),
                            colorFilter = ColorFilter.tint(Color(0xFF6F797A)),
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "")
                    }
                    Text(modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                        text = "Ой! Пусто" // text = "(ㆆ_ㆆ)"
                    )
                }
            }
    }
}

@Composable
@Preview
fun TicketEmptyListPreview(@PreviewParameter(TicketEmptyListProvider::class) tickets: List<ProstoTicket>) {
    ProstoTheme {
        Box {
            TicketListView(tickets = tickets, onClick = {})
        }
    }
}

@Composable
@Preview
fun TicketListPreview(@PreviewParameter(TicketListProvider::class) tickets: List<ProstoTicket>) {
    ProstoTheme {
        Box {
            TicketListView(tickets = tickets, onClick = {})
        }
    }
}
