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


class GPSService: Service() {

    private lateinit var listener: LocationListener
    private var locationManager: LocationManager? = null
    private val TAG = "GPSService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_HIGH
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setNotificationSilent()
                .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
                .setContentTitle(getString(R.string.notificationTitle))
                .setContentText(getString(R.string.notificationMessage))
                .build()

            startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand GPSService")
        getGPSLocation()
        return START_NOT_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun getGPSLocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Stop GPSService")
            stopSelf()
            return
        }

        Log.d(TAG, "Get location")

        listener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                val i = Intent("location_update")
                i.putExtra("Lat", location.latitude)
                i.putExtra("Lng", location.longitude)
                sendBroadcast(i)
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0f, listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(listener)
    }
}