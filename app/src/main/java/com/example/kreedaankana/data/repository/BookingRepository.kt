package com.example.kreedaankana.data.repository

import com.example.kreedaankana.data.db.dao.BookingDao
import com.example.kreedaankana.data.db.entity.Booking
import com.example.kreedaankana.utils.Constants
import com.example.kreedaankana.utils.TimeUtils

import com.example.kreedaankana.data.firebase.FirebaseService

class BookingRepository(
    private val bookingDao: BookingDao,
    private val firebaseService: FirebaseService
) {

    /**
     * Attempts to book a slot. Enforces all business rules:
     * 1. Max 2 bookings/day per user per ground
     * 2. Max 10 bookings/week per user
     * 3. No overlapping bookings on same ground
     * 4. Practice: 30–120 min; Match: 30–300 min
     */
    suspend fun bookSlot(
        userId: Int,
        groundId: Int,
        groundName: String,
        sport: String,
        slotType: String,
        date: String,
        startTime: String,
        durationMinutes: Int
    ): Result<Booking> {
        // Duration validation
        val maxDuration = if (slotType == Constants.SLOT_PRACTICE) 120 else 300
        if (durationMinutes < 30 || durationMinutes > maxDuration) {
            return Result.failure(Exception("Invalid duration for $slotType"))
        }

        val startMs = TimeUtils.toMillis(date, startTime)
        val endMs = startMs + (durationMinutes * 60 * 1000L)
        val endTime = TimeUtils.millisToDisplayTime(endMs)

        // Rule 1: Max daily bookings per user per ground
        val dayLimit = if (slotType == Constants.SLOT_PRACTICE) Constants.MAX_PRACTICE_PER_DAY else Constants.MAX_MATCH_PER_DAY
        val dayCount = bookingDao.countBookingsForUserOnDate(userId, groundId, date, slotType)
        if (dayCount >= dayLimit) {
            return Result.failure(Exception("max_daily"))
        }

        // Rule 2: Max weekly bookings
        val weekLimit = if (slotType == Constants.SLOT_PRACTICE) Constants.MAX_PRACTICE_PER_WEEK else Constants.MAX_MATCH_PER_WEEK
        val (weekStart, weekEnd) = TimeUtils.getWeekRange(date)
        val weekCount = bookingDao.countBookingsForUserInWeek(userId, weekStart, weekEnd, slotType)
        if (weekCount >= weekLimit) {
            return Result.failure(Exception("max_weekly"))
        }

        // Rule 3: No overlapping bookings
        val overlapping = bookingDao.getOverlappingBookings(groundId, date, startMs, endMs)
        if (overlapping.isNotEmpty()) {
            return Result.failure(Exception("overlap"))
        }

        val booking = Booking(
            userId = userId,
            groundId = groundId,
            groundName = groundName,
            sport = sport,
            slotType = slotType,
            date = date,
            startTime = startTime,
            endTime = endTime,
            startTimeMillis = startMs,
            endTimeMillis = endMs
        )
        val id = bookingDao.insertBooking(booking)
        val savedBooking = booking.copy(id = id.toInt())
        
        // Sync to Firebase
        val firebaseResult = firebaseService.saveBooking(savedBooking)
        if (firebaseResult.isSuccess) {
            val updatedBooking = savedBooking.copy(firebaseId = firebaseResult.getOrNull())
            bookingDao.updateBooking(updatedBooking)
            return Result.success(updatedBooking)
        }
        
        return Result.success(savedBooking)
    }

    suspend fun bookSlotForMatch(booking: Booking): Result<Booking> {
        // Rule 1: Max daily match bookings per user per ground
        val dayCount = bookingDao.countBookingsForUserOnDate(booking.userId, booking.groundId, booking.date, Constants.SLOT_MATCH)
        if (dayCount >= Constants.MAX_MATCH_PER_DAY) {
            return Result.failure(Exception("max_match_daily"))
        }

        // Rule 2: Max weekly match bookings
        val (weekStart, weekEnd) = TimeUtils.getWeekRange(booking.date)
        val weekCount = bookingDao.countBookingsForUserInWeek(booking.userId, weekStart, weekEnd, Constants.SLOT_MATCH)
        if (weekCount >= Constants.MAX_MATCH_PER_WEEK) {
            return Result.failure(Exception("max_match_weekly"))
        }

        val id = bookingDao.insertBooking(booking)
        val savedBooking = booking.copy(id = id.toInt())
        
        // Sync to Firebase
        val firebaseResult = firebaseService.saveBooking(savedBooking)
        if (firebaseResult.isSuccess) {
            val updatedBooking = savedBooking.copy(firebaseId = firebaseResult.getOrNull())
            bookingDao.updateBooking(updatedBooking)
            return Result.success(updatedBooking)
        }
        
        return Result.success(savedBooking)
    }

    fun getBookingsByUser(userId: Int) = bookingDao.getBookingsByUser(userId)
    fun getAllBookings() = bookingDao.getAllBookings()
    fun getBookingsOnDate(date: String) = bookingDao.getBookingsOnDate(date)
    fun getBookingsForUserOnDate(userId: Int, date: String) = bookingDao.getBookingsForUserOnDate(userId, date)
    suspend fun getBookingsForGroundOnDate(groundId: Int, date: String) =
        bookingDao.getBookingsForGroundOnDate(groundId, date)
    suspend fun cancelBooking(bookingId: Int) = bookingDao.cancelBooking(bookingId)
}
