package dev.andrew.prosto.android.compose

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.andrew.prosto.ProstoTheme
import dev.andrew.prosto.repository.Coworking

@Composable
fun CoworkingItemView(coworking: Coworking, isSelected: Boolean) {
    Card(
        modifier = Modifier
            .padding(10.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(coworking.firmColor)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )
    ) {
        Surface(
            modifier = Modifier
                .animateContentSize(),
            shape = CardDefaults.shape
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxHeight(0.3f),
                model = coworking.tumblrLink,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
        }

        Text(
            modifier = Modifier
                .height(44.dp)
                .fillMaxWidth(),
            text = coworking.fullName,
            color = Color.Black,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoworkingHorizontalListView(
    list: List<Coworking>,
    pagerState: PagerState = rememberPagerState(),
    flingBehavior: SnapFlingBehavior = PagerDefaults.flingBehavior(state = pagerState),
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
        Orientation.Horizontal
    ),
    onSelect: (coworking: Coworking) -> Unit
) {
    LaunchedEffect(pagerState.currentPage) {
        onSelect(list[pagerState.currentPage])
    }

    HorizontalPager(
        pageCount = list.size,
        state = pagerState,
        flingBehavior = flingBehavior,
        pageNestedScrollConnection = pageNestedScrollConnection,
    ) { page ->
        val item = list[page]
        val isSelected = page == pagerState.currentPage
        CoworkingItemView(item, isSelected)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
fun ListPreviewLight(@PreviewParameter(CoworkingListProvider::class) coworkingList: List<Coworking>) {
    ProstoTheme {
        CoworkingHorizontalListView(coworkingList) {
        }
    }
}
