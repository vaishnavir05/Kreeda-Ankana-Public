package com.example.kreedaankana.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val displayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

    fun formatTime12Hr(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return displayFormat.format(cal.time).uppercase(Locale.ROOT)
    }

    fun toMillis(date: String, time: String): Long {
        return try {
            dateTimeFormat.parse("$date $time")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun todayString(): String = dateFormat.format(Date())

    fun formatDateDisplay(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr)
            dateDisplayFormat.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getWeekRange(date: String = todayString()): Pair<String, String> {
        val cal = Calendar.getInstance()
        val d = dateFormat.parse(date)
        if (d != null) cal.time = d
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val end = dateFormat.format(cal.time)
        return Pair(start, end)
    }

    fun getMonthDays(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun addMinutesToTime(time: String, date: String, minutes: Int): Long {
        val ms = toMillis(date, time)
        return ms + (minutes * 60 * 1000L)
    }

    fun millisToDisplayTime(millis: Long): String {
        return displayFormat.format(Date(millis)).uppercase(Locale.ROOT)
    }

    fun millisToDate(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    fun formatDateTime(date: String, time: String): String {
        return "${formatDateDisplay(date)}, $time"
    }
}
