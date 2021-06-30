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
    private val mFireStore = FirebaseFirestore.getInstance()
        .collection("Utenti") //istanza firestore riferita alla collezione Utenti
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.title =
            resources.getString(R.string.signin) //imposto il titolo dell'activity che verrà visualizzato sulla toolbar
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //la night mode viene disabilata
    }

    override fun onStart() {
        super.onStart()
        //verifico se l'utente ha già fatto l'accesso o meno
        val user = mAuth.currentUser //credenziali utente loggato
        if (user != null) { //utente ha già fatto accesso
            readUserDocument(user.uid)
            openMainActivity()
        }
    }

    private fun openMainActivity() { //funzione permette di aprire activity main
        val intent = Intent(
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

    //deve capire se quello inserito nei campi username e password sono validi o meno e nel caso segnalare
    fun checkLogin(v: View?) {
        val email: String =
            editTextEmailLogin.text.toString() //prelevo l'email dal relativo editText
        if (!isValidEmail(email)) { //Controllo sull'email che l'utente ha inserito, se c'è qualcosa che non va...
            editTextEmailLogin.error =
                resources.getString(R.string.invalid_email) //...imposto un errore sull'editText
            return //non proseguo (guard)
        }

        val pwd =
            editTextPasswordRegister.text.toString() //prelevo la password dal relativo editText
        if (!isValidPassword(pwd)) {  //Controllo sulla password che l'utente ha inserito, se c'è qualcosa che non va...
            editTextPasswordRegister.error =
                resources.getString(R.string.invalid_password) //...settiamo un errore
            return //non proseguo (guard)
        }
        ChecktoFirebase(email, pwd)
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

    private fun ChecktoFirebase(
        email: String,
        password: String
    ) { //verifichiamo se l'utente è autenticato o meno, controllando la sua email e password
        mAuth.signInWithEmailAndPassword(email, password)
            //verifico ciò che è successo tramite listener
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { //se tutto è andato a buon fine
                    Toast.makeText(this, getString(R.string.signedIn), Toast.LENGTH_LONG).show()

                    val user = mAuth.currentUser //credenziali utente connesso
                    //preleviamo dati utenti
                    if (user != null) { //utente ha già fatto accesso
                        readUserDocument(user.uid)
                        openMainActivity()
                    }
                } else { //autenticazione non è andata a buon fine
                    Toast.makeText(this, getString(R.string.notSignedIn), Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Accesso fallito", task.exception)
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
}