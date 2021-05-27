package com.testosterolapp.freenow.vehicles

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.testosterolapp.freenow.R
import com.testosterolapp.freenow.util.ClusterMarker
import com.testosterolapp.freenow.data.Vehicle
import com.testosterolapp.freenow.database.DaoRepository
import com.testosterolapp.freenow.util.Constants.MAPVIEW_BUNDLE_KEY
import com.testosterolapp.freenow.util.LinearLayoutManagerWrapper
import com.testosterolapp.freenow.util.MyClusterManagerRenderer
import com.testosterolapp.freenow.util.ViewWeightAnimationWrapper


class VehiclesActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener,
        VehiclesAdapter.VehicleAdapterClickListener {

    private val TAG = "VehiclesFragment"

    private val MAP_LAYOUT_STATE_CONTRACTED = 0
    private val MAP_LAYOUT_STATE_EXPANDED = 1

    //widgets
    private var mClusterMarkers: ArrayList<ClusterMarker> = ArrayList()
    private var mUserListRecyclerView: RecyclerView? = null

    //vars
    private var mLocation: Location? = null;
    private var mMapContainer: RelativeLayout? = null
    private var mMapView: MapView? = null
    private var mGoogleMap: GoogleMap? = null
    private var mMapBoundary: LatLngBounds? = null
    private var mClusterManager: ClusterManager<ClusterMarker>? = null
    private var mClusterManagerRenderer: MyClusterManagerRenderer? = null
    private var mMapLayoutState = 0
    lateinit var expandShrinkMapButton: AppCompatImageButton
    private var mGeoApiContext: GeoApiContext? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicles_list)

        mMapView = findViewById(R.id.vehicle_list_map);
        mMapContainer = findViewById(R.id.map_container);
        expandShrinkMapButton = findViewById(R.id.btn_full_screen_map)
        expandShrinkMapButton.setOnClickListener(this)

        initGoogleMap(savedInstanceState)
        getUserLocation(this)

        val model: VehiclesViewModel by viewModels()

        val recyclerView = findViewById<RecyclerView>(R.id.vehicle_list_recycler_view)
        mUserListRecyclerView = recyclerView
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManagerWrapper(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        divider.setDrawable(ContextCompat.getDrawable(baseContext, R.drawable.line_divider)!!)
        recyclerView.addItemDecoration(divider)
        recyclerView.setHasFixedSize(true)

        val adapter = VehiclesAdapter(this, this)
        recyclerView.adapter = adapter

        val repositoryDao = DaoRepository(this).vehicleDao
        model.init(repositoryDao!!)

        model.allRepositories!!.observe(this) { vehicles ->
            if (vehicles.isNotEmpty()) {
                adapter.submitList(vehicles)
                addMapMarkers(vehicles)
            }
        }
        model.filterTextAll.setValue("")
    }


    /**
     * Retrieving persisted user's location or retrieving the location through FusedLocationClient
     * if persisted data is not present
     */
    private fun getUserLocation(context: Context) {
        val daoRepository = DaoRepository(context)
        val user = daoRepository.getUser()
        if (user != null) {
            val temp = Location(LocationManager.GPS_PROVIDER)
            temp.latitude = user.latitude!!
            temp.longitude = user.longitude!!
            mLocation = temp
        } else {
            getLocationOfUserFromFusedLocationClient()
        }
    }

    /**
     * Method to retrieve user's location if the persisted data is not present
     */
    private fun getLocationOfUserFromFusedLocationClient() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                mLocation = location
            }
        }
    }

    /**
     * Method to set the camera view on the target
     */
    private fun setCameraView(location: Location) {
        // Set a boundary to start
        val bottomBoundary: Double = location.latitude - .1
        val leftBoundary: Double = location.longitude - .1
        val topBoundary: Double = location.latitude + .1
        val rightBoundary: Double = location.longitude + .1
        mMapBoundary = LatLngBounds(LatLng(bottomBoundary, leftBoundary), LatLng(topBoundary, rightBoundary))
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
    }

    /**
     * Method to add Map markers
     */
    private fun addMapMarkers(vehicles: List<Vehicle>) {
        if (mGoogleMap != null) {
            if (mClusterManager == null) {
                mClusterManager = ClusterManager<ClusterMarker>(this, mGoogleMap)
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = MyClusterManagerRenderer(
                    this,
                    mGoogleMap,
                    mClusterManager
                )
                mClusterManager!!.renderer = mClusterManagerRenderer
            }

            for (vehicle in vehicles) {
                try {
                    var snippet = ""
                    var avatar: Int = R.drawable.taxi // set the default avatar
                    val newClusterMarker =
                        ClusterMarker(
                            LatLng(vehicle.latitude!!, vehicle.longitude!!),
                            vehicle.fleetType, snippet, avatar, vehicle
                        )
                    if (!mClusterMarkers.contains(vehicle)) {
                        mClusterManager!!.addItem(newClusterMarker)
                        mClusterMarkers.add(newClusterMarker)
                    }
                } catch (e: NullPointerException) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.message)
                }
                mClusterManager!!.cluster()
                setCameraView(mLocation!!)
            }
        }
    }


    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView!!.onCreate(mapViewBundle)
        mMapView!!.getMapAsync(this)

        if (mGeoApiContext == null) {
            mGeoApiContext = getGeoContext()
        }
    }

    /**
     * Method to build GeoApiContext, necessary in order to determine routes
     */
    private fun getGeoContext(): GeoApiContext? {
        return GeoApiContext.Builder().apiKey("AIzaSyBsWd93uHi24oQEyUX0cULV8_S16YvSMtk").build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMapView!!.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mMapView!!.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMapView!!.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMapView!!.onStop()
    }

    override fun onPause() {
        mMapView!!.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMapView!!.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView!!.onLowMemory()
    }

    /**
     * Callback to let us know that google map is ready
     */
    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mGoogleMap = map;
        map.isMyLocationEnabled = true
    }


    /**
     * Animation method to animate expanding of the map
     */
    private fun expandMapAnimation() {
        val mapAnimationWrapper = ViewWeightAnimationWrapper(mMapContainer)
        val mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper, "weight", 50f, 100f)
        mapAnimation.duration = 800
        val recyclerAnimationWrapper = ViewWeightAnimationWrapper(mUserListRecyclerView)
        val recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper, "weight", 50f, 0f)
        recyclerAnimation.duration = 800
        recyclerAnimation.start()
        mapAnimation.start()
    }

    /**
     * Animation method to animate shrinking of the map back to 50/50
     */
    private fun contractMapAnimation() {
        val mapAnimationWrapper = ViewWeightAnimationWrapper(mMapContainer)
        val mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper, "weight", 100f, 50f)
        mapAnimation.duration = 800
        val recyclerAnimationWrapper = ViewWeightAnimationWrapper(mUserListRecyclerView)
        val recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper, "weight", 0f, 50f)
        recyclerAnimation.duration = 800
        recyclerAnimation.start()
        mapAnimation.start()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_full_screen_map -> {
                if (mMapLayoutState == MAP_LAYOUT_STATE_CONTRACTED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_EXPANDED
                    expandMapAnimation()
                } else if (mMapLayoutState == MAP_LAYOUT_STATE_EXPANDED) {
                    mMapLayoutState = MAP_LAYOUT_STATE_CONTRACTED
                    contractMapAnimation()
                }
            }
        }
    }

    /**
     * OnClick interface when an item is being clicked on in Adapter
     */
    override fun onVehicleClickListener(vehicle: Vehicle, v: View?, position: Int) {
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(vehicle.latitude!!,
            vehicle.longitude!!), 14.0f), 600, null)

        if (!mClusterMarkers.contains(vehicle)) {
            var snippet = ""
            var avatar: Int = R.drawable.taxi // set the default avatar
            val newClusterMarker = ClusterMarker(
                LatLng(vehicle.latitude!!, vehicle.longitude!!),
                vehicle.fleetType, snippet, avatar, vehicle
            )
            mClusterManager!!.addItem(newClusterMarker)
            mClusterMarkers.add(newClusterMarker)
            mClusterManager!!.cluster()

        }
    }


    /**
     * Method to calculate direction from taxi to user
     *
     * Does not work because google needs a billing account </3
     */
    private fun calculateDirections(vehicle: Vehicle) {
        Log.d(TAG, "calculateDirections: calculating directions.")
        val destination = com.google.maps.model.LatLng(vehicle.latitude!!, vehicle.longitude!!)
        val directions = DirectionsApiRequest(mGeoApiContext)
        directions.alternatives(false)
        directions.origin(com.google.maps.model.LatLng(mLocation!!.latitude, mLocation!!.longitude))
        Log.d(TAG, "calculateDirections: destination: $destination")
        directions.destination(destination).setCallback(object :
            PendingResult.Callback<DirectionsResult?> {
            override fun onResult(result: DirectionsResult?) {
                Log.d(TAG, "onResult: routes: " + result!!.routes[0].toString())
                Log.d(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString())
                addPolylinesToMap(result) // adding the polylines
            }

            override fun onFailure(e: Throwable?) {
                Log.d(TAG, "onFailure: failed to determine the route: " + e)
            }
        })
    }

    /**
     * Method to draw polylines on the map.
     *
     *  Does not work because google needs a billing account </3, shame really.
     */
    private fun addPolylinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "run: result routes: " + result.routes.size)
            for (route in result.routes) {
                Log.d(TAG, "run: leg: " + route.legs[0].toString())
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)
                val newDecodedPath: MutableList<LatLng> = ArrayList()

                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {
                    newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                }
                val polyline = mGoogleMap!!.addPolyline(PolylineOptions().addAll(newDecodedPath))
                polyline.color = ContextCompat.getColor(this, R.color.grey)
                polyline.isClickable = true
            }
        }
    }


}
