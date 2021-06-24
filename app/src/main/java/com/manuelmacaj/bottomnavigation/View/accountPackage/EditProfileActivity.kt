package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.BASE64
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.radioGroupGenderModify
import java.util.regex.Pattern

class EditProfileActivity : AppCompatActivity() {

    private val TAG = "EditProfileActivity"
    private lateinit var nameSurname: EditText
    private lateinit var emailField: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var genderRadio: RadioButton
    private lateinit var genderSelection: String
    private lateinit var passwordUtente: String

    //istanza firestore riferita alla collezione utenti. Se non esiste, la crea
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")
    private val mAuthUser = FirebaseAuth.getInstance().currentUser //istanza firebase riferita alla sezione di autenticazione

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        this.title = resources.getString(R.string.edit_account) //imposto un titolo per questa activity

        nameSurname = findViewById(R.id.editTextModifyNameSurname)
        emailField = findViewById(R.id.editTextModifyEmail)
        radioGroup = findViewById(R.id.radioGroupGenderModify)

        nameSurname.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Do Something
                    Toast.makeText(this, getString(R.string.warningNameSurname), Toast.LENGTH_LONG).show()
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        //settiamo i due editText...
        nameSurname.setText(Global.utenteLoggato?.nomeCognomeUtente) //con il nome e cognome dell'utente connesso
        emailField.setText(Global.utenteLoggato?.emailUtente) //con l'email dell'utente connesso

        if (Global.utenteLoggato?.genere == resources.getString(R.string.male)) { //se il genere dell'utente corrente è maschio...
            radioGroup.check(R.id.radioButtonMaleModify) //...seleziono il radio button per il genere maschio
            genderRadio = findViewById(R.id.radioButtonMaleModify)
        } else { //...se il genere è femmina
            radioGroup.check(R.id.radioButtonFemaleModify) //seleziono il radio button per il genere femmina
            genderRadio = findViewById(R.id.radioButtonFemaleModify)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            genderRadio = findViewById(checkedId)
        }
    }

    fun checkFields(v: View?) { //funzione per verificare se il nome e cognome, il genere e l'email dell'utente corrente sono corretti

        val nomeCognome = nameSurname.text.toString()
        val email = emailField.text.toString()

        if (nomeCognome.isEmpty() || !isValidNameSurname(nomeCognome)) { //controllo se il nome e cognome dell'utente è vuoto o non rispetta il pattern apposito
            editTextPersonNameSurname.error = resources.getString(R.string.empty_name_surname) //messaggio di errore
            return
        }

        if (radioGroupGenderModify.checkedRadioButtonId == -1) { //nessun radio button selezionato da parte dell'utente
            genderSelection = resources.getString(R.string.gender_not_selected) //messaggio di errore
            Toast.makeText(this, "" + genderSelection, Toast.LENGTH_LONG).show() //toast che mostra messaggio di errore
            return
        }

        if (!isValidEmail(email)) { //controllo se l'email è valida
            editTextModifyEmail.error = resources.getString(R.string.invalid_email) //messaggio di errore
            return
        }

        //AlertDialog per l'aggiornamento delle informazioni
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.titleInformation))
            .setMessage(getString(R.string.messaggeInformation))
            .setPositiveButton(getString(R.string.yesButton)) { _, _ -> //se l'utente clicca su si...
                updateInformation(nomeCognome, email) //...verranno aggiornate le informazioni
            }
            .setNegativeButton("No") { _, _ -> //se l'utente clicca su no...
                finish() //chiudo la finestra corrente
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun updateInformation(nomeCognome: String, email: String) { //funzione per l'aggiornamento delle informazioni dell'utente

        if (email != Global.utenteLoggato?.emailUtente) { //se le email non corrispondono...
            if (mAuthUser == null) //...e se l'istanza di firebase riferita alla sezione di autenticazione è null
                return //mi fermo
            mAuthUser.updateEmail(email) //aggiornamento email su firebase
                .addOnCompleteListener {
                    if (it.isSuccessful) { //se il cambio email è evvenuto con successo
                        Log.d(TAG, "Cambio email avvenuta con successo")
                        //aggiorniamo la collezione su firestore riferito all'id utente specifico con la nuova email
                        mFireStore.document(Global.utenteLoggato!!.idUtente).update(
                            "Email", email
                        )
                        //aggiorniamo l'informazione in locale dell'email dell'utente
                        Global.utenteLoggato?.emailUtente = email
                    } else {
                        Log.d(TAG, "Cambio email non avvenuto.")
                    }
                }
        }

        //aggiorniamo la collezione su firestore riferito all'id utente specifico con il nuovo nome e cognome e genere
        mFireStore.document(Global.utenteLoggato!!.idUtente).update(
            "Nome e Cognome", nomeCognome,
            "Genere", genderRadio.text,
        )
        //aggiorniamo le informazioni in locale del nome e cognome e genere dell'utente
        Global.utenteLoggato?.nomeCognomeUtente = nomeCognome
        Global.utenteLoggato?.genere = genderRadio.text.toString()
        finish()
    }

    private fun isValidEmail(email: String): Boolean { //funzione prende in ingresso una email e mi restituisce un risultato booleano
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" //regular expression che bisogna rispettare per l'email
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email) //metto a confronto l'email dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }

    private fun isValidNameSurname(nameSurname: String): Boolean { //funzione prende in ingresso un nome e un cognome e mi restituisce un risultato booleano
        val NAMESURNAME_PATTERN = "^([a-zA-Z]{2,}\\s[a-zA-Z]+'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]+)?)" //regular expression che bisogna rispettare per il nome e il cognome
        val pattern = Pattern.compile(NAMESURNAME_PATTERN)
        val matcher = pattern.matcher(nameSurname) //metto a confronto il nome e cognome dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }
}