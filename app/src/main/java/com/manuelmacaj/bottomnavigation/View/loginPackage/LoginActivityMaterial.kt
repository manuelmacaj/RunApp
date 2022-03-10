package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.Model.Utente
import com.manuelmacaj.bottomnavigation.R
import com.manuelmacaj.bottomnavigation.R.id.*
import com.manuelmacaj.bottomnavigation.View.MainActivity
import java.util.regex.Pattern


class LoginActivityMaterial : AppCompatActivity() {

    private lateinit var email: TextInputLayout
    private lateinit var passw: TextInputLayout
    private lateinit var mailText: TextInputEditText
    private lateinit var passwordText: TextInputEditText
    private lateinit var signInbutton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private val TAG = "LoginActivity"

    private val mAuth = FirebaseAuth.getInstance() //istanza firebase, sezione autenticazione
    private val mFireStore = FirebaseFirestore.getInstance()
        .collection("Utenti") //istanza firestore riferita alla collezione Utenti

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_material)
        Log.w("LoginActivity", "Prova")
        this.title = resources.getString(R.string.signin)

        email = findViewById(emailMaterialDesignLayout)
        passw = findViewById(passwordMaterialDesignLayout)
        mailText = findViewById(emailMaterialDesignEditText)
        passwordText = findViewById(passwordMaterialDesignEditText)
        signInbutton = findViewById(buttonLoginMaterialDesign)
        registerButton = findViewById(buttonRegisterMaterialDesign)

        signInbutton.setOnClickListener {
            checkLogin()
        }
        registerButton.setOnClickListener {
            openRegisterActivity()
        }
        
    }

    override fun onStart() {
        super.onStart()

        val user = mAuth.currentUser //credenziali utente loggato
        if (user != null) { //utente ha già fatto accesso
            readUserDocument(user.uid)
            openMainActivity()
        }
    }

    private fun checkLogin() { // check input text
        if (!isValidEmail(mailText.text.toString())) {
            email.error = resources.getString(R.string.invalid_email)
            requestFocus(mailText)
            return
        } else {
            email.isErrorEnabled = false
        }
        if (!isValidPassword(passwordText.text.toString())) {
            passw.error = resources.getString(R.string.invalid_password)
            requestFocus(passwordText)
            return
        } else {
            passw.isErrorEnabled = false
        }
        checkToFirebase(mailText.text.toString(), passwordText.text.toString())
    }

    private fun openMainActivity() { //open main activity
        val intent = Intent(
            this@LoginActivityMaterial,
            MainActivity::class.java
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun openRegisterActivity() { //open register activity
        val intent = Intent(
            this@LoginActivityMaterial,
            RegisterActivityMaterial::class.java
        )
        startActivity(intent)
    }


    private fun checkToFirebase(email: String, password: String) { // Firebase auth check
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { //se tutto è andato a buon fine
                    Toast.makeText(this, getString(R.string.signedIn), Toast.LENGTH_LONG).show()
                    val user = mAuth.currentUser
                    if (user != null) {
                        readUserDocument(user.uid)
                        openMainActivity()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.notSignedIn), Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Accesso fallito", task.exception)
                }
            }
    }

    private fun readUserDocument(documentID: String) { // funzione per la lettura del documento su firestore.
        mFireStore.document(documentID).get()
            .addOnSuccessListener { documentSnapshot -> //caso di successo
                if (documentSnapshot.exists()) { //se il documento esiste
                    //prelevo i dati dal documento presente in firestore
                    val idutente = documentSnapshot.getString("ID utente")
                    val nomeCognome = documentSnapshot.getString("Nome e Cognome")
                    val dataNascita = documentSnapshot.getString("Data di nascita")
                    val genere = documentSnapshot.getString("Genere")
                    val percorsoImmagineProfilo = documentSnapshot.getString("URIImage")

                    //inserico le informazioni lette dal documento di firestore in utente loggato
                    Global.utenteLoggato = Utente(
                        idutente.toString(),
                        nomeCognome.toString(),
                        FirebaseAuth.getInstance().currentUser?.email.toString(),
                        dataNascita.toString(),
                        genere.toString(),
                        percorsoImmagineProfilo.toString()
                    )
                    Log.d(TAG, Global.utenteLoggato!!.toStringUtente())
                } else {
                    Log.d(TAG, "Non disponibile su firestore")
                }
            }
    }


    fun isValidPassword(pwd: String): Boolean { //funzione prende in ingresso una password e mi restituisce un risultato booleano
        val PASSWORD_PATTERN =
            ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$") //regular expression che bisogna rispettare per la password
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher =
            pattern.matcher(pwd) //metto a confronto la password dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }

    fun isValidEmail(email: String): Boolean { //funzione prende in ingresso una email e mi restituisce un risultato booleano
        val EMAIL_PATTERN =
            ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" //regular expression che bisogna rispettare per l'email
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher =
            pattern.matcher(email) //metto a confronto l'email dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}