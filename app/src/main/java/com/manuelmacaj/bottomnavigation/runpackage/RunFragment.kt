package com.manuelmacaj.bottomnavigation.runpackage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.fragment_run.view.*


class RunFragment : Fragment(), OnMapReadyCallback {

    private val TAG = "RunFragment"
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient //nota: è necessario il Google Play Services installato sul proprio dispositivo (reale o virtuale), per poter utilizzare FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var locationRequest: LocationRequest? = null // l'oggetto LocationRequest permette di migliorare il servizio di localizzazione dell'utente
    private var locationCallback: LocationCallback? = null // oggetto che notifica il possibile cambiamento di posizione
    private var LocationPermission: Boolean? = null
    private var GPScheck = false

    private var manager: LocationManager? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 54 // codice indetificativo per la richiesta della geolocalizzazione

/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GPScheck = checkGPSLocation()
    }*/


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Inizializzo il fragment della corsa

        requireActivity().title = getString(R.string.run) //imposto il titolo che verrà visualizzato nella toolbar

        val view = inflater.inflate(R.layout.fragment_run, container, false)

        mapView = view.findViewById(R.id.mapRun) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // on click listener sul bottone
        view.startRunButton.setOnClickListener {
            if(LocationPermission == true) {
                val intent = Intent(activity, RunSessionActivity::class.java)
                startActivity(intent)
            }
            else{
                val alertMessage = AlertDialog.Builder(activity!!)
                    .setTitle(getString(R.string.titleNoRun))
                    .setMessage(getString(R.string.messageNoRun))
                    .setPositiveButton(getString(R.string.setting), object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    })
                    .create()
                    .show()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!) // creo l'istanza per poter poi utilizzare le localizzazioni

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.setAllGesturesEnabled(false)
        map.uiSettings.isMyLocationButtonEnabled = false
        map.isBuildingsEnabled = false
        checkPermissions()
    }

 /*   private fun checkGPSLocation(): Boolean {
        if (manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
            return true
        }
        return false
    }*/


    private fun checkPermissions() { // funzione di  verifica dei permessi di accesso alla posizione (ovviamente, bisogna dichirare nel manifest)

        //Se l'utente non ha mai dato il consenso alla localizzazione o è la prima volta che accede all'app, allora verrà richiesto fornire il consenso alla posizione
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) { // se l'utente non ha dato il permesso, mostro il popUp in cui chiedo il consenso
            //Alert dialog che permette di informare l'utente il motivo della richiesta di autorizzazione

            val alertMessage = AlertDialog.Builder(activity!!)
                .setTitle(getString(R.string.titleRequestPermission))
                .setMessage(getString(R.string.messageRequestPermission))
                .setPositiveButton("Ok", object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        //dopo aver premuto il tasto ok, viene generato l'Alert dialog dedicato ai permessi
                        requestPermissions(
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            LOCATION_PERMISSION_REQUEST_CODE) //invierò il risultato alla funzione override fun onRequestPermissionsResult, per comprendere se l'app ha l'autorizzazione o no

                    }
                })
                .create()
                .show()
        }

        else { // se il permesso di localizzazione è attivo, allora avvio la localizzazione
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) { // metodo che permette di verificare i permessi che l'utente ha fornito
        LocationPermission = false //inizializzo la variabile booleana a false

        when(requestCode) { //switch che verifica il tipo di request code restituito
            LOCATION_PERMISSION_REQUEST_CODE -> { //se il request code corrisponde al code dedicato alla geolarizzazione, allora verifico
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // se l'utente mi ha fornito l'autorizzazione...
                    Log.d("", "Localizzazione abilitata")
                    mapView.invalidate()
                    LocationPermission = true // variabile booleana settata a true
                    getLocation() //... avvio la localizzazione dell'utente

                } else { //se ha rifiutato allora la localizzazione non è abilitata
                    Log.d("", "Localizzazione disabilitata")
                    Toast.makeText(requireContext(), "La localizzazione è disabilitata", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        //mostra un punto blu sulla mappa che indica la mia posizione corrente
        map.isMyLocationEnabled = true
        //fusedLocationClient.lastLocation.addOnSuccessListener(this) ci restituisce la posizione più recente dell'utente
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location == null) { // se il risultato è null, allora la localizzazione risulta impossibile
                Log.d(TAG, "Impossibile localizzare l'utente")
            }

            else { // se invece location è diverso da null, allora
                lastLocation = location //last location assumerà l'ultima posizione fornita
                val currentLaLng = LatLng(lastLocation.latitude, lastLocation.longitude) //la nuova variabile assumerà la latitudine e longitudine
                Log.d(TAG, "lat: ${currentLaLng.latitude} log: ${currentLaLng.longitude} ")
                val cameraPositionUser = CameraPosition.builder() // mi costruisco la "Telecamera" che segue il "tondino blu"
                    .target(currentLaLng) // indico dove si deve posizionare la visualizzazione sulla mappa
                    .zoom(16f) // indico lo zoom della telecamera (minimo 1, massimo 20. consigliato tra il 15 e il 20 se vogliamo monitorare il posizionamento dell'utente)
                    .build() // costruisco la variabile CameraPosition
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPositionUser)) // eseguo la funzione di animazione
            }
        }

        //--------
        /* Queste righe di codice mi permettono di verificare
        in tempo reale il posizionamento dell'utemte.
        Senza questa sezione di codice, non potremmo seguire
        l'utente sulla mappa, ma la localizzazione funzionerebbe lo stesso */

        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
            locationRequest?.let { locationRequest ->
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                locationRequest.interval = 5000
                locationRequest.fastestInterval = 1000

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        getLocation() //richiamo la funzione di localizzazione
                    }
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

}

/*map.addPolyline(
  PolylineOptions()
      .addAll(PolyUtil.decode(stringPolyline))
      .width(10f)
      .color(Color.CYAN)
)*/