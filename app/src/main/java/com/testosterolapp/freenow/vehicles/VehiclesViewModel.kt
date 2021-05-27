package com.testosterolapp.freenow.vehicles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.testosterolapp.freenow.data.Vehicle
import com.testosterolapp.freenow.database.VehicleDao

class VehiclesViewModel(application: Application) : AndroidViewModel(application) {

    var allRepositories: LiveData<PagedList<Vehicle>>? = null
    val filterTextAll = MutableLiveData<String>()

    fun init(vehicleDao: VehicleDao) {
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setPageSize(8)
            .setPrefetchDistance(10)
            .build()

        allRepositories = Transformations.switchMap(filterTextAll) {
            return@switchMap LivePagedListBuilder(vehicleDao.getInitialVehiclesInUnsortedOrder(),
                pagedListConfig)
                .build()
        }
    }
}