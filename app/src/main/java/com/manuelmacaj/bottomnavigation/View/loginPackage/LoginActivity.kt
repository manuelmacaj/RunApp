package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.Model.Utente
import com.manuelmacaj.bottomnavigation.R
import com.manuelmacaj.bottomnavigation.View.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance() //istanza firebase, sezione autenticazione
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.title = resources.getString(R.string.signin) //imposto il titolo che verrà visualizzato sulla toolbar
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //la night mode viene disabilata
    }

    override fun onStart() {
        super.onStart()
        //verifico se l'utente ha fatto la login o meno
        val user = mAuth.currentUser //credenziali utente loggato
        if (user != null) { //utente ha già fatto accesso
            readUserDocument(user.uid)
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent( //apriamo l'altra activity
            this@LoginActivity,
            MainActivity::class.java
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    fun openRegisterActivity(v: View?) { //funzione consente di aprire activity per la registrazione
        val intent = Intent(
            this@LoginActivity,
            RegisterActivity::class.java
        )
        startActivity(intent)
    }

    fun checkLogin(v: View?) { //deve capire se quello inserito in username e password è valido o meno e nel caso segnalare
        val email: String = editTextEmailLogin.getText().toString()
        if (!isValidEmail(email)) {
            editTextEmailLogin.setError(getResources().getString(R.string.invalid_email))
            return //non proseguo (guard)
        }

        val pwd = editTextPasswordRegister.getText().toString()
        if (!isValidPassword(pwd)) {
            editTextPasswordRegister.setError(getResources().getString(R.string.invalid_password)) //...settiamo un errore
            return //non proseguo (guard)
        }
        ChecktoFirebase(email, pwd)
    }

    private fun readUserDocument(documentID: String) { // funzione per la lettura del documento su firestore.
        mFireStore.document(documentID).get()
            .addOnSuccessListener { documentSnapshot -> //caso successo
                if (documentSnapshot.exists()) { //se il documento esiste
                    val idutente = documentSnapshot.getString("ID utente")
                    val nomeCognome = documentSnapshot.getString("Nome e Cognome")
                    val emailFirestore = documentSnapshot.getString("Email")
                    val dataNascita = documentSnapshot.getString("Data di nascita")
                    val genere = documentSnapshot.getString("Genere")
                    val pwdCriptata = documentSnapshot.getString("EncryptedPassword")

                    Global.utenteLoggato = Utente(
                        idutente.toString(),
                        nomeCognome.toString(),
                        emailFirestore.toString(),
                        dataNascita.toString(),
                        genere.toString(),
                        pwdCriptata.toString()
                    )
                    Log.d(TAG, Global.utenteLoggato!!.toStringUtente())
                } else {
                    Log.d(TAG, "Non disp su firestore")
                }
            }
    }

    private fun ChecktoFirebase(
        email: String,
        password: String
    ) { //verifichiamo se utente è autenticato o meno
        //verifichiamo email e password utente
        mAuth.signInWithEmailAndPassword(email, password)
            //verifico ciò che è successo
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { //tutto ok
                    Toast.makeText(this, "Accesso effettuato", Toast.LENGTH_LONG).show()

                    val user = mAuth.currentUser //credenziali utente loggato
                    //preleviamo dati utenti
                    if (user != null) {
                        readUserDocument(user.uid)
                        openMainActivity()
                    }
                } else { //autenticazione non è andata a buon fine
                    Toast.makeText(this, "Accesso negato, registrati", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Accesso fallito", task.exception)
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