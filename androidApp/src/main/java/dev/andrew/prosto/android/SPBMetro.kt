package dev.andrew.prosto.android

import androidx.compose.ui.graphics.Color
import dev.andrew.prosto.repository.Coworking
import dev.andrew.prosto.repository.MetroStation

private val MP_COLOR = Color(0xFF0078C9)
private val NV_COLOR = Color(0xFF009A49)
private val KV_COLOR = Color(0xFFD6083B)
private val FP_COLOR = Color(0xFF702785)

fun getMetroColor(coworking: Coworking): Color {
    return when (coworking.metroStation) {
        MetroStation.SPB_VASKA -> NV_COLOR
        MetroStation.SPB_MOSKOVSKAY -> MP_COLOR
        MetroStation.SPB_PARK_POBEDY -> MP_COLOR
        MetroStation.SPB_PLOSCHAD_MUZHESTVA -> KV_COLOR
        MetroStation.SPB_PETROGA -> MP_COLOR
        MetroStation.SPB_ELEKTRA -> MP_COLOR
        MetroStation.SPB_SADOVAYA -> FP_COLOR
    }
}