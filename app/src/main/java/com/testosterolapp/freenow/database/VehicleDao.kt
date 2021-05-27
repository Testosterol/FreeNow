package com.testosterolapp.freenow.database

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.testosterolapp.freenow.data.Vehicle

@Dao
interface VehicleDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vehicle: Vehicle)

    @Query("SELECT * FROM vehicle")
    fun getInitialVehiclesInUnsortedOrder(): DataSource.Factory<Int, Vehicle>

    @Query("SELECT * FROM vehicle")
    fun getAll(): List<Vehicle?>?


}