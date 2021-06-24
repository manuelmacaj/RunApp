package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_run_session.*

class DetailRunSessionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private var polylineList: MutableList<LatLng> = ArrayList()
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
        polylineList = PolyUtil.decode(polylineEncode)
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
        //map.uiSettings.setAllGesturesEnabled(false) //Tutte le gesture possibili sulla mappa sono disabilitate
        map.uiSettings.isMyLocationButtonEnabled = false // disabilito il location button.
        map.isBuildingsEnabled = false

        map.addPolyline(
            PolylineOptions() //funzione per decodificare polyline in una lista
                .addAll(polylineList)
                .width(5F) //quanto è marcata la polyline
                .color(Color.BLUE)
                .geodesic(false)
        )
        val positionForCamera = LatLng(polylineList[polylineList.size/2].latitude, polylineList[polylineList.size/2].longitude)
        val cameraPositionPolyline = CameraPosition.builder() // mi costruisco la "Telecamera" che segue il "tondino blu"
            .target(positionForCamera) // indico dove si deve posizionare la camera
            .zoom(14f) // indico lo zoom della telecamera (minimo 1, massimo 20. consigliato tra il 15 e il 20 se vogliamo monitorare il posizionamento dell'utente)
            .build() // costruisco la variabile CameraPosition
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPositionPolyline))

    }

}