package com.testosterolapp.freenow.main

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDex
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.testosterolapp.freenow.R
import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.database.DaoRepository
import com.testosterolapp.freenow.serverCommunication.GetServerData
import com.testosterolapp.freenow.util.Constants.ERROR_DIALOG_REQUEST
import com.testosterolapp.freenow.util.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.testosterolapp.freenow.util.Constants.PERMISSIONS_REQUEST_ENABLE_GPS
import com.testosterolapp.freenow.vehicles.VehiclesActivity

/**
 * Project introduction
 *
 * Dear Mr or Mrs / Dzien Dobry
 *
 * Project can be found on https://github.com/Testosterol/FreeNow
 *
 * I enjoyed the project very much, I wanted to fines it and make it look like an actual taxi/car sharing app.
 * However, after I finished my methods to calculate directions and draw Polygons on the map, I found out that in order to
 * get directions, google requires a Billing account and I didn't want to get charged. It's a pity because the app could have been much cooler.
 *
 * The app consists of one screen. Map on the top half focusing on the user's location and with drawn vehicles in Hamburg
 * and vehicles list from the server on the bottom part with some additional information.
 *
 * You are able to zoom in, zoom out, move around the map, expand the map to the full screen or shrink it back to 50/50,
 * you can re-focus the camera back to user's location. After clicking on any vehicle item in the list, the camera will automatically
 * move to the location of the vehicle and zoom in, showing the direction of which the car is heading to.
 *
 * Technicals:
 *
 * Code is straightforward. @MainActivity handles permissions and checks if google play services are available, it then requests
 * the data from the server, then it starts VehiclesActivity.
 *
 * VehiclesActivity does the bread and butter, it has PagedList adapter with ViewModel to render the List of the vehicles
 * into the UI and it also holds the map with the map functionalities.
 *
 * Other classes and packages handle rest of the work such as serverCommunication, Data persistance, Data models,
 * and util classes.
 *
 * If you have any questions please reach out to me.
 *
 * Have a great day.
 *
 */
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var mLocationPermissionGranted = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    /**
     * Method to check if google play services are available
     */
    fun isServicesOK(): Boolean {
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            val dialog: Dialog? = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog!!.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    /**
     * Method to request server data
     */
    private fun requestServerData() {
        val getServerData = GetServerData()
        getServerData.getServerData(this)
    }

    /**
     * Method to start Vehicle Activity where we store list of vehicles and displaying them on map
     */
    private fun instantiateVehicleActivity() {
        val intent = Intent(this@MainActivity, VehiclesActivity::class.java)
        startActivity(intent)
    }

    /**
     * Method to check if GPS is enabled
     */
    fun isMapsEnabled(): Boolean {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    /**
     * Alert dialog
     */
    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
            }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    /**
     * Method to request permissions if they are not present
     */
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            saveUser()
            requestServerData()
            instantiateVehicleActivity()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Response from permissions dialog
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    saveUser()
                    requestServerData()
                    instantiateVehicleActivity()
                } else {
                    getLocationPermission()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                saveUser()
                requestServerData()
                instantiateVehicleActivity()
            } else {
                getLocationPermission()
            }
        }
    }

    private fun saveUser() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastKnownLocationAndSaveUser()
    }

    /**
     * Get location of the user and persist in the room database.
     * LocationService will update the data when necessary.
     */
    private fun getLastKnownLocationAndSaveUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                val user = User(location.latitude, location.longitude)
                val daoRepository = DaoRepository(this)
                daoRepository.insertUser(user)
            }
        }
    }
}