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
import android.view.View
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

    private lateinit var btn_start: Button
    private lateinit var btn_stop: Button
    private lateinit var textView: TextView
    private lateinit var polylineText: EditText
    private lateinit var chronometer: Chronometer
    private lateinit var textKM: TextView
    private var totalKM: Double = 0.0

    private var timeWhenStop = 0L

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_session)

        btn_start = findViewById(R.id.btnStartService)
        btn_stop = findViewById(R.id.btnStopService)
        textView = findViewById(R.id.textViewCoordinate)
        polylineText = findViewById(R.id.editTextPolyline)
        chronometer = findViewById(R.id.chronometer)
        textKM = findViewById(R.id.textViewTotalKm)

        enable_buttons()

    }

    private fun enable_buttons() {

        btn_start.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d(TAG, "Inizio del servizio")

                startGPSService()
                btn_start.isClickable = false
                btn_stop.isClickable = true

                chronometer.base = SystemClock.elapsedRealtime() + timeWhenStop
                chronometer.start()
            }
        })


        btn_stop.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d(TAG, "Fine del servizio")

                timeWhenStop = chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa
                chronometer.stop() // pausa cronometro

                btn_stop.isClickable = false
                btn_start.isClickable = true

                val intent = Intent(applicationContext, GPSService::class.java)
                stopService(intent)
                Log.d(TAG, PolyUtil.encode(track))
                polylineText.setText(PolyUtil.encode(track))
            }
        })
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

                        calculateKilometersDuringRun(lat, lng)

                        track.add(LatLng(lat, lng))
                        textKM.text = String.format("%.2f", totalKM)
                        textKM.append(" Km")

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
            val metersToKm = Measure(previousLocation.distanceTo(currentLocation)/1000, MeasureUnit.KILOMETER)

            totalKM += metersToKm.number.toDouble()

            Log.d(TAG, "km percorsi: $totalKM ${metersToKm.unit} ")

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
