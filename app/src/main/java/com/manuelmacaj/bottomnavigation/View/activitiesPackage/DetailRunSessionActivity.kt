package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_run_session.*

class DetailRunSessionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var sessionBeginDate: TextView
    private lateinit var polylineEncode: String
    private lateinit var time: TextView
    private lateinit var distance: TextView
    private lateinit var averagePale: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_run_session)
        title = "Dettaglio sessione corsa"
        polylineEncode = intent.getStringExtra("polyline").toString()
        mapView =
            findViewById<MapView>(R.id.mapViewDetailRun) //configurazione a livello visivo della mappa di google
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // azione assolutamente necessaria nel momento in cui si include un oggetto di tipo MapView

        sessionBeginDate = findViewById(R.id.textViewTimeWhenStart)
        time = findViewById(R.id.textViewTimeValue)
        distance = findViewById(R.id.textViewDistanceDetailValue)
        averagePale = findViewById(R.id.textViewAverageDetailValue)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        sessionBeginDate.text = intent.getStringExtra("date")
        time.text = intent.getStringExtra("time")
        distance.text = intent.getStringExtra("distance")
        averagePale.text = intent.getStringExtra("averagePale")


    }

    override fun onMapReady(googleMap: GoogleMap) { //Metodo implementato da OnMapReadyCallback, per la gestione della mappa di GoogleMaps
        map = googleMap
        map.uiSettings.setAllGesturesEnabled(false) //Tutte le gesture possibili sulla mappa sono disabilitate
        map.uiSettings.isMyLocationButtonEnabled = false // disabilito il location button.
        map.isBuildingsEnabled = false

        map.addPolyline(
            PolylineOptions() //funzione per decodificare polyline in una lista
                .addAll(PolyUtil.decode(polylineEncode))
                .width(4F) //quanto è marcata la polyline
                .color(Color.BLUE)
                .geodesic(false)
        )
    }

}