package com.testosterolapp.freenow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var latitude: Double? = null
    var longitude: Double? = null


    constructor(latitude: Double?,longitude: Double? ){
        this.latitude = latitude
        this.longitude = longitude
    }
}