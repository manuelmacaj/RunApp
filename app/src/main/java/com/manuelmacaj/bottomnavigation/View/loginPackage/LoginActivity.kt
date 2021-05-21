package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance() //istanza firebase, sezione autenticazione
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()
        //verifico se l'utente ha fatto la login o meno

    }

    fun openRegisterActivity(v: View?){ //funzione consente di aprire activity per la registrazione
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java) //primo paramatro->context;secondo parametro->activity che deve essere eseguita
        startActivity(intent)
    }

    fun checkLogin(v: View?) { //deve capire se quello inserito in username e password è valido o meno e nel caso segnalare
        val email: String = editTextEmailLogin.getText().toString()
        if(!isValidEmail(email)){
            editTextEmailLogin.setError(getResources().getString(R.string.invalid_email))
            return
        }

        val pwd = editTextPasswordRegister.getText().toString()
        if(!isValidPassword(pwd)){
            editTextPasswordRegister.setError(getResources().getString(R.string.invalid_password)) //...settiamo un errore
            return //non proseguo (guard)
        }
        ChecktoFirebase(email, pwd)
    }

    private fun ChecktoFirebase(email: String, password: String){ //verifichiamo se utente è autenticato o meno
        //verifichiamo email e password utente
        mAuth.signInWithEmailAndPassword(email, password)
            //verifico ciò che è successo
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful) { //tutto ok
                    Toast.makeText(this, "Accesso effettuato", Toast.LENGTH_LONG).show()
                }
                else { //autenticazione non è andata a buon fine
                    Toast.makeText(this, "Accesso negato, registrati", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Accesso fallito", task.exception)
                }
            }
    }

    private fun isValidPassword(pwd: String): Boolean {
        return if ((pwd != null) && (pwd.length >= 4))
            true
        else false
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