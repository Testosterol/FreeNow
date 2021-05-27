package com.testosterolapp.freenow.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.data.Vehicle

/**
 * Database to persist the data
 */
@androidx.room.Database(entities = [Vehicle::class, User::class], version = 2, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao?
    abstract fun userDao(): UserDao?


    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, Database::class.java, "db_vehicles")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}