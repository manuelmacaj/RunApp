package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import java.time.LocalDate
import java.time.Period


class PersonalAccountFragment : Fragment() {

    private val TAG = "PersonalAccountFragment"
    private lateinit var textFullName: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var textEmail: TextView
    private lateinit var textAge: TextView
    private lateinit var textGender: TextView
    private lateinit var textDateofBirth: TextView

    private lateinit var dateOfBirth: LocalDate
    private var currentDate: LocalDate = LocalDate.now()
    private val GALLERY_PERMISSION_REQUEST_CODE = 1
    private val galleryPhotoCode = 1
    private var firebaseStorage = FirebaseStorage.getInstance()
    private val collezioneUtenti = "Utenti"
    private var context = null


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_account, container, false)

        requireActivity().title = getString(R.string.account)

        textFullName = view.findViewById(R.id.fullName)
        imageProfile = view.findViewById(R.id.profilePhoto)
        textEmail = view.findViewById(R.id.email)
        textAge = view.findViewById(R.id.age)
        textGender = view.findViewById(R.id.userGender)
        textDateofBirth = view.findViewById(R.id.userBirthday)

        calculateAge()

        return view
    }

    private fun calculateAge() {
        val arrayDate: List<String> = Global.utenteLoggato?.dataNascita?.split("-")!!

        dateOfBirth = LocalDate.of(arrayDate[0].toInt(), arrayDate[1].toInt(), arrayDate[2].toInt())

        val agePeriod = Period.between(dateOfBirth, currentDate)
        val ageText = resources.getString(R.string.user_years)
        textAge.text = "${agePeriod.years} $ageText"

    }

    override fun onResume() {
        super.onResume()
        //aggiorno i dati dell'utente che sono stati modificati
        textFullName.text = Global.utenteLoggato?.nomeCognomeUtente
        textEmail.text = Global.utenteLoggato?.emailUtente
        textGender.text = Global.utenteLoggato?.genere
        textDateofBirth.text = Global.utenteLoggato?.dataNascita

        val storageRef =
            firebaseStorage.reference //creiamo un nuovo riferimento della sezione storage
        val getImageReference =
            storageRef.child(Global.utenteLoggato?.pathImageProfile.toString()) //percorso dell'immagine

        getImageReference //proviamo il download dell'URL che abbiamo passato
            .downloadUrl
            .addOnSuccessListener {
               //fonte: https://www.html.it/pag/71017/glide-e-il-caricamento-delle-immagini/
                Glide.with(requireContext()).load(it).into(imageProfile)
            }
            .addOnFailureListener {
                Log.w(TAG, "Immagine non scaricata", it.cause)
            }

        //impostiamo un click listener per l'image view
        imageProfile.setOnClickListener {
            //permesso per accedere alla galleria
            Log.d(TAG, "onClick immagine del profilo")
            checkGalleryPermission()
        }
    }

    private fun checkGalleryPermission() { //funzione di  verifica dei permessi di accesso alla galleria (bisogna dichirare nel manifest)

        //Se l'utente non ha mai dato il consenso per accedere alla galleria o è la prima volta che accede all'app, allora verrà richiesto di fornire il consenso
        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_REQUEST_CODE
            ) //invierò il risultato alla funzione override fun onRequestPermissionsResult, per comprendere se l'app ha l'autorizzazione o no
        } else { // se il permesso di accesso alla galleria è attivo, allora...
            requestPermissions(
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) { //switch per verificare il tipo di request code restituito
            GALLERY_PERMISSION_REQUEST_CODE -> { //se il request code corrisponde al code dedicato al prelevamento delle foto dalla galleria,
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //se l'utente mi ha fornito l'autorizzazione...
                    takePictureFromGallery()
                } else //se ha rifiutato allora il permesso per accedere alla fotocamera non è stato abilitato
                    Log.d(TAG, "Accesso alla galleria negato")
                return
            }
        }
    }

    private fun takePictureFromGallery() { //funzione per prelevare foto dalla galleria
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickPhoto, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            galleryPhotoCode -> {
                if (resultCode == RESULT_OK) { //se tutto va bene
                    //preleviamo immagine dalla galleria
                    val selectedImage: Uri? = data?.data
                    val storageRef = firebaseStorage.reference
                    val image =
                        storageRef.child("userProfile/${Global.utenteLoggato?.idUtente}.png") //percorso in cui caricare l'immagine profilo utente

                    //facciamo l'upload dell'immagine
                    val uploadTask = image.putFile(selectedImage!!)
                    uploadTask
                        .addOnSuccessListener { task ->
                            Log.d(
                                TAG, "Immagine caricata in " + image.path + "Dimensione " +
                                        task.metadata?.sizeBytes
                            )
                            Global.utenteLoggato?.pathImageProfile = image.path
                            //aggiornare firestore
                            val mFirestore = FirebaseFirestore.getInstance()
                            mFirestore.collection(collezioneUtenti) //accedo alla collezione utenti
                                .document(Global.utenteLoggato?.idUtente!!) //accedo all'id dell'utente connesso
                                .update(
                                    "URIImage",
                                    Global.utenteLoggato?.pathImageProfile
                                ) //aggiorno il campo relativo all'URI dell'immagine
                            Glide.with(requireContext()).load(selectedImage).into(imageProfile)
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "Immagine non caricata", exception.cause)
                        }
                }
            }
        }
    }
}