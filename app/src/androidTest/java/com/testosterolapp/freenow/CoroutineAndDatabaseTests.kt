package com.testosterolapp.freenow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.data.Vehicle
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CoroutineAndDatabaseTests : DatabaseTest() {

    @After
    fun teardown() {
        // At the end of all tests, query executor should be idle.
        countingTaskExecutorRule.drainTasks(500, TimeUnit.MILLISECONDS)
        assert(countingTaskExecutorRule.isIdle)
    }

    @Test
    fun testVehicleRemovalDelete() {
        runBlocking {
            val vehicle = Vehicle(1,1.1,1.2,"taxi",1.3)
            vehicleDao.insert(vehicle)
            val vehicle2 = Vehicle(2,1.1,1.2,"taxi",1.3)
            vehicleDao.insert(vehicle2)
            Assert.assertEquals(vehicleDao.getAll()!!.size, 2)
            database.clearAllTables()
            assertEquals(vehicleDao.getAll(), listOf<Vehicle>())
        }
    }

    @Test
    fun testVehicleLongitudeSuspend() = runBlocking {
        val vehicle = Vehicle(1,1.1,1.2,"taxi",1.3)
        vehicleDao.insert(vehicle)
        assertEquals(vehicleDao.getAll()!![0]!!.longitude, ((vehicle.longitude)))
    }

    @Test
    fun testVehicleFleetTypeSuspend() = runBlocking {
        val vehicle = Vehicle(1,1.1,1.2,"taxi",1.3)
        vehicleDao.insert(vehicle)
        assertEquals(vehicleDao.getAll()!![0]!!.fleetType, ((vehicle.fleetType)))
    }

    @Test
    fun testVehicleHeadingSuspend() = runBlocking {
        val vehicle = Vehicle(1,1.1,1.2,"taxi",1.3)
        vehicleDao.insert(vehicle)
        assertEquals(vehicleDao.getAll()!![0]!!.heading, ((vehicle.heading)))
    }

    @Test
    fun testUserLatitudeSuspend() = runBlocking {
        val user = User(1.1,1.2)
        userDao.insert(user)
        assertEquals(userDao.getAll()!![0]!!.latitude, ((user.latitude)))
    }

    @Test
    fun testUserLongitudeSuspend() = runBlocking {
        val user = User(1.1,1.2)
        userDao.insert(user)
        assertEquals(userDao.getAll()!![0]!!.longitude, ((user.longitude)))
    }



}