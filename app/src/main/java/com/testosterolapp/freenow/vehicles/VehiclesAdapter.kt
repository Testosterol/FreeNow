package com.testosterolapp.freenow.vehicles

import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.testosterolapp.freenow.R
import com.testosterolapp.freenow.data.Vehicle
import com.testosterolapp.freenow.database.DaoRepository
import com.testosterolapp.freenow.util.GenericViewHolder


class VehiclesAdapter(context : Context, private val clickListener: VehicleAdapterClickListener) :
    PagedListAdapter<Vehicle, GenericViewHolder>(DiffUtilCallBack()) {

    private var vehicle: Vehicle? = null
    private val daoRepository: DaoRepository = DaoRepository(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val contactView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_layout,
            parent, false
        )
        return MyViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.onBindView(position)

    }

    inner class MyViewHolder(itemView: View) : GenericViewHolder(itemView) {
        private var fleetType: TextView = itemView.findViewById(R.id.fleet_type)
        private var distance: TextView = itemView.findViewById(R.id.distance)

        override fun onBindView(position: Int) {
            if (position <= -1) {
                return
            }
            vehicle = try {
                getItem(position)
            } catch (e: IndexOutOfBoundsException) {
                return
            }

            if(vehicle!!.fleetType.equals("POOLING")){
                fleetType.text = "Car sharing"
            }else{
                fleetType.text = vehicle!!.fleetType
            }

            calculateDistance()
            distance.text = String.format("Distance: %.2f km", calculateDistance())
        }

        init {
            itemView.setOnClickListener { v: View? -> clickListener.onVehicleClickListener(
                getItem(
                    adapterPosition
                )!!, v, adapterPosition
            ) }
        }

    }

    class DiffUtilCallBack : DiffUtil.ItemCallback<Vehicle>() {
        override fun areItemsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
            return oldItem.heading == newItem.heading
        }
    }

    interface VehicleAdapterClickListener {
        fun onVehicleClickListener(vehicle: Vehicle, v: View?, position: Int)
    }

    /**
     * Method to calculate the distance between the user and the vehicle
     */
    private fun calculateDistance(): Double {
        val user = daoRepository.getUser()
        val userLocation = Location("locationA")
        userLocation.latitude = user!!.latitude!!
        userLocation.longitude = user.longitude!!
        val vehicleLocation = Location("locationB")
        vehicleLocation.latitude = vehicle!!.latitude!!
        vehicleLocation.longitude = vehicle!!.longitude!!
        return (userLocation.distanceTo(vehicleLocation)/1000).toDouble()
    }



}


