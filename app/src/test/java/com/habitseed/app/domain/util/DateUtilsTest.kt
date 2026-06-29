package com.habitseed.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilsTest {

    @Test
    fun dateKey_usesLocalDateFormat() {
        val timestamp = ZonedDateTime.of(2026, 6, 29, 9, 15, 0, 0, ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        assertEquals("2026-06-29", DateUtils.getDateKey(timestamp))
    }

    @Test
    fun startAndEndOfDay_areCorrectForLocalTimezone() {
        val timestamp = ZonedDateTime.of(2026, 6, 29, 14, 45, 0, 0, ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val start = DateUtils.getStartOfDay(timestamp)
        val end = DateUtils.getEndOfDay(timestamp)

        assertTrue(start <= timestamp)
        assertTrue(end >= timestamp)
        assertEquals("2026-06-29", DateUtils.getDateKey(start))
        assertEquals("2026-06-29", DateUtils.getDateKey(end))
    }
}
