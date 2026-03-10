package com.quran.tathbeet.core.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

interface TimeProvider {
    fun today(): LocalDate

    fun now(): ZonedDateTime

    fun zoneId(): ZoneId
}
