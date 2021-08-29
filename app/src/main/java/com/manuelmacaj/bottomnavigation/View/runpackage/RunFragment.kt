package com.manuelmacaj.bottomnavigation.View.runpackage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private var locationPermission: Boolean? = null
    private var GPScheck = false
    private var GPSPopUpEnable = false;

    private lateinit var listener: LocationListener
    private var locationManager: LocationManager? = null

    private lateinit var manager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 54 // codice indetificativo per la richiesta della geolocalizzazione

    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(mContext == null) {
            mContext = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inizializzo il fragment della corsa

        requireActivity().title = getString(R.string.run) //imposto il titolo che verrà visualizzato nella toolbar

        val view = inflater.inflate(R.layout.fragment_run, container, false)

        mapView = view.findViewById(R.id.mapRun) as MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // azione assolutamente necessaria nel momento in cui si include un oggetto di tipo MapView

        // on click listener sul bottone
        view.startRunButton.setOnClickListener {
            if (locationPermission == true && GPScheck) { // se ho il permesso e se il GPS è attivo
                val intent = Intent(activity, RunSessionActivity::class.java) // Posso generare un
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } else {
                AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.titleNoRun))
                    .setMessage(getString(R.string.messageNoRun))
                    .setPositiveButton(getString(R.string.setting)) { _, _ ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
        // creo l'istanza per poter poi utilizzare FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return view
    }

    override fun onStart() {
        super.onStart()
        checkGPSIsEnable()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        checkPermissions() // chiamata del metodo checkPermission
    }

    override fun onPause() {
        super.onPause()
        val mgr = MapStateManager(mContext!!)
        mgr.saveMapState(map)
        mapView.onPause()
        //Toast.makeText(mContext, "Map State has been save?", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) { //Metodo implementato da OnMapReadyCallback, per la gestione della mappa di GoogleMaps
        map = googleMap
        map.uiSettings.setAllGesturesEnabled(false) //Tutte le gesture possibili sulla mappa sono disabilitate
        map.uiSettings.isMyLocationButtonEnabled = false // disabilito il location button.
        map.isBuildingsEnabled = false

        val mgr = MapStateManager(mContext!!)
        val position = mgr.savedCameraPosition
        if (position != null) {
            val update = CameraUpdateFactory.newCameraPosition(position)
            //Toast.makeText(mContext, "entering Resume State", Toast.LENGTH_SHORT).show()
            map.moveCamera(update)
            map.mapType = mgr.savedMapType
        }
    }

    private fun checkGPSIsEnable() { //funzione di check
        manager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // se il GPS non è abilitato
            buildAlertMessageNoGps() // chiamo il metodo buildAlertMessageNoGps()
            GPScheck = false // imposto a false la variabile booleana GPScheck
        } else{  // se il gps è attivo
            GPScheck = true // imposto a true la variabile booleana GPScheck
            GPSPopUpEnable = false
        }
    }

    private fun buildAlertMessageNoGps() { // metodo con all'interno l'alert dialog che avvisa che il GPS non è abilitato
        AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.titleGPSNotEnable))
            .setMessage(getString(R.string.messageGPSNotEnable))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yesButton)) { dialog, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) //il bottone positivo, spedisce l'utente nelle impostazioni dedicata alla localizzazione
                dialog.cancel()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel() // qualora l'utente non volesse andare nelle impostazioni, l'AlertDialog viene chiuso
            }
            .create()
            .show()
        GPSPopUpEnable = true
    }

    private fun checkPermissions() { // funzione di  verifica dei permessi di accesso alla posizione (ovviamente, bisogna dichirare nel manifest)
        
        //Se l'utente non ha mai dato il consenso alla localizzazione o è la prima volta che accede all'app, allora verrà richiesto fornire il consenso alla posizione
        if (ContextCompat.checkSelfPermission(
                mContext!!, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) { // se l'utente non ha dato il permesso, mostro un AlertDialog
            //L'alert dialog informerà che dovrà fornire il consenso alla localizzazione
            val alertMessage = AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.titleRequestPermission))
                .setMessage(getString(R.string.messageRequestPermission))
                .setPositiveButton(
                    "Ok") { _, _ ->
                    //dopo aver premuto il tasto ok, viene generato l'Alert dialog dedicato ai permessi
                    requestPermissions(
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    ) //invierò il risultato alla funzione override fun onRequestPermissionsResult, per comprendere se l'app ha l'autorizzazione o no
                }
                .setCancelable(false)
                .create()
                .show()
        } else { // se il permesso di localizzazione è attivo, allora avvio la localizzazione
            locationPermission = true
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) { // controllo permessi
        locationPermission = false 

        when(requestCode) { //switch che verifica il tipo di request code restituito
            LOCATION_PERMISSION_REQUEST_CODE -> { //se il request code corrisponde al code dedicato alla geolocalizzazione, allora verifico
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // se l'utente mi ha fornito l'autorizzazione...
                    Log.d("", "Localizzazione abilitata") //posso localizzarlo
                    mapView.invalidate()
                    locationPermission = true // variabile booleana settata a true
                    getLocation() //... avvio la localizzazione dell'utente

                } else { //se ha rifiutato allora la localizzazione non è abilitata
                    Log.d("", "Localizzazione disabilitata")
                    Toast.makeText(
                        mContext,
                        getString(R.string.localizationDisable),
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission") // mi permette di non considerare i warning riguardanti ai permessi.
    private fun getLocation() {

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) { // viene invocato quando il listener nota un cambiamento di posizione
                map.isMyLocationEnabled = true
                lastLocation = location //last location assumerà l'ultima posizione recente
                val currentLaLng = LatLng(lastLocation.latitude, lastLocation.longitude) // l'oggetto di tipo LatLng avrà come valori di latitudine e longitudine i valori presenti in lastLocation
                Log.d(TAG, "lat: ${currentLaLng.latitude} log: ${currentLaLng.longitude} ")
                val cameraPositionUser = CameraPosition.builder() // mi costruisco la "Telecamera" che segue il "tondino blu"
                    .target(currentLaLng) // indico dove si deve posizionare la camera
                    .zoom(16f) // indico lo zoom della telecamera (minimo 1, massimo 20. consigliato tra il 15 e il 20 se vogliamo monitorare il posizionamento dell'utente)
                    .build() // costruisco la variabile CameraPosition
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPositionUser)) // eseguo la funzione di animazione
            }

            override fun onProviderEnabled(provider: String) {
                GPScheck = true
            }

            override fun onProviderDisabled(provider: String) {
                map.isMyLocationEnabled = false
                if (!GPSPopUpEnable) {
                    checkGPSIsEnable()
                }
            }
        }
        locationManager = mContext?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //il listener viene registrato all'interno del location manager che specifica il tipo di provider (in questo caso GPS_PROVIDER), il tempo minimo di aggiornamento della posizione (ogni 2 secondi) e la posizone minima di aggiornamento (espresso in metri)
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, listener)
    }
}