package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.manuelmacaj.bottomnavigation.Model.Corsa
import com.manuelmacaj.bottomnavigation.R
import java.util.*
import kotlin.collections.ArrayList

class ActivitiesFragment : Fragment() {

    private val collezioneUtenti = "Utenti"
    private val collezioneSessioneCorsa = "SessioniCorsa"
    private lateinit var listView: ListView
    private val listaSessioniCorsa: MutableList<Corsa> = ArrayList()
    private lateinit var myAdapterActivities : AdapterActivities
    private val mRunSessionFirestore = FirebaseFirestore.getInstance()
        .collection(collezioneUtenti) //accedo alla collezione "Utenti"
        .document(FirebaseAuth.getInstance().currentUser!!.uid) //accedo al documento riferito all'utente attualmente connesso
        .collection(collezioneSessioneCorsa) //accedo alla collezione "SessioniCorsa"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)
        requireActivity().title = getString(R.string.activity)
        listView = view.findViewById(R.id.activitiesList) as ListView
        return view
    }

    override fun onStart() {
        super.onStart()
        mRunSessionFirestore
            .orderBy("TimeWhenStart", Query.Direction.DESCENDING) //applico un orderBy cosi mostro le sessioni dalla più recente fino alla più remota
            .get()
            .addOnSuccessListener { result ->
                if(result.isEmpty) {
                    //gestiamo il fatto che l'utente non presenta una collezione di sessioni di corsa
                    Toast.makeText(requireContext(), "Non hai nessuna attività, inizia a correre", Toast.LENGTH_LONG)
                        .show()
                }
                else{ //se invece l'utente presenta una collezione di sessioni di corsa

                    for (document in result) {
                        Log.d("TAG", "${document.id} => ${document.data}")
                        val corsa = Corsa(
                            document.getString("Polyline encode").toString(),
                            document.getString("Tempo").toString(),
                            document.getString("Distanza").toString(),
                            document.getDate("TimeWhenStart").toString(),
                            document.getString("AndaturaAlKm").toString()
                        )
                        listaSessioniCorsa.add(corsa) //aggiungiamo all lista tutte le informazioni riguardanti la sessione di corsa
                    }
                    myAdapterActivities = AdapterActivities(requireContext(), listaSessioniCorsa)

                    listView.adapter = myAdapterActivities
                }

            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }

    override fun onResume() {
        super.onResume()
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(requireActivity(), DetailRunSessionActivity::class.java)
            intent.putExtra("polyline", listaSessioniCorsa[position].polylineString)
            intent.putExtra("date", listaSessioniCorsa[position].DataOrarioPartenza)
            intent.putExtra("time", listaSessioniCorsa[position].tempo)
            intent.putExtra("distance", listaSessioniCorsa[position].km)
            intent.putExtra("averagePale", listaSessioniCorsa[position].andaturaMedia)
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        listaSessioniCorsa.clear() //cancellazione di tutti gli elementi nella lista
        myAdapterActivities.notifyDataSetChanged() //notifico all'adapter che deve gestire l'aggiornamento della lista perchè sono stati cancellati i dati
    }
}


