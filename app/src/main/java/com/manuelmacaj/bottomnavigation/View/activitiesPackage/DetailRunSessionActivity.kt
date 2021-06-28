package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.R
import java.time.LocalTime
import kotlin.time.hours


class DetailRunSessionActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG: String = "DetailRunSessionActivity"
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private var polylineList: MutableList<LatLng> = ArrayList()
    private lateinit var averageSpeed: TextView
    private lateinit var polylineEncode: String
    private lateinit var time: TextView
    private lateinit var distance: TextView
    private lateinit var averagePace: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_run_session)

        title = intent.getStringExtra("date")

        polylineEncode = intent.getStringExtra("polyline").toString()
        polylineList = PolyUtil.decode(polylineEncode)
        mapView = findViewById(R.id.mapViewDetailRun) //configurazione a livello visivo della mappa di google
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // azione assolutamente necessaria nel momento in cui si include un oggetto di tipo MapView

        time = findViewById(R.id.textViewTimeValue)
        distance = findViewById(R.id.textViewDistanceDetailValue)
        averagePace = findViewById(R.id.textViewAverageDetailValue)
        averageSpeed = findViewById(R.id.textViewAverageSpeedValue)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        time.text = intent.getStringExtra("time")
        distance.text = intent.getStringExtra("distance")
        averagePace.text = intent.getStringExtra("averagePace")
        calculateAverageSpeed()
    }

    private fun calculateAverageSpeed() {
        val timeList = time.text.split(":")
        val km = distance.text.split(" ")
        //val timeRun = LocalTime.of(timeList[0].toInt(), timeList[1].toInt(), timeList[2].toInt())
        val kmValue = km[0].toDouble()

        val velocitaMedia:Double  =kmValue / (timeList[0].toDouble() + (timeList[1].toDouble() / 60) + (timeList[2].toDouble() / 3600))

        averageSpeed.text = String.format("%.2f", velocitaMedia)
        averageSpeed.append(" km/h")

    }

    override fun onMapReady(googleMap: GoogleMap) { //Metodo implementato da OnMapReadyCallback, per la gestione della mappa di GoogleMaps
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false // disabilito il location button.
        map.isBuildingsEnabled = false

        map.addPolyline(
            PolylineOptions() //funzione per decodificare polyline in una lista
                .addAll(polylineList)
                .width(5F) //quanto è marcata la polyline
                .color(Color.RED)
                .geodesic(true)
        )

        zoomToFit()

        val markerStartSession =
            LatLng(polylineList.first().latitude, polylineList.first().longitude)
        val markerEndSession = LatLng(polylineList.last().latitude, polylineList.last().longitude)

        // Marker per indicare la partenza
        map.addMarker(
            MarkerOptions()
                .position(markerStartSession)
                .title(getString(R.string.departureMarker))
        )
        // Marker per indicare l'arrivo
        map.addMarker(
            MarkerOptions()
                .position(markerEndSession)
                .title(getString(R.string.endMarker))
        )
    }
    private fun checkGooglePlayService(): Boolean { // metodo per controllare se il device presenta il Google Play Service
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return resultCode == ConnectionResult.SUCCESS
    }

    // fonte: https://stackoverflow.com/questions/25531477/google-map-v2-zoom-to-fit-all-markers-and-poly-lines
    private fun zoomToFit() { // metodo per il posizionamento della telecamera sulla mappa
        val builder = LatLngBounds.Builder()

        for(i in 0 until polylineList.size) {
            builder.include(polylineList[i])
        }
        val bounds = builder.build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 2000, null)
    }

    override fun onStart() {
        super.onStart()
        if (!checkGooglePlayService()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleNoGooglePlayServices))
                .setMessage(getString(R.string.messageNoGooglePlayService))
                .setPositiveButton("Ok") { _, _ ->
                    finish() // siccome il Google Play service è necessario, chiudo l'app
                }
                .setCancelable(false) // l'utente dovrà necessariamente premere il bottone ok, dato che il Play Service è necessario
                .create()
                .show()
        } else {
            Log.d(TAG, "Google Play Service installed")
        }
    }
}