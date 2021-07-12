package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.manuelmacaj.bottomnavigation.Model.Corsa
import com.manuelmacaj.bottomnavigation.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivitiesFragment : Fragment() {

    private val TAG = "ActivitiesFragment"

    private val collezioneUtenti = "Utenti"
    private val collezioneSessioneCorsa = "SessioniCorsa"
    private lateinit var listView: ListView
    private val listaSessioniCorsa: MutableList<Corsa> = ArrayList()
    private lateinit var myAdapterActivities : AdapterActivities
    private val mRunSessionFirestore = FirebaseFirestore.getInstance()
        .collection(collezioneUtenti) //accedo alla collezione "Utenti"
        .document(FirebaseAuth.getInstance().currentUser!!.uid) //accedo al documento riferito all'id dell'utente attualmente connesso
        .collection(collezioneSessioneCorsa) //accedo alla collezione "SessioniCorsa"
    private var mContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(mContext == null) {
            mContext = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)
        requireActivity().title = getString(R.string.activity)
        listView = view.findViewById(R.id.activitiesList) as ListView
        return view
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        mRunSessionFirestore
            .orderBy("TimeWhenStart", Query.Direction.DESCENDING) //applico un orderBy cosi mostro le sessioni dalla più recente fino alla più remota
            .get()
            .addOnSuccessListener { result ->
                if(result.isEmpty) { //gestiamo il fatto che l'utente non presenta una collezione di sessioni di corsa
                    //Toast che avvisa la mancanza di sessioni di corsa
                    Toast.makeText(mContext, getString(R.string.noActivity), Toast.LENGTH_LONG)
                        .show()
                }
                else{ //se invece l'utente presenta una collezione di sessioni di corsa

                    for (document in result) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        val date: Date? = document.getDate("TimeWhenStart")
                        val format = SimpleDateFormat("yyyy/MM/dd HH:mm")

                        val corsa = Corsa(
                            document.getString("Polyline encode").toString(),
                            document.getString("Tempo").toString(),
                            document.getString("Distanza").toString(),
                            format.format(date!!),
                            document.getString("AndaturaAlKm").toString()
                        )
                        listaSessioniCorsa.add(corsa) // Aggiungo il documento letto nella lista
                    }

                    myAdapterActivities = AdapterActivities(mContext!!, listaSessioniCorsa) // creo l'oggetto per customizzare l'adapter
                    listView.adapter = myAdapterActivities //impostiamo l'adapter per questa listview
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()

        listView.setOnItemClickListener { _, _, position, _ -> // setOnItemClickListener mi permette di capire quale elemento della listView è stato selezionato
            val intent = Intent(requireActivity(), DetailRunSessionActivity::class.java) // genero un intent per l'apertura di un activity
            intent.putExtra("polyline", listaSessioniCorsa[position].polylineString) //incapsulo le informazioni nell'intent
            intent.putExtra("date", listaSessioniCorsa[position].DataOrarioPartenza)
            intent.putExtra("time", listaSessioniCorsa[position].tempo)
            intent.putExtra("distance", listaSessioniCorsa[position].km)
            intent.putExtra("averagePace", listaSessioniCorsa[position].andaturaMedia)
            startActivity(intent) // avvio l'activity
        }
    }

    override fun onStop() {
        super.onStop()
        if(listaSessioniCorsa.isNotEmpty()) { // se l'utente presenta delle sessioni di corsa
            listaSessioniCorsa.clear() // cancellazione di tutti gli elementi nella lista
            myAdapterActivities.notifyDataSetChanged() // notifico all'adapter che deve gestire l'aggiornamento della lista, perchè sono stati cancellati i dati al suo interno
        }
    }
}


