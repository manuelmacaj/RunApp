package com.manuelmacaj.bottomnavigation.Service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.manuelmacaj.bottomnavigation.R


class GPSService : Service() {

    private lateinit var listener: LocationListener
    private var locationManager: LocationManager? = null
    private val TAG = "GPSService"

    override fun onBind(intent: Intent?): IBinder? { //servizio di tipo unbind
        return null
    }

    override fun onCreate() {
        super.onCreate()

        //Passaggi per la creazione di una notifica
        val CHANNEL_ID = "my_channel_01"
        val channel = NotificationChannel(
            CHANNEL_ID,
            "My Channel",
            NotificationManager.IMPORTANCE_HIGH
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentTitle(getString(R.string.notificationTitle))
            .setContentText(getString(R.string.notificationMessage))
            .build()

        startForeground(1, notification) // avvio la notifica (simile a startService)
        /* questo mi permette di monitorare delle coordinate in background (il servizio è
        in foreground grazie alla notifica che compare nella barra delle notifiche). Questa azione è
        possibile grazie al startServiceForeground specificato in RunSessionActivity.kt
        */
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand GPSService")
        getGPSLocation()
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun getGPSLocation() {
        Log.d(TAG, "Get location")

        // il listener gestisce gli aggiornamenti della posizione
        listener = object : LocationListener {

            override fun onLocationChanged(location: Location) { // viene invocato quando il listener nota un cambiamento di posizione
                val i = Intent("location_update") // creo un intent
                i.putExtra("Lat", location.latitude) // inserisco le informazioni della latidudine
                i.putExtra("Lng", location.longitude) // inserisco le informazioni della longitudine
                sendBroadcast(i) // invio l'intent in modalità broadcast. Un broadcast receiver gestirà queste informazioni
            }

            override fun onProviderEnabled(provider: String) { // viene invocato quando l'utente abilita il provider, ma non viene gestito perché il GPS deve essere attivo
            }

            override fun onProviderDisabled(provider: String) { // viene invocato qualora l'utente disabilitasse la localizzazione
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }
        locationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //il listener viene registrato all'interno del location manager che specifica il tipo
        // di provider (in questo caso GPS_PROVIDER), il tempo minimo di aggiornamento della
        // posizione (ogni 2 secondi) e la posizone minima di aggiornamento (espresso in metri)
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, listener)

    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(listener) //rimuovo gli aggiormamenti perché ho finito di usare il servizio
        stopSelf() // fermo il servizio, qualora l'utente chiudesse l'app durante la sessione
    }
}