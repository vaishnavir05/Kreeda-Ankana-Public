package com.example.kreedaankana.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.kreedaankana.data.db.entity.Booking

@Dao
interface BookingDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)

    @Query("SELECT * FROM bookings ORDER BY startTimeMillis DESC")
    fun getAllBookings(): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY startTimeMillis DESC")
    fun getBookingsByUser(userId: Int): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE groundId = :groundId AND date = :date AND status = 'CONFIRMED' ORDER BY startTimeMillis ASC")
    suspend fun getBookingsForGroundOnDate(groundId: Int, date: String): List<Booking>

    @Query("SELECT * FROM bookings WHERE date = :date AND status = 'CONFIRMED' ORDER BY startTimeMillis ASC")
    fun getBookingsOnDate(date: String): LiveData<List<Booking>>

    @Query("SELECT * FROM bookings WHERE userId = :userId AND date = :date AND status = 'CONFIRMED' ORDER BY startTimeMillis ASC")
    fun getBookingsForUserOnDate(userId: Int, date: String): LiveData<List<Booking>>

    @Query("""
        SELECT COUNT(*) FROM bookings 
        WHERE userId = :userId AND groundId = :groundId 
        AND date = :date AND status = 'CONFIRMED'
        AND slotType = :slotType
    """)
    suspend fun countBookingsForUserOnDate(userId: Int, groundId: Int, date: String, slotType: String): Int

    @Query("""
        SELECT COUNT(*) FROM bookings 
        WHERE userId = :userId 
        AND date >= :weekStart AND date <= :weekEnd 
        AND status = 'CONFIRMED'
        AND slotType = :slotType
    """)
    suspend fun countBookingsForUserInWeek(userId: Int, weekStart: String, weekEnd: String, slotType: String): Int

    @Query("""
        SELECT * FROM bookings 
        WHERE groundId = :groundId AND date = :date
        AND status = 'CONFIRMED'
        AND NOT (endTimeMillis <= :startMs OR startTimeMillis >= :endMs)
    """)
    suspend fun getOverlappingBookings(groundId: Int, date: String, startMs: Long, endMs: Long): List<Booking>

    @Query("UPDATE bookings SET status = 'CANCELLED' WHERE id = :bookingId")
    suspend fun cancelBooking(bookingId: Int)

    @Query("SELECT * FROM bookings WHERE matchId = :matchId LIMIT 1")
    suspend fun getBookingByMatchId(matchId: Int): Booking?
}
