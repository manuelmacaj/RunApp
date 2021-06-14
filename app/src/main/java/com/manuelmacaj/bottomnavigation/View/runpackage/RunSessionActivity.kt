package com.manuelmacaj.bottomnavigation.View.runpackage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.Service.GPSService
import com.manuelmacaj.bottomnavigation.R


class RunSessionActivity : AppCompatActivity() {

    private var track: MutableList<LatLng> = ArrayList()
    private val TAG = "RunSessionActivity"

    private lateinit var btnRunSession: Button
    private lateinit var btn_stop: Button
    private lateinit var textView: TextView
    private lateinit var polylineText: EditText
    private lateinit var chronometer: Chronometer

    private lateinit var textKM: TextView
    private var isRuning: Boolean = false
    private var totalKM: Double = 0.0
    private var timeWhenStop = 0L

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_session)

        btnRunSession = findViewById(R.id.btnStartService)
        btn_stop = findViewById(R.id.btnStopService)
        textView = findViewById(R.id.textViewCoordinate)
        polylineText = findViewById(R.id.editTextPolyline)
        chronometer = findViewById(R.id.chronometer)
        textKM = findViewById(R.id.textViewTotalKm)

        enableButtons()
    }

    private fun enableButtons() {

        btnRunSession.setOnClickListener {

            if (!isRuning) {
                startGPSService()
                chronometer.base = SystemClock.elapsedRealtime() + timeWhenStop
                chronometer.start()
                isRuning = true
                Log.d(TAG, "Avvio corsa")
            } else {
                timeWhenStop = chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa
                chronometer.stop() // pausa cronometro
                isRuning = false
                Log.d(TAG, "Stop corsa")
            }
            btnRunSession.setText(if (!isRuning) R.string.resume else R.string.stop)

        }

        btn_stop.setOnClickListener {
            Log.d(TAG, "Fine del servizio")

            timeWhenStop = chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa
            chronometer.stop() // pausa cronometro

            btn_stop.isClickable = false
            btnRunSession.isClickable = true

            val intent = Intent(applicationContext, GPSService::class.java)
            stopService(intent)
            Log.d(TAG, PolyUtil.encode(track))
            polylineText.setText(PolyUtil.encode(track))
        }
    }

    private fun startGPSService() {
        val intent = Intent(applicationContext, GPSService::class.java)
        startForegroundService(intent)
    }

    override fun onResume() {
        super.onResume()

        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {

                    if (intent != null) {
                        val lat = intent.extras?.get("Lat") as Double
                        val lng = intent.extras?.get("Lng") as Double

                        if(isRuning) { //se l'utente sta correndo allora calcolo i km, se è in pausa non li calcolo
                            calculateKilometersDuringRun(lat, lng) //chiamata del metodo di calcolo dei km percorsi
                            textKM.text = String.format("%.2f", totalKM) //inserisco il risultato finale dei km percorsi
                            textKM.append(" Km")
                        }

                        addCoordinates(lat, lng)
                    }
                }
            }
        }

        registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }

    private fun calculateKilometersDuringRun(lat: Double, lng: Double) {

        val currentLocation = Location("")
        currentLocation.latitude = lat
        currentLocation.longitude = lng

        if (track.isNotEmpty()) {

            val previousLocation = Location("")
            previousLocation.latitude = track[track.size - 1].latitude
            previousLocation.longitude = track[track.size - 1].longitude

            //val distanceInMeters = previousLocation.distanceTo(currentLocation)
            val metersToKm = Measure(
                previousLocation.distanceTo(currentLocation) / 1000,
                MeasureUnit.KILOMETER
            )

            totalKM += metersToKm.number.toDouble()

            Log.d(TAG, "km percorsi: $totalKM ${metersToKm.unit} ")
        }
    }

    private fun addCoordinates(lat: Double, lng: Double) {
        val currentLocation = Location("")
        currentLocation.latitude = lat
        currentLocation.longitude = lng

        if (track.isNotEmpty()) {
            val previousLocation = Location("")
            previousLocation.latitude = track[track.size - 1].latitude
            previousLocation.longitude = track[track.size - 1].longitude

            if (previousLocation != currentLocation) { //questa condizione mi permette di non aggiungere le geolocalizzazioni uguali, così da avere una polyline pulita
                track.add(LatLng(lat, lng))
                Log.d(TAG, "Acquisisco la posizione")
            } else {
                Log.d(TAG, "Non acquisisco la posizione")
            }

        }
        else {
            track.add(LatLng(lat, lng)) // aggiungo alla lista le coordinate che il service mi ha fornito
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
        finish()
    }
}
