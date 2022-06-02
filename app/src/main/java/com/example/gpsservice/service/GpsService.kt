package com.example.gpsservice.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper
import com.example.gpsservice.db.LocationDatabase
import com.google.android.gms.location.*
import com.example.gpsservice.entity.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class GpsService : IntentService("GpsService") {
    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationDatabase: LocationDatabase
    private lateinit var locationRequest : LocationRequest

    companion object{
        var GPS= "com.example.service.GPS_EVENT"
    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)
        getLocation()
    }

    @SuppressLint("MissingPermission")
    fun getLocation(){
        //  Inicializa los atributos locationCallback y fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()

        //  Coloca un intervalo de actualización de 10000 y una prioridad de PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            //  Recibe ubicación de gps mediante un onLocationResult
            override fun onLocationResult(locationResult : LocationResult) {
                if (locationRequest==null) {
                    return
                }
                // Dibujar en el mapa los puntos
                for (location in locationResult.locations) {

                    //  Envía un broadcast con una instancia de localización y la acción gps
                    val localizacion = Location(null,location.latitude,location.longitude)
                    val bcIntent=Intent()
                    bcIntent.action = GPS

                    //bcIntent.putExtra("location", locationResult.lastLocation)
                    bcIntent.putExtra("localizacion", localizacion)
                    sendBroadcast(bcIntent)

                    //  Guarda la localización en la BD)
                    locationDatabase.locationDAO.insert(Location(null, localizacion.latitude, localizacion.longitude))
                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Looper.loop()
    }
}