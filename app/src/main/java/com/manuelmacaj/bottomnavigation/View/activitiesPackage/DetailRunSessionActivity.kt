package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.R

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
        polylineList = PolyUtil.decode(polylineEncode) //decodifica della polyline
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

        //prelevo informazioni dall'intent
        time.text = intent.getStringExtra("time")
        distance.text = intent.getStringExtra("distance")
        averagePace.text = intent.getStringExtra("averagePace")
        calculateAverageSpeed()
    }

    private fun calculateAverageSpeed() { //funzione per il calcolo della velocità media
        val timeList = time.text.split(":")
        val km = distance.text.split(" ")

        val kmValue: Double = if (km[0].contains(","))
            km[0].replace(",", ".").toDouble()
        else
            km[0].toDouble()

        val velocitaMedia: Double = //calcolo velocità media come distanza/tempo(in minuti)
            kmValue / (timeList[0].toDouble() + (timeList[1].toDouble() / 60) + (timeList[2].toDouble() / 3600))

        averageSpeed.text = String.format("%.2f", velocitaMedia)
        averageSpeed.append(" km/h")

    }

    override fun onMapReady(googleMap: GoogleMap) { //Metodo implementato da OnMapReadyCallback, per la gestione della mappa di GoogleMaps
        map = googleMap
        map.uiSettings.isMyLocationButtonEnabled = false // disabilito il location button.
        map.isBuildingsEnabled = false // non mostro le costruzioni in 3d sulla mappa

        map.addPolyline(
            PolylineOptions()
                .addAll(polylineList) // costruisco una polyline in base alla lista che ho a disposizione
                .width(5F) //quanto è lo spessore della polyline
                .color(Color.RED)
                .geodesic(false)
        )

        zoomToFit()

        val markerStartSession =
            LatLng(polylineList.first().latitude, polylineList.first().longitude) // coordinate marker di partenza del tracciato
        val markerEndSession = LatLng(polylineList.last().latitude, polylineList.last().longitude) //coordinate marker di arrivo del tracciato

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

    // fonte: https://stackoverflow.com/questions/25531477/google-map-v2-zoom-to-fit-all-markers-and-poly-lines
    private fun zoomToFit() { // metodo per il posizionamento della telecamera sulla mappa
        val builder = LatLngBounds.Builder()

        for(i in 0 until polylineList.size) {
            builder.include(polylineList[i])
        }
        val bounds = builder.build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 2000, null)
    }
}