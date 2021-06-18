package com.manuelmacaj.bottomnavigation.View.runpackage

import android.app.AlertDialog
import android.content.*
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import com.manuelmacaj.bottomnavigation.Service.GPSService
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class RunSessionActivity : AppCompatActivity() {

    private var track: MutableList<LatLng> = ArrayList()
    private val TAG = "RunSessionActivity"

    private lateinit var btnRunSession: Button
    private lateinit var btn_endRun: Button
    private lateinit var chronometer: Chronometer

    private lateinit var textKM: TextView
    private var isRunning: Boolean = false
    private var totalKM: Double = 0.0
    private var timeWhenStop = 0L
    private var currentTime: Date = Calendar.getInstance().time

    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_session)

        btnRunSession = findViewById(R.id.btnStartService)
        btn_endRun = findViewById(R.id.btnStopService)
        chronometer = findViewById(R.id.chronometer)
        textKM = findViewById(R.id.textViewTotalKm)

        chronometer.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - chronometer.base
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val t = (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
            chronometer.text = t
        }
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.text = "00:00:00"

        enableButtons()
    }

    private fun enableButtons() {

        btnRunSession.setOnClickListener {

            if (!isRunning) {
                startGPSService()
                chronometer.base = SystemClock.elapsedRealtime() + timeWhenStop
                chronometer.start()
                isRunning = true
                Log.d(TAG, "Avvio corsa")
            } else {
                timeWhenStop = chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa
                chronometer.stop() // pausa cronometro
                isRunning = false
                Log.d(TAG, "Stop corsa")
            }
            btnRunSession.setText(if (!isRunning) R.string.resume else R.string.stop)

        }

        btn_endRun.setOnClickListener {
            Log.d(TAG, "Fine della corsa")

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleEndRun))
                .setMessage(getString(R.string.messageEndRun))
                .setPositiveButton(getString(R.string.yesButton)) { _, _ ->

                    timeWhenStop = chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa
                    chronometer.stop() //fermo il cronometro
                    isRunning = false //l'utente non corre più, lo imposto a false

                    val intent = Intent(applicationContext, GPSService::class.java)
                    stopService(intent) //fermo il servizio
                    Log.d(TAG, PolyUtil.encode(track))

                    sendToFirebase()
                }
                .setNegativeButton("No") { _, _ ->
                    //non faccio niente, proseguo la corsa
                }
                .create()
                .show()
        }
    }

    private fun startGPSService() {
        val intent = Intent(applicationContext, GPSService::class.java)
        startForegroundService(intent)
    }

    private fun sendToFirebase() {
        //Creo una HashMap che mi servità quando caricherò i dati della sessione appena conclusa
        val sessionMap = HashMap<String, Any>()
        sessionMap["TimeWhenStart"] = currentTime
        sessionMap["Polyline encode"] = PolyUtil.encode(track)
        sessionMap["Chilometri prercorsi"] = textKM.text.toString()
        sessionMap["Tempo"] = chronometer.text.toString()

        /*Creo un oggetto di tipo Collection Reference che mi permette di accedere
         alla collezione Utenti -> documento (idUtente) -> collezione SessioneCorsa */

        val mFirestore = FirebaseFirestore.getInstance().collection("Utenti").document(Global.utenteLoggato?.idUtente.toString()).collection("SessioniCorsa")
        mFirestore.document().set(sessionMap).addOnCompleteListener { task -> //creo un nuovo documento (document è senza parametro, Firebase provvederà alla creazione di un documento un id generato)
            if (task.isSuccessful) {
                Toast.makeText(this, "Caricamento della sessione eseguita", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Impossibile caricare la sessione", Toast.LENGTH_LONG).show()
            }
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

                        if(isRunning) { //se l'utente sta correndo allora calcolo i km, se è in pausa non li calcolo
                            calculateKilometersDuringRun(lat, lng) //chiamata del metodo di calcolo dei km percorsi
                            textKM.text = String.format("%.2f", totalKM) //inserisco il risultato finale dei km percorsi
                            textKM.append(" Km") //aggiungo la stringa contenente l'unità di misura utilizzata
                        }

                        addCoordinates(lat, lng) //chiamata del metodo per l'aggiunta delle coordinate
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
