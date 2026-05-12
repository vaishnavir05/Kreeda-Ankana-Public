package com.example.kreedaankana.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kreedaankana.data.db.dao.*
import com.example.kreedaankana.data.db.entity.*

@Database(
    entities = [
        User::class,
        Sport::class,
        Ground::class,
        Booking::class,
        Team::class,
        TeamMember::class,
        CaptainRequest::class,
        Match::class,
        Score::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sportDao(): SportDao
    abstract fun groundDao(): GroundDao
    abstract fun bookingDao(): BookingDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kreeda_ankana_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
