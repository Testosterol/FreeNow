package com.testosterolapp.freenow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Vehicle {

    @PrimaryKey
    var id: Int = 0

    var latitude: Double? = null
    var longitude: Double? = null
    var fleetType: String? = null
    var heading: Double? = null


    constructor(id: Int, latitude: Double?,longitude: Double?,fleetType: String?,heading: Double?, ){
        this.id = id
        this.latitude = latitude
        this.longitude = longitude
        this.fleetType = fleetType
        this.heading = heading
    }
}