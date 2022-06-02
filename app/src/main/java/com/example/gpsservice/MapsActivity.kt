package com.example.gpsservice

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.gpsservice.databinding.ActivityMapsBinding
import com.example.gpsservice.db.LocationDatabase
import com.example.gpsservice.entity.Location
import com.example.gpsservice.service.GpsService
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private lateinit var mMap: GoogleMap
private lateinit var locationDatabase: LocationDatabase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationDatabase=LocationDatabase.getInstance(this)
        validaPermisos()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        iniciaServicio()

        recuperarPuntos(mMap)
    }

    //  Obtener los puntos almacenados en la bd y mostrarlos en el mapa
    fun recuperarPuntos(googleMap:GoogleMap){
        mMap = googleMap

        for(location in locationDatabase.locationDAO.query()){
            val sydney = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        }
    }

    //  hace un filtro del broadcast GPS
    //  E inicia el servicio GpsService
    fun iniciaServicio(){
        val filter= IntentFilter()
        filter.addAction(GpsService.GPS)
        val rcv = ProgressReceiver()
        registerReceiver(rcv,filter)
        //  este debe contener ambas clases
        startService(Intent(this, GpsService::class.java))
    }


    //   Valida si la app tiene permisos de ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION
    //   si no tiene permisos solicita al usuario permisos(requestPermission)

    fun validaPermisos(){
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            // NO Tengo Permisos, entonces se solicitan
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                SOLICITAR_GPS
            )
        }
    }

    /**
     * validar que se le dieron los permisos a la app, en caso contrario salir
     */

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            SOLICITAR_GPS -> {
                if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.exit(1)
                }
            }
        }
    }

    // Clase para recibir los mensajes de broadcast
    class ProgressReceiver:BroadcastReceiver(){
        //  se obtiene el parametro enviado por el servicio (Location)
        //  coloca en el mapa la localizacion
        //  mueve la camara a esa localizacion
        override fun onReceive(context: Context, intent: Intent) {
            if(intent.action == GpsService.GPS){
                val localizacion : Location = intent?.getSerializableExtra("localizacion") as Location
                val valor = LatLng(localizacion.latitude,localizacion.longitude)
                mMap.addMarker(MarkerOptions().position(valor).title("Marker in Costa Rica"))
            }
        }
    }
}