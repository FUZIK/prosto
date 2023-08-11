package dev.andrew.prosto.utilities

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone

val UTC_ZONE = TimeZone.UTC
val SYSTEM_ZONE = TimeZone.currentSystemDefault()
val MSK_ZONE = UtcOffset(+3).asTimeZone()
val PROSTO_ZONE = MSK_ZONE

