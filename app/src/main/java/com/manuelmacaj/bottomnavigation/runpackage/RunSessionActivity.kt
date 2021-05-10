package com.manuelmacaj.bottomnavigation.runpackage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_session)

        btn_start = findViewById(R.id.btnStartService)
        btn_stop = findViewById(R.id.btnStopService)
        textView = findViewById(R.id.textViewCoordinate)

        enable_buttons()

    }

    private fun enable_buttons() {

        btn_start.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d(TAG, "Inizio del servizio")
                startGPSService()
            }
        })


        btn_stop.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.d(TAG, "Fine del servizio")
                val intent = Intent(applicationContext, GPSService::class.java)
                stopService(intent)
                Log.d(TAG, PolyUtil.encode(track))
                textView.append(PolyUtil.encode(track))
            }
        })
    }

    private fun startGPSService() {
        val intent = Intent(applicationContext, GPSService::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        }
        else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent != null) {
                        val lat = intent.extras?.get("Lat") as Double
                        val lng = intent.extras?.get("Lng") as Double

                        track.add(LatLng(lat, lng))

                    }
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update"))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
        finish()
    }
}




/*  val chronometer = findViewById<Chronometer>(R.id.chronometer)
    val button = findViewById<Button>(R.id.button)

    var timeWhenStop = 0L //variabile di tipo long che mi servirà durante l'uso del cronometro

    button.setOnClickListener(object : View.OnClickListener {
        var isPlaying = false //variabile inizializzata a false
        override fun onClick(v: View?) {
            if (!isPlaying) { //se isPlaying è diverso da false avvio il cronometro

                chronometer.base = SystemClock.elapsedRealtime() + timeWhenStop
                chronometer.start() // avvio cronometro
                isPlaying = true

            } else { //altrimenti metto in pausa il cronometro
                timeWhenStop =
                    chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale delcronometro e il tempo trascorso dalla pausa
                chronometer.stop() // pausa cronometro
                isPlaying = false
            }
            button.setText(if (!isPlaying) R.string.start else R.string.stop)
        }


    })*/