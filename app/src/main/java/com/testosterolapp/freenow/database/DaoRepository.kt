package com.testosterolapp.freenow.database

import android.content.Context
import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.data.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DaoRepository {

    var vehicleDao: VehicleDao? = null
    var userDao: UserDao? = null

    constructor(context: Context) : this() {
        val database = Database.getDatabase(context)
        this.vehicleDao = database.vehicleDao()!!
        this.userDao = database.userDao()!!
    }

    constructor()

    suspend fun insertVehicle(vehicle: Vehicle) {
        return vehicleDao?.insert(vehicle)!!
    }


    fun insertUser(user: User) {
        return userDao?.insert(user)!!
    }


    fun getUser(): User? {
        return userDao?.getUser()
    }

    fun updateUser(user: User) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao?.update(user)!!
        }
    }

}