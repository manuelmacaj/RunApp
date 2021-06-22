package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import java.time.LocalDate

class PersonalAccountFragment : Fragment() {

    private val TAG = "PersonalAccountFragment"

    private lateinit var textFullName: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var textEmail: TextView
    private lateinit var textAge: TextView
    private lateinit var textGender: TextView
    private lateinit var textDateofBirth: TextView
    private lateinit var dateOfBirth: LocalDate
    private var currentTime: LocalDate = LocalDate.now()
    private val GALLERY_PERMISSION_REQUEST_CODE = 1
    private val galleryPhotoCode = 1

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

        val arrayDate: List<String> = Global.utenteLoggato?.dataNascita?.split("-")!!

        dateOfBirth = LocalDate.of(arrayDate[0].toInt(), arrayDate[1].toInt(), arrayDate[2].toInt())

        if ((currentTime.month <= dateOfBirth.month) && (currentTime.dayOfMonth < dateOfBirth.dayOfMonth))
            textAge.text = ((currentTime.year - dateOfBirth.year) - 1).toString()
        else
            textAge.text = (currentTime.year - dateOfBirth.year).toString()

        val age = resources.getString(R.string.user_years)
        textAge.append(" $age")

        return view
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onStart() {
        super.onStart()
  /*     if (Global.utenteLoggato?.genere == "Maschio") { //controllo se l'utente connesso all'app è di sesso maschile
            imageProfile.setImageDrawable(resources.getDrawable(R.drawable.male_profile_picture)) //carico un'immagine generica per utente maschio
        } else imageProfile.setImageDrawable(resources.getDrawable(R.drawable.female_profile_picture)) //carico un'immagine generica per l'utente femmina
*/
    }

    override fun onResume() {
        super.onResume()
        //aggiorno i dati dell'utente che sono stati modificati
        textFullName.text = Global.utenteLoggato?.nomeCognomeUtente
        textEmail.text = Global.utenteLoggato?.emailUtente
        textGender.text = Global.utenteLoggato?.genere
        textDateofBirth.text = Global.utenteLoggato?.dataNascita

        //impostiamo un click listener per l'image view
        imageProfile.setOnClickListener {
            //permesso per accedere alla galleria
            Log.d(TAG, "onClick immagine del profilo")
            checkGalleryPermission()
        }
    }

    private fun checkGalleryPermission() { // funzione di  verifica dei permessi di accesso alla galleria (bisogna dichirare nel manifest)

        //Se l'utente non ha mai dato il consenso per accedere alla galleria o è la prima volta che accede all'app, allora verrà richiesto fornire il consenso
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

    private fun takePictureFromGallery() {

        AlertDialog.Builder(requireActivity())
            .setTitle("Funzione ancora in beta")
            .setMessage("E' possibile inserire un'immagine dalla galleria, ma non verrà salvata.\nNel prossimo aggiornamanto verrà migliorato")
            .setPositiveButton("Ok") {_, _ ->
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, 1)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            galleryPhotoCode -> {
                if (resultCode == RESULT_OK) {
                    val selectedImage: Uri? = data?.data
                    imageProfile.setImageURI(selectedImage)
                    imageProfile.adjustViewBounds = true
                }
            }
        }
    }
}