package com.testosterolapp.freenow

import androidx.arch.core.executor.testing.CountingTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.testosterolapp.freenow.database.Database
import com.testosterolapp.freenow.database.UserDao
import com.testosterolapp.freenow.database.VehicleDao
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.TimeUnit

abstract class DatabaseTest {
    @Rule

    @JvmField
    val countingTaskExecutorRule = CountingTaskExecutorRule()
    protected lateinit var database: Database
    protected lateinit var vehicleDao: VehicleDao
    protected lateinit var userDao: UserDao

    @Before
    @Throws(Exception::class)
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            Database::class.java)
            .build()
        vehicleDao = database.vehicleDao()!!
        userDao = database.userDao()!!
    }
    @After
    @Throws(Exception::class)
    fun tearDown() {
        database.close()
    }

    fun drain() {
        countingTaskExecutorRule.drainTasks(10, TimeUnit.SECONDS)
    }
}