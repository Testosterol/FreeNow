package com.testosterolapp.freenow.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.testosterolapp.freenow.R
import com.testosterolapp.freenow.data.Vehicle
import com.testosterolapp.freenow.database.DaoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class DbUtil {

    companion object {

        fun launchCoroutineForInsertDataIntoDatabase(dataJsonObject: JSONObject, context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                insertData(dataJsonObject, context)
            }
        }

        private suspend fun insertData(dataJsonObject: JSONObject, context: Context) {
            val daoRepository = DaoRepository(context)
            val jsonArr = dataJsonObject.getJSONArray(context.getString(R.string.poiList))
            for (j in 0 until jsonArr.length()) {
                val vehicleToInsert = Vehicle(
                    jsonArr.getJSONObject(j).get(context.getString(R.string.id)) as Int,
                    jsonArr.getJSONObject(j).getJSONObject(context.getString(R.string.coordinate))
                        .get(context.getString(R.string.latitude)) as Double,
                    jsonArr.getJSONObject(j).getJSONObject(context.getString(R.string.coordinate))
                        .get(context.getString(R.string.longitude)) as Double,
                    jsonArr.getJSONObject(j).get(context.getString(R.string.fleetType)) as String,
                    jsonArr.getJSONObject(j).get(context.getString(R.string.heading)) as Double
                )
                daoRepository.insertVehicle(vehicleToInsert)
            }
        }
    }
}