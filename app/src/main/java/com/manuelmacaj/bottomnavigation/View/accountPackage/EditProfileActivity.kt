package com.manuelmacaj.bottomnavigation.View.accountPackage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.radioGroupGenderModify
import java.util.regex.Pattern

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameSurname: EditText
    private lateinit var emailField: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var genderRadio: RadioButton
    private lateinit var genderSelection: String

    //istanza firestore riferita alla collezione utenti. Se non esiste, la crea
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")
    private val mAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nameSurname = findViewById(R.id.editTextModifyNameSurname)
        emailField = findViewById(R.id.editTextModifyEmail)
        radioGroup = findViewById(R.id.radioGroupGenderModify)

        nameSurname.setText(Global.utenteLoggato?.nomeCognomeUtente)
        emailField.setText(Global.utenteLoggato?.emailUtente)

        if (Global.utenteLoggato?.genere == resources.getString(R.string.male)) {
            radioGroup.check(R.id.radioButtonMaleModify) //seleziono il radio button per il genere maschio
            genderRadio = findViewById(R.id.radioButtonMaleModify)
        } else {
            radioGroup.check(R.id.radioButtonFemaleModify) //seleziono il radio button per il genere femmina
            genderRadio = findViewById(R.id.radioButtonFemaleModify)
        }
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            genderRadio = findViewById(checkedId)
        }
    }

    fun checkFields(v: View?) { //deve capire se quello inserito in username e password è valido o meno e nel caso segnalare

        val nomeCognome = nameSurname.text.toString()
        val email = emailField.text.toString()

        if (nomeCognome.isEmpty()) {
            //settiamo un errore se il campo in cui inserire nome e cognome è vuoto
            editTextPersonNameSurname.error = resources.getString(R.string.empty_name_surname)
            return
        }

        if (radioGroupGenderModify.checkedRadioButtonId == -1) { //nessun radio button selezionato da parte dell'utente
            //genderSelection.error = resources.getString(R.string.gender_not_selected)
            genderSelection = resources.getString(R.string.gender_not_selected)
            Toast.makeText(this, "" + genderSelection, Toast.LENGTH_LONG).show()
            return
        }

        if (!isValidEmail(email)) {
            //settiamo un errore se l'email inserita dall'utente non è valida
            editTextModifyEmail.error = resources.getString(R.string.invalid_email)
            return
        }

        updateInformation(nomeCognome)

    }

    private fun updateInformation(nomeCognome: String) {

        //val userAuth = mAuth.currentUser //accesso account utente corrente

        //val credential = EmailAuthProvider.getCredential(email)

        //userAuth?.updateEmail(email)

        mFireStore.document(Global.utenteLoggato!!.idUtente).update(
            "Nome e Cognome", nomeCognome,
            "Genere", genderRadio.text
        )

        Global.utenteLoggato?.nomeCognomeUtente = nomeCognome
        Global.utenteLoggato?.genere = genderRadio.text.toString()
        finish()
    }

    // validating email id
    private fun isValidEmail(email: String): Boolean {
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches() //se indirizzo email è soddisfatto, tutto bene sennò...
    }
}