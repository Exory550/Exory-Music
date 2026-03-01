package com.exory550.exorymusic.util

import java.util.*

class CalendarUtil {
    private val calendar = Calendar.getInstance()

    val elapsedToday: Long
        get() = (calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]) * MS_PER_MINUTE + calendar[Calendar.SECOND] * 1000 + calendar[Calendar.MILLISECOND]

    val elapsedWeek: Long
        get() {
            var elapsed = elapsedToday
            val passedWeekdays = calendar[Calendar.DAY_OF_WEEK] - 1 - calendar.firstDayOfWeek
            if (passedWeekdays > 0) {
                elapsed += passedWeekdays * MS_PER_DAY
            }
            return elapsed
        }

    val elapsedMonth: Long
        get() = elapsedToday + (calendar[Calendar.DAY_OF_MONTH] - 1) * MS_PER_DAY

    fun getElapsedMonths(numMonths: Int): Long {
        var elapsed = elapsedMonth

        var month = calendar[Calendar.MONTH]
        var year = calendar[Calendar.YEAR]
        for (i in 0 until numMonths) {
            month--
            if (month < Calendar.JANUARY) {
                month = Calendar.DECEMBER
                year--
            }
            elapsed += getDaysInMonth(month) * MS_PER_DAY
        }
        return elapsed
    }

    val elapsedYear: Long
        get() {
            var elapsed = elapsedMonth
            var month = calendar[Calendar.MONTH] - 1
            while (month > Calendar.JANUARY) {
                elapsed += getDaysInMonth(month) * MS_PER_DAY
                month--
            }
            return elapsed
        }

    private fun getDaysInMonth(month: Int): Int {
        val monthCal: Calendar = GregorianCalendar(calendar[Calendar.YEAR], month, 1)
        return monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getElapsedDays(numDays: Int): Long {
        var elapsed = elapsedToday
        elapsed += numDays * MS_PER_DAY
        return elapsed
    }

    companion object {
        private const val MS_PER_MINUTE = (60 * 1000).toLong()
        private const val MS_PER_DAY = 24 * 60 * MS_PER_MINUTE
    }
}
