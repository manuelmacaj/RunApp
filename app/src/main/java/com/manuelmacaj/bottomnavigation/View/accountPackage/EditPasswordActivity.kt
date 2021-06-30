package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.manuelmacaj.bottomnavigation.BASE64
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import java.util.regex.Pattern

class EditPasswordActivity : AppCompatActivity() {

    private val TAG = "EditPasswordActivity"
    private lateinit var oldPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private val BASE64: BASE64 = BASE64()

    //istanza firestore riferita alla collezione Utenti. Se non esiste, viene creata
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")
    private val mAuthUser =
        FirebaseAuth.getInstance().currentUser //istanza firebase riferita alla sezione di autenticazione

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_password)
        this.title = resources.getString(R.string.edit_password) //imposto un titolo per questa activity

        oldPassword = findViewById(R.id.editTextOldPassword)
        newPassword = findViewById(R.id.editTextNewPassword)
        confirmPassword = findViewById(R.id.editTextConfirmThePassword)

        oldPassword.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Toast di avviso per l'inserimento della password
                    Toast.makeText(this, getString(R.string.warningPassword), Toast.LENGTH_LONG).show()
                }
            }
            v?.onTouchEvent(event) ?: true
        }

        newPassword.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Toast di avviso per l'inserimento della password
                    Toast.makeText(this, getString(R.string.warningPassword), Toast.LENGTH_LONG).show()
                }
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    fun checkPasswordFields(v: View?) { //funzione di verifica delle password inserite dall'utente

        val vecchiaPassword = oldPassword.text.toString()
        val password = newPassword.text.toString()
        val confermaPassword = confirmPassword.text.toString()
        val oldPasswordEncrypt = BASE64.encrypt(vecchiaPassword)

        if (oldPasswordEncrypt != Global.utenteLoggato?.encryptedPassword) { //controllo se la vecchia password corrisponde a quella che abbiamo salvato
            Log.d(TAG, "La vecchia password non corrisponde alla password utilizzata fino adesso")
            oldPassword.error = resources.getString(R.string.old_password_check) //messaggio di errore
            return //non proseguo(guard)
        }

        if (vecchiaPassword == password) { //controllo se la nuova password è diversa da quella usata in precedenza
            newPassword.error = resources.getString(R.string.choose_another_password) //messaggio di errore
            return //non proseguo(guard)
        }

        if (!isValidPassword(password)) { //controllo se la password inserita dall'utente soddisfa i requisiti
            newPassword.error = resources.getString(R.string.invalid_password) //messaggio di errore
            return //non proseguo(guard)
        }

        if (!isValidPassword(confermaPassword) && (confermaPassword != password)) { //controllo se le password corrispondono
            confirmPassword.error = resources.getString(R.string.password_check) //messaggio di errore
            return //non proseguo(guard)
        }

        //AlertDialog per l'aggiornamento di password
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.titlePassword))
            .setMessage(getString(R.string.messagePassword))
            .setPositiveButton(getString(R.string.yesButton)) { _, _ -> //se l'utente clicca su si...
                updatePassword(password, vecchiaPassword) //...verrà aggiornata la password
            }
            .setNegativeButton("No") { _, _ -> //se l'utente clicca su no...
                finish() //...chiudo la finestra corrente
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun updatePassword(password: String, oldPassword: String) { //funzione per l'aggiornamento della password
        val credential = EmailAuthProvider //otteniamo le credenziali (email e password) dell'utente
            .getCredential(Global.utenteLoggato?.emailUtente.toString(), oldPassword)

        if (mAuthUser == null) { //se l'utente non presenta le credenziali (nella cache)
            return //non proseguo
        }

        mAuthUser.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) { //se la riautenticazione va a buon fine
                mAuthUser.updatePassword(password) //aggiorniamo la password dell'utente in firebase authentication
                    .addOnCompleteListener {
                        if (it.isSuccessful) { //se il cambio password è avvenuto correttamente
                            Log.d(TAG, "Cambio password avvenuto con successo")
                            //aggiorniamo la collezione su firestore dell'id utente connesso con la nuova password
                            mFireStore.document(Global.utenteLoggato!!.idUtente).update(
                                "EncryptedPassword", BASE64.encrypt(password)
                            )
                            //aggiorniamo la password dell'utente in locale
                            Global.utenteLoggato?.encryptedPassword =
                                BASE64.encrypt(password).toString()
                            //Toast per notificare all'utente che la password non è stata aggiornata
                            Toast.makeText(
                                this,
                                "Password aggiornata correttamente",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Log.d(TAG, "Cambio password non avvenuto")
                            //alert dialog se il cambio password non avviene correttamente
                            AlertDialog.Builder(this)
                                .setTitle(getString(R.string.titleUpdatePassword))
                                .setMessage(getString(R.string.messageUpdatePassword))
                                .setPositiveButton("Ok") { _, _ ->
                                    /*   Toast.makeText(
                                           this,
                                           "Riprova a riaggiornare la password",
                                           Toast.LENGTH_LONG
                                       ).show()*/
                                }
                                .create()
                                .show()
                        }
                    }
            } else {
                Log.d(TAG, "La riautenticazione è fallita")
            }
        }
        finish() //chiudo la finestra
    }

    private fun isValidPassword(pwd: String): Boolean { //funzione prende in ingresso una password e mi restituisce un risultato booleano
        val PASSWORD_PATTERN = ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$") //regular expression che bisogna rispettare per la password
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(pwd) //metto a confronto la password dell'utente e la regular expression
        return matcher.matches() //restituisco un risultato booleano
    }
}