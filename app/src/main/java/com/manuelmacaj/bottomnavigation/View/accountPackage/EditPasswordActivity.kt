package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.os.Bundle
import android.util.Log
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

    //istanza firestore riferita alla collezione utenti. Se non esiste, la crea
    private val mFireStore = FirebaseFirestore.getInstance().collection("Utenti")
    private val mAuthUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_password)
        this.title = resources.getString(R.string.edit_password)

        oldPassword = findViewById(R.id.editTextOldPassword)
        newPassword = findViewById(R.id.editTextNewPassword)
        confirmPassword = findViewById(R.id.editTextConfirmThePassword)
    }

    fun checkPasswordFields(v: View?) { //deve capire se quello inserito in username e password è valido o meno e nel caso segnalare

        val vecchiaPassword = oldPassword.text.toString()
        val password = newPassword.text.toString()
        val confermaPassword = confirmPassword.text.toString()
        val oldPasswordEncrypt = BASE64.encrypt(vecchiaPassword)

        if (oldPasswordEncrypt != Global.utenteLoggato?.encryptedPassword) {
            Log.d(TAG, "La vecchia password non corrisponde alla password utilizzata fino adesseo")
            oldPassword.error = resources.getString(R.string.old_password_check)
            return
        }

        if (vecchiaPassword == password) { //controllo se la nuova password è diversa da quella usata in precedenza
            newPassword.error = resources.getString(R.string.choose_another_password)
            return
        }

        if (!isValidPassword(password)) {
            //settiamo un errore se la password inserita dall'utente non soddisfa i requisiti di lunghezza
            newPassword.error = resources.getString(R.string.invalid_password)
            return
        }

        if (!isValidPassword(confermaPassword) && (confermaPassword != password)) {
            //settiamo un errore se le password non corrispondono
            confirmPassword.error = resources.getString(R.string.password_check)
            return
        }

        updatePassword(password, vecchiaPassword)
    }

    private fun updatePassword(password: String, oldPassword: String) {
        val credential = EmailAuthProvider
            .getCredential(Global.utenteLoggato?.emailUtente.toString(), oldPassword)
        if (mAuthUser == null) {
            return
        }

        mAuthUser.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                mAuthUser.updatePassword(password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Cambio password avvenuta con successo")
                            mFireStore.document(Global.utenteLoggato!!.idUtente).update(
                                "EncryptedPassword", BASE64.encrypt(password)
                            )
                            Global.utenteLoggato?.encryptedPassword =
                                BASE64.encrypt(password).toString()
                            //toast
                            Toast.makeText(
                                this,
                                "Password aggiornata correttamente",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        } else {
                            Log.d(TAG, "Cambio password non avvenuto")
                            //AlertBuilder
                            AlertDialog.Builder(this)
                                .setTitle("Impossibile aggiornare la password")
                                .setMessage("Spiacenti, non è stato possibile aggiornare la password")
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

        finish()
    }

    private fun isValidPassword(pwd: String): Boolean {
        val PASSWORD_PATTERN = ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$")
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(pwd)
        return matcher.matches()
    }
}