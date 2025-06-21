package com.srh.randomuserapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.srh.randomuserapp.data.models.User

/**
 * Main Room database for the RandomUser application.
 * Manages local storage of user data with SQLite backend.
 */
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to User data operations
     * @return UserDao instance
     */
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "random_user_database"

        /**
         * Creates and configures the Room database instance
         * @param context Application context
         * @return Configured database instance
         */
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
    }
}