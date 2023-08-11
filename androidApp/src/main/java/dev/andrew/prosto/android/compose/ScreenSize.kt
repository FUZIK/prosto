package dev.andrew.prosto.android.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ScreenSize {
    @Composable
    fun height(): Dp {
        val configuration = LocalConfiguration.current
        configuration.screenHeightDp
        return configuration.screenHeightDp.dp
    }

    @Composable
    fun width(): Dp {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp.dp
    }
}
