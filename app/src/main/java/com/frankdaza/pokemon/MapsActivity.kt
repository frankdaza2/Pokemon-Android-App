package com.frankdaza.pokemon

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.frankdaza.pokemon.model.Pokemon

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var location: Location = Location("Start")
    private var oldLocation: Location = Location("Start")
    private val accessCode = 123
    private var listPokemon = ArrayList<Pokemon>()
    private var playerPower: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        loadPokemons()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkPermission()
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), accessCode)
            }
        }
        getUserLocation()
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        Toast.makeText(this, "User location access on", Toast.LENGTH_LONG).show()

        var myLocation = MyLocationListener()
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 3f, myLocation)
        var myThread = MyThread()
        myThread.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            this.accessCode -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getUserLocation()
                else
                    Toast.makeText(this, "We cannot access to your location", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun loadPokemons() {
        this.listPokemon.add(Pokemon("Bulbasaur", R.drawable.bulbasaur, "It is from forest", 40.0, 3.3784764, -76.5224232))
        this.listPokemon.add(Pokemon("Charmander", R.drawable.charmander, "It is a fire beast", 65.0, 3.3781865, -76.5220044))
        this.listPokemon.add(Pokemon("Squirtle", R.drawable.squirtle, "It is a water pokemon", 59.7, 3.3783303, -76.5222941))
    }

    inner class MyLocationListener : LocationListener {

        constructor() {
            location = Location("Start")
            location.latitude = 3.3784764
            location.longitude = -76.5224232
        }

        override fun onLocationChanged(p0: Location?) {
            location = p0!!
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderEnabled(p0: String?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onProviderDisabled(p0: String?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    inner class MyThread : Thread {

        constructor() : super() {
            oldLocation = Location("Start")
            oldLocation.latitude = 3.37
            oldLocation.longitude = -76.52
        }

        override fun run() {
            try {
                while (true) {
                    if (oldLocation.distanceTo(location) == 0f) {
                        continue
                    }

                    oldLocation = location

                    runOnUiThread {
                        mMap.clear()

                        // Show me
                        // Add a marker in Sydney and move the camera
                        val sydney = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions()
                                .position(sydney)
                                .title("Me")
                                .snippet("Here is my location - My Power is: $playerPower")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f))

                        // Show Pokemons
                        for (pokemon in listPokemon) {
                            if (pokemon.isCatch == false) {
                                val sydney = LatLng(pokemon.location.latitude, pokemon.location.longitude)
                                mMap.addMarker(MarkerOptions()
                                        .position(sydney)
                                        .title(pokemon.name)
                                        .snippet(pokemon.description + " - Power: ${pokemon.power}")
                                        .icon(BitmapDescriptorFactory.fromResource(pokemon.image)))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20f))

                                if (location.distanceTo(pokemon.location) <= 3) {
                                    pokemon.isCatch = true
                                    playerPower += pokemon.power
                                    Toast.makeText(applicationContext, "You catch a new ${pokemon.name}, your new power is $playerPower", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    Thread.sleep(1000)
                }
            } catch (e: Exception) {

            }
        }
    }

}
