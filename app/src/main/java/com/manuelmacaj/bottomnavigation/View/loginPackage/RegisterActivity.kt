package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.R
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
    private var dateOfBirth: LocalDate? = null
    private lateinit var genderSelection: String


    private val mRegister =
        FirebaseAuth.getInstance() //istanza firebase riferita alla sezione di autenticazione

    //istanza firestore riferita alla collezione Utenti. Se non esiste, la crea
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        this.title =
            resources.getString(R.string.register) //imposto il titolo che verrà visualizzato nella toolbar

        nameSurname = findViewById(R.id.editTextPersonNameSurname)
        emailField = findViewById(R.id.editTextEmailRegister)
        firstPasswordField = findViewById(R.id.editTextPasswordRegister)
        confirmPasswordField = findViewById(R.id.editTextConfirmPassword2)
        radioGroupGender = findViewById(R.id.radioGroupGenderModify)

        firstPasswordField.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Toast di avviso
                    Toast.makeText(this, getString(R.string.warningPassword), Toast.LENGTH_LONG).show()
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        nameSurname.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Toast di avviso
                    Toast.makeText(this, getString(R.string.warningNameSurname), Toast.LENGTH_LONG).show()
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        radioGroupGender.setOnCheckedChangeListener { group, checkedId ->
            genderRadio = findViewById(checkedId)
        }

        mDisplayDate = findViewById(R.id.textViewDate)
        mDisplayDate.setOnClickListener {

            //AlertDialog per la data di nascita utente
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleDateofBirth))
                .setMessage(getString(R.string.messageDateofBirth))
                .setPositiveButton(getString(R.string.yesButton)) { _, _ ->
                    val cal = Calendar.getInstance()
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val day = cal[Calendar.DAY_OF_MONTH]

                    val dialog = DatePickerDialog(
                        this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                        year, month, day
                    )
                    dialog.datePicker.maxDate =
                        System.currentTimeMillis() //il datapicker mostrerà le date fino al giorno corrente
                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.show()
                }
                .setCancelable(false)
                .create()
                .show()
        }
        mDateSetListener =
            OnDateSetListener { datePicker, year, month, day ->
                val mese: Int = month + 1
                Log.d(TAG, "onDateSet: mm/dd/yyy: $mese/$day/$year")
                dateOfBirth = LocalDate.of(year, mese, day)
                mDisplayDate.text = dateOfBirth.toString() //inserisco la data di nascita nella textView
            }
    }

    fun checkRegister(v: View?) { //funzione per verificare se username e password sono validi o meno

        val nomeCognome = nameSurname.text.toString()
        val email = emailField.text.toString()
        val password = firstPasswordField.text.toString()
        val confermaPassword = confirmPasswordField.text.toString()

        if (nomeCognome.isEmpty() || !isValidNameSurname(nomeCognome)) {
            //settiamo un errore se il campo in cui inserire nome e cognome è vuoto o non rispetta la regular expression
            nameSurname.error = resources.getString(R.string.empty_name_surname) //imposto un errore
            return
        }

        if (radioGroupGender.checkedRadioButtonId == -1) { //nessun radio button selezionato da parte dell'utente
            genderSelection = resources.getString(R.string.gender_not_selected) //imposto un errore
            Toast.makeText(this, "" + genderSelection, Toast.LENGTH_LONG).show()
            return
        }

        if (dateOfBirth == null) { //se la data di nascita è a null
            Toast.makeText( //visualizzo un toast per avvisare l'utente
                this,
                getString(R.string.enterDateofBirth),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (!isValidEmail(email)) { //verifico se l'email inserita dall'utente è valida o meno
            emailField.error = resources.getString(R.string.invalid_email) //settiamo un errore
            return
        }

        if (!isValidPassword(password)) { //verifico se la password inserita dall'utente è valida o meno
            firstPasswordField.error = resources.getString(R.string.invalid_password) //settiamo un errore
            return
        }

        if (!isValidPassword(confermaPassword) && (confermaPassword != password)) { //verifico se le due password inserite dall'utente corrispondono o meno
            confirmPasswordField.error = resources.getString((R.string.password_check)) //settiamo un errore
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

                    //inserisco le informazioni dell'utente in una HashMap
                    userMap["ID utente"] = id
                    userMap["Nome e Cognome"] = nomeCognome
                    userMap["Genere"] = genderRadio.text
                    userMap["Data di nascita"] = dateOfBirth.toString()
                    val dateTime = LocalDateTime.now()
                    userMap["Data registrazione"] =
                        dateTime.format(DateTimeFormatter.ofPattern("d/M/y H:m:ss"))
                    userMap["URIImage"] = "userProfile/UPmale_profile_picture.png"

                    //creazione documento con informazioni utente
                    mFireStore.document(id).set(userMap).addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) { //creazione documento andata a buon fine
                            Toast.makeText(this, getString(R.string.registrationCompleted), Toast.LENGTH_LONG)
                                .show()
                            finish()
                        } else { //caricamento dati su firestore non riuscito
                            Toast.makeText(this, getString(R.string.registrationFailed), Toast.LENGTH_LONG).show()
                            Log.w(TAG, "Accesso fallito", task.exception)
                        }
                    }
                } else {
                    Log.w(TAG, "Email già esistente", task.exception)
                    Toast.makeText(this, "Email esistente, usare un altra email", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun isValidPassword(pwd: String): Boolean { //funzione prende in ingresso una password e mi restituisce un risultato booleano
        val PASSWORD_PATTERN = ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$") //regular expression che bisogna rispettare per la password
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(pwd) //metto a confronto la password dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }

    private fun isValidEmail(email: String): Boolean { //funzione prende in ingresso una email e mi restituisce un risultato booleano
        val EMAIL_PATTERN = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" //regular expression che bisogna rispettare per l'email
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email) //metto a confronto l'email dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }

    private fun isValidNameSurname(nameSurname: String): Boolean {
        val NAMESURNAME_PATTERN = "^([a-zA-Z]{2,}\\s[a-zA-Z]+'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]+)?)"
        val pattern = Pattern.compile(NAMESURNAME_PATTERN)
        val matcher = pattern.matcher(nameSurname)
        return matcher.matches()
    }
}