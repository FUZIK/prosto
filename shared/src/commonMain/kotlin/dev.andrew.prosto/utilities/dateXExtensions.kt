package dev.andrew.prosto.utilities

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun LocalDate.localFormat(now: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.UTC).date) = run {
    when(this.toEpochDays() - now.toEpochDays()) {
        3 -> "послепослезавтра"
        2 -> "послезавтра"
        1 -> "завтра"
        0 -> "сегодня"
        -1 -> "вчера"
        -2 -> "позавчера"
        -3 -> "позапозавчера"
        else -> "${dayOfMonth}.${monthNumber}.${year}"
    }
}