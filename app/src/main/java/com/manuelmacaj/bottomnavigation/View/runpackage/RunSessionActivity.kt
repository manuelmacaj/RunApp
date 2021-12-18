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
import kotlin.math.round

class RunSessionActivity : AppCompatActivity() {

    private var track: MutableList<LatLng> = ArrayList() //Lista di tipo LatLng in cui inserisco le coordinate
    private val TAG = "RunSessionActivity"

    private lateinit var btnRunSession: Button
    private lateinit var btnEndRun: Button
    private lateinit var chronometer: Chronometer
    private lateinit var textKM: TextView
    private lateinit var avaragePace: TextView

    private var andaturaAlKm: Double = 0.0
    private var isRunning: Boolean = false
    private var totalKM: Double = 0.0
    private var timeWhenStop = 0L
    private var currentTime: Date = Calendar.getInstance().time

    private var broadcastReceiver: BroadcastReceiver? = null //oggetto di tipo BroadcastReceiver che gestirà i sendBroadcast provenienti dal Servizio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_session)

        btnRunSession = findViewById(R.id.btnPlayPauseRun)
        btnEndRun = findViewById(R.id.btnEndRunSession)
        chronometer = findViewById(R.id.chronometer)
        textKM = findViewById(R.id.textViewTotalKm)
        avaragePace = findViewById(R.id.avaragePace)

        //Il listener mi permette di poter creare il formato HH:MM:SS per il cronometro
        //fonte: https://stackoverflow.com/questions/38237947/chronometer-with-hmmss/38238363
        chronometer.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - chronometer.base
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val t =
                (if (h < 10) "0$h" else h).toString() + ":" + (if (m < 10) "0$m" else m) + ":" + if (s < 10) "0$s" else s
            chronometer.text = t
        }
        chronometer.text = "00:00:00" // come verrà visualizzato il cronometro prima dell'avvio

        enableButtons()
    }

    private fun enableButtons() { //metodo con annesso i buttons presenti all'interno dell'Activity
        // setOnClick per la gestione della sessione corsa da parte dell'utente
        btnRunSession.setOnClickListener {
            if (!isRunning) { // se vuole correre
                startGPSService() //avvio il servizio di localizzazione
                startResumeRun()
                Log.d(TAG, "Avvio corsa")
            } else { // se invece non sta corendo o è in pausa
                stopRun()
                Log.d(TAG, "Stop corsa")
            }
            //btnRunSession.setText(if (!isRunning) R.string.resume else R.string.pause) //in base al valore di isRunning il testo del bottone cambierà
        }

        btnEndRun.setOnClickListener {
            btnEndRun.isClickable = false //una volta cliccato sul bottone, è visibile ma viene disattivato
            AlertDialog.Builder(this) //AlertDialog che chiede all'utente se vuole terminare la corsa
                .setTitle(getString(R.string.titleEndRun))
                .setMessage(getString(R.string.messageEndRun))
                .setPositiveButton(getString(R.string.yesButton)) { _, _ -> //se la risposta è si, allora la sessione corsa finisce qui
                    Log.d(TAG, "Fine della corsa")
                    chronometer.stop() //fermo il cronometro
                    isRunning = false // l'utente non corre più, lo imposto a false

                    val intent = Intent(applicationContext, GPSService::class.java) // Creo un intent per poter fermare il Service
                    stopService(intent) //fermo il servizio
                    Log.d(TAG, PolyUtil.encode(track))

                    sendToFirebaseFirestore() //chiamo il metodo per che mi invierà i risultati a firebase
                }
                .setNegativeButton("No") { dialog, _ ->
                    //non faccio niente, proseguo la corsa
                    dialog.cancel()
                    btnEndRun.isClickable = true //il bottone è di nuovo cliccabile
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun startResumeRun() {
        chronometer.base =
            SystemClock.elapsedRealtime() + timeWhenStop // il cronometro si avvia/riprende da dove si è fermato
        chronometer.start() // avvio il cronometro (si baserà sul calcolo fatto in precedenza)
        isRunning = true // isRunning lo imposto a true
    }

    private fun stopRun() {
        timeWhenStop =
            chronometer.base - SystemClock.elapsedRealtime() //calcolo il tempo che avanza tra il valore attuale del cronometro e il tempo trascorso dalla pausa (SystemClock.elapsedRealtime() = restituisce i millisecondi dall'avvio, incluso il tempo trascorso in modalità di sospensione).
        chronometer.stop() // pausa cronometro
        isRunning = false //isRunning lo imposto a false
        Log.d(TAG, "Stop corsa")

    }

    private fun startGPSService() { //metodo per avviare il servizio
        val intent = Intent(applicationContext, GPSService::class.java)
        startForegroundService(intent) //avvio il servizio (è simile a startService, ma posso far in modo che venga creata una notifica nel servizio
    }

    override fun onResume() {
        super.onResume()
        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) { //gestisco il broadcast receiver
                    if (intent != null) { // se l'intent presenta qualcosa allora...
                        val lat = intent.extras?.get("Lat") as Double //prelevo dall'intent la latitudine
                        val lng = intent.extras?.get("Lng") as Double //prelevo dall'intent la longitudine

                        if (isRunning) { //se l'utente sta correndo allora calcolo i km, se è in pausa non li calcolo
                            calculateKilometersDuringRun(lat, lng) //chiamata del metodo di calcolo dei km percorsi
                            textKM.text = String.format("%.2f", totalKM) //inserisco il risultato finale dei km percorsi
                            textKM.append(" Km") //aggiungo la stringa contenente l'unità di misura utilizzata
                        }
                        addCoordinates(lat, lng) //chiamata del metodo per l'aggiunta delle coordinate
                    }
                }
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("location_update")) //registro il broadcastReceiver
    }

    private fun calculateKilometersDuringRun(lat: Double, lng: Double) { // metodo per il calcolo dei km durante la corsa
        val currentLocation = Location("") //creo un oggetto di tipo Location
        currentLocation.latitude = lat //currentLocation contiene le informazioni della latitudine e longitudine prelevati nell'onReceive
        currentLocation.longitude = lng

        if (track.isNotEmpty()) { // Se la lista contiene già delle informazioni (quindi la lista non è vuota)
            val previousLocation = Location("") //Creo un oggetto di tipo Location
            previousLocation.latitude = track[track.size - 1].latitude //previousLocation contiene le ultime informazioni salvate nella lista
            previousLocation.longitude = track[track.size - 1].longitude

            val metersToKm = Measure(
                previousLocation.distanceTo(currentLocation) / 1000,
                MeasureUnit.KILOMETER
            )

            totalKM += metersToKm.number.toDouble() // effettuo la somma tra i km totali e km calcolati in precedenza

            if(totalKM > 0.01)
                calculateAveragePace() // chiamata al metodo per calcolare l'andatura della corsa

            Log.d(TAG, "km percorsi: $totalKM Km ")
        }
    }

    /*fonte sul funzionamento: https://www.polar.com/it/running-academy/running-pace-calculator*/

    private fun calculateAveragePace() {
        val time = SystemClock.elapsedRealtime() - chronometer.base //prelevo il tempo così da trovare le ore, minuti e secondi
        val h = (time / 3600000).toInt()
        val m = (time - h * 3600000).toInt() / 60000
        val s = (time - h * 3600000 - m * 60000).toInt() / 1000

        val totMin = ((h * 60) + (m) + (s.toDouble() / 60)) //calcolo il totale dei minuti (converto le ore e i secondi in minuti)

        andaturaAlKm = totMin / totalKM // calcolo l'andatura (rapporto tra il totale dei minuti e i km percorsi)

        // calcolo l'andatura della corsa
        val passoMinuti = (andaturaAlKm).toInt() //solo la parte intera del rapporto fatto in precedenza (la parte intera mi rappresenta i minuti)
        val passoSecondi  = round((andaturaAlKm - passoMinuti) * 60).toInt() //calcolo i secondi e arrondo il risultato e poi fornisco la parte intera

        avaragePace.text = (if (passoMinuti < 10) "0$passoMinuti'" else "$passoMinuti'") + "," + (if (passoSecondi < 10) "0$passoSecondi''" else "$passoSecondi''")
    }

    private fun addCoordinates(lat: Double, lng: Double) { // metodo per l'inserimento delle coordinate nella lista
        val currentLocation = Location("")
        currentLocation.latitude = lat
        currentLocation.longitude = lng

        if (track.isNotEmpty()) {
            val previousLocation = Location("")
            previousLocation.latitude = track[track.size - 1].latitude
            previousLocation.longitude = track[track.size - 1].longitude

            if (previousLocation.longitude != currentLocation.longitude && previousLocation.latitude != currentLocation.latitude) { //questa condizione mi permette di non aggiungere le geolocalizzazioni uguali, così da avere una polyline pulita
                track.add(LatLng(currentLocation.latitude, currentLocation.longitude))
                Log.d(TAG, "Acquisisco la posizione")
                if (!isRunning) {
                    chronometer.base =
                        SystemClock.elapsedRealtime() + timeWhenStop // il cronometro si avvia/riprende da dove si è fermato
                    chronometer.start() // avvio il cronometro (si baserà sul calcolo fatto in precedenza)
                    isRunning = true // isRunning lo imposto a true
                }
            } else {
                Log.d(TAG, "Non acquisisco la posizione")
                Toast.makeText(this, "Sessione in pausa", Toast.LENGTH_LONG).show()
                stopRun()
            }
        } else {
            track.add(LatLng(currentLocation.latitude, currentLocation.longitude)) // aggiunto la nuova coordinata
        }
    }

    private fun sendToFirebaseFirestore() {

        if (totalKM >= 0.10) { //se l'utente ha percorso almeno 100m, allora salvo la sessione
            //Creo una HashMap che mi servirà quando caricherò i dati della sessione appena conclusa
            val sessionMap = HashMap<String, Any>()
            sessionMap["TimeWhenStart"] = currentTime
            sessionMap["Polyline encode"] = PolyUtil.encode(track)
            sessionMap["Distanza"] = textKM.text.toString()
            sessionMap["Tempo"] = chronometer.text.toString()
            sessionMap["AndaturaAlKm"] = avaragePace.text

            /*Creo un oggetto di tipo Collection Reference che mi permette di accedere
             alla collezione Utenti -> documento (idUtente) -> collezione SessioneCorsa */
            val mFirestore = FirebaseFirestore.getInstance().collection("Utenti")
                .document(Global.utenteLoggato?.idUtente.toString()).collection("SessioniCorsa")
            //creo un nuovo documento, passandogli l'HashMap configurata
            mFirestore.document().set(sessionMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) { //se tutto va a buon fine, carico i risultati su firestore
                        Toast.makeText(
                            this,
                            getString(R.string.sendToFirebaseOk),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.sendToFirebaseFailed),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        finish()
                    }
                }
        } else { //avviso l'utente che la sessione non è stata caricata
            Toast.makeText(this, getString(R.string.messageNoConsideration), Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    override fun onDestroy() { // metodo onDestroy
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver) // annullo la registrazione del broadcastReceiver
        }
        val intent = Intent(applicationContext, GPSService::class.java)
        stopService(intent) //fermo il servizio
    }
}
