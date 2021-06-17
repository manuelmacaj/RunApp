package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.BASE64
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_register.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {

    private var TAG = ".RegisterActivity"
    private lateinit var nameSurname: EditText
    private lateinit var emailField: EditText
    private lateinit var firstPasswordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var mDisplayDate: TextView
    private lateinit var mDateSetListener: OnDateSetListener
    private lateinit var genderRadio: RadioButton
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var dateOfBirth: LocalDate
    private lateinit var genderSelection : String

    private val BASE64: BASE64 = BASE64()

    private val mRegister =
        FirebaseAuth.getInstance() //istanza firebase riferita alla sezione di autenticazione

    //istanza firestore riferita alla collezione utenti. Se non esiste, la crea
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        nameSurname = findViewById(R.id.editTextPersonNameSurname)
        emailField = findViewById(R.id.editTextEmailRegister)
        firstPasswordField = findViewById(R.id.editTextPasswordRegister)
        confirmPasswordField = findViewById(R.id.editTextConfirmPassword)
        radioGroupGender = findViewById(R.id.radioGroupGenderModify)

        radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            genderRadio = findViewById(checkedId)
        }

        mDisplayDate = findViewById(R.id.textViewDate)
        mDisplayDate.setOnClickListener {
            val cal = Calendar.getInstance()
            val year = cal[Calendar.YEAR]
            val month = cal[Calendar.MONTH]
            val day = cal[Calendar.DAY_OF_MONTH]

            val dialog = DatePickerDialog(
                this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                year, month, day
            )
            dialog.datePicker.maxDate =
                System.currentTimeMillis() //il datapicker mostrerà le date fino al giorno corremte
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
        mDateSetListener =
            OnDateSetListener { datePicker, year, month, day ->
                var mese = month
                mese = month + 1
                Log.d(TAG, "onDateSet: mm/dd/yyy: $mese/$day/$year")
                dateOfBirth = LocalDate.of(year, month, day)
                mDisplayDate.text = dateOfBirth.toString()
            }
    }

    fun checkRegister(v: View?) { //deve capire se quello inserito in username e password è valido o meno e nel caso segnalare

        val nomeCognome = nameSurname.text.toString()
        val email = emailField.text.toString()
        val password = firstPasswordField.text.toString()
        val confermaPassword = confirmPasswordField.text.toString()

        if (nomeCognome.isEmpty()) {
            //settiamo un errore se il campo in cui inserire nome e cognome è vuoto
            editTextPersonNameSurname.error = resources.getString(R.string.empty_name_surname)
            return
        }

        if (!isValidEmail(email)) {
            //settiamo un errore se l'email inserita dall'utente non è valida
            editTextEmailRegister.error = resources.getString(R.string.invalid_email)
            return
        }

        if (radioGroupGender.checkedRadioButtonId == -1) { //nessun radio button selezionato da parte dell'utente
            //genderSelection.error = resources.getString(R.string.gender_not_selected)
            genderSelection = resources.getString(R.string.gender_not_selected)
            Toast.makeText(this, ""+genderSelection, Toast.LENGTH_LONG).show()
            return
        }

        /*if (dateOfBirth.toString() == "") {
            Toast.makeText(
                this,
                "Per favore inserire la propria data di nascita",
                Toast.LENGTH_LONG
            ).show()
            return
        }*/

        if (!isValidPassword(password)) {
            //settiamo un errore se la password inserita dall'utente non soddisfa i requisiti di lunghezza
            editTextPasswordRegister.error = resources.getString(R.string.invalid_password)
            return
        }

        if (!isValidPassword(confermaPassword) && (confermaPassword != password)) {
            //settiamo un errore se le password non combaciano tra loro
            editTextConfirmPassword.error = resources.getString((R.string.password_check))
            return
        }
        registerNewAccount(nomeCognome, email, password)
    }

    private fun registerNewAccount(nomeCognome: String, email: String, password: String) {

        mRegister.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task -> //proviamo a registrare utente
                if (task.isSuccessful) { //email utente non era presente nella sezione di autenticazione
                    val userAuth = mRegister.currentUser //prelevo informazioni utente
                    val id = userAuth!!.uid
                    val userMap = HashMap<String, Any>()

                    userMap["ID utente"] = id
                    userMap["Nome e Cognome"] = nomeCognome
                    userMap["Email"] = email
                    userMap["Genere"] = genderRadio.text
                    userMap["Data di nascita"] = dateOfBirth.toString()
                    val dateTime = LocalDateTime.now()
                    userMap["Data registrazione"] =
                        dateTime.format(DateTimeFormatter.ofPattern("d/M/y H:m:ss"))
                    userMap["EncryptedPassword"] = BASE64.encrypt(password).toString()

                    //creazione documento
                    mFireStore.document(id).set(userMap).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { //creazione documento andata a buon fine
                            Toast.makeText(this, "Registrazione completata", Toast.LENGTH_LONG)
                                .show()
                            finish()
                        } else { //caricamento dati su firestore non riuscito
                            Toast.makeText(this, "Registrazione fallita", Toast.LENGTH_LONG).show()
                            Log.w(TAG, "Accesso fallito", task.exception)
                        }
                    }
                } else {
                    Log.w(TAG, "Email già esistente", task.exception)
                }
            }
    }

    private fun isValidPassword(pwd: String): Boolean {
        val PASSWORD_PATTERN = ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$")
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(pwd)
        return matcher.matches()
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