package com.manuelmacaj.bottomnavigation.View.activitiesPackage

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
import com.manuelmacaj.bottomnavigation.Model.Corsa
import com.manuelmacaj.bottomnavigation.R
import java.util.*
import kotlin.collections.ArrayList

class ActivitiesFragment : Fragment() {

    val mRunSessionFirestore = FirebaseFirestore.getInstance()
    private val collezioneUtenti = "Utenti"
    private val collezioneSessioneCorsa = "SessioniCorsa"
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        requireActivity().title = getString(R.string.activity)
        listView = view.findViewById(R.id.activitiesList) as ListView
        // creo l'istanza di firestore;
        //Collezione Utenti -> Documento utente -> Collezione SessioneCorsa


        return view
    }

    override fun onResume() {
        super.onResume()
        mRunSessionFirestore
            .collection(collezioneUtenti)
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection(collezioneSessioneCorsa)
            .get()
            .addOnSuccessListener { result ->
                if(result.isEmpty) {
                    //gestiamo il fatto che l'utente non presenta una collezione di sessioni di corsa
                    Toast.makeText(requireContext(), "Non hai nessuna attività, inizia a correre", Toast.LENGTH_LONG)
                        .show()
                }
                else{
                    val listaSessioniCorsa: MutableList<Corsa> = ArrayList()
                    for (document in result) {
                        //Log.d("TAG", "${document.id} => ${document.data}")
                        val corsa = Corsa(
                            document.getString("Polyline encode").toString(),
                            document.getString("Tempo").toString(),
                            document.getString("Distanza").toString(),
                            document.getDate("TimeWhenStart").toString(),
                            document.getString("AndaturaAlKm").toString()
                        )
                        listaSessioniCorsa.add(corsa)
                    }
                    Toast.makeText(requireContext(), "Hai delle attività. Attualemnte non sono visibili (ci stiamo lavorando)", Toast.LENGTH_LONG)
                        .show()

                    listaSessioniCorsa.forEach {
                        Log.d("->", it.toString())
                    }

                    /*val listViewAdapter: ArrayAdapter<Corsa> = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, listaSessioniCorsa
                    )
                    listView.adapter = listViewAdapter*/
                }

            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error getting documents: ", exception)
            }
    }
}

