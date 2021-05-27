package com.testosterolapp.freenow

import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.data.Vehicle
import com.testosterolapp.freenow.serverCommunication.Url
import junit.framework.Assert
import org.junit.Test

class UnitTests {

    @Test
    fun testIsUrlCorrect() {
        val url = Url.URL_DATA
        Assert.assertEquals(url, "https://fake-poi-api.mytaxi.com/?p1Lat=53.694865&p1Lon=9.757589&p2Lat=53.394655&p2Lon=10.099891")
    }

    @Test
    fun testVehicleId() {
        val vehicle = Vehicle(4, 4.4,
            4.5, "Taxi", 6.6,)
        Assert.assertEquals(vehicle.id, 4)
    }

    @Test
    fun testVehicleLatitude() {
        val vehicle = Vehicle(4, 4.4,
            4.5, "Taxi", 6.6,)
        Assert.assertEquals(vehicle.latitude, 4.4)
    }

    @Test
    fun testVehicleLongitude() {
        val vehicle = Vehicle(4, 4.4,
            4.5, "Taxi", 6.6,)
        Assert.assertEquals(vehicle.longitude, 4.5)
    }

    @Test
    fun testVehicleFleetType() {
        val vehicle = Vehicle(4, 4.4,
            4.5, "Taxi", 6.6,)
        Assert.assertEquals(vehicle.fleetType, "Taxi")
    }

    @Test
    fun testVehicleHeading() {
        val vehicle = Vehicle(4, 4.4,
            4.5, "Taxi", 6.6,)
        Assert.assertEquals(vehicle.heading, 6.6)
    }

    @Test
    fun testUserLatitude() {
        val user = User(4.4, 4.5)
        Assert.assertEquals(user.latitude, 4.4)
    }

    @Test
    fun testuserLongitude() {
        val user = User(4.4, 4.5)
        Assert.assertEquals(user.longitude, 4.5)
    }




}