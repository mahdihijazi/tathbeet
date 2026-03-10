package com.quran.tathbeet.core.time

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class SystemTimeProvider(
    private val currentZoneId: ZoneId = ZoneId.systemDefault(),
) : TimeProvider {
    override fun today(): LocalDate = LocalDate.now(currentZoneId)

    override fun now(): ZonedDateTime = ZonedDateTime.now(currentZoneId)

    override fun zoneId(): ZoneId = currentZoneId
}
