package com.manuelmacaj.bottomnavigation.View.loginPackage

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.manuelmacaj.bottomnavigation.R
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.regex.Pattern

class RegisterActivityMaterial : AppCompatActivity() {
    private var TAG = "RegisterActivity"
    private lateinit var nomeCognomeTextInputLayout: TextInputLayout
    private lateinit var nomeCognomeTextInputEditText: TextInputEditText
    private lateinit var genere: TextInputLayout
    private lateinit var dataNascitaRegistrazione: TextInputLayout
    private lateinit var mDateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var dateOfBirth: LocalDate
    private lateinit var emailField: TextInputLayout
    private lateinit var firstPasswordField: TextInputLayout
    private lateinit var confirmPasswordField: TextInputLayout
    private lateinit var registerButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_material_design)
        this.title = resources.getString(R.string.register)

        nomeCognomeTextInputLayout = findViewById(R.id.fullNameMaterialDesignLayoutRegister)
        nomeCognomeTextInputEditText = findViewById(R.id.fullNameMaterialDesignEditTextRegister)
        genere = findViewById(R.id.genderMaterialDesignLayoutRegister)
        dataNascitaRegistrazione = findViewById(R.id.DateMaterialDesignLayoutRegister)
        emailField = findViewById(R.id.emailMaterialDesignLayoutRegister)
        firstPasswordField = findViewById(R.id.passwordMaterialDesignLayoutRegister)
        confirmPasswordField = findViewById(R.id.confirmPasswordMaterialDesignLayoutRegister)

        registerButton = findViewById(R.id.buttonRegistrationMaterialDesign)

        val items = listOf(resources.getString(R.string.male), resources.getString(R.string.female))
        val adapter = ArrayAdapter(this, R.layout.list_item_gender, items)
        (genere.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        dataNascitaRegistrazione.editText?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleDateofBirth))
                .setMessage(getString(R.string.messageDateofBirth))
                .setPositiveButton(getString(R.string.yesButton)) { _, _ ->
                    val cal = Calendar.getInstance()
                    val year = cal[Calendar.YEAR]
                    val month = cal[Calendar.MONTH]
                    val day = cal[Calendar.DAY_OF_MONTH]

                    val dialog = DatePickerDialog(this, mDateSetListener, year, month, day)

                    dialog.datePicker.maxDate = System.currentTimeMillis()
                    dialog.show()
                }
                .setCancelable(false)
                .create()
                .show()
        }

        mDateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val mese: Int = month + 1
            Log.d(TAG, "onDateSet: mm/dd/yyy: $mese/$day/$year")
            dateOfBirth = LocalDate.of(year, mese, day)
            dataNascitaRegistrazione.editText?.setText(dateOfBirth.toString())
        }

        // it disables if the user add text
        nomeCognomeTextInputEditText.addTextChangedListener {
            if (nomeCognomeTextInputLayout.isErrorEnabled) {
                nomeCognomeTextInputLayout.isErrorEnabled = false
            }
        }

        // it disables if the user selects something
        genere.editText?.addTextChangedListener {
            if (genere.isErrorEnabled)
                genere.isErrorEnabled = false
        }

        // it disables if the user selects the date
        dataNascitaRegistrazione.editText?.addTextChangedListener {
            if (dataNascitaRegistrazione.isErrorEnabled)
                dataNascitaRegistrazione.isErrorEnabled = false
        }

        registerButton.setOnClickListener {
            checkInput()
        }
    }

    private fun checkInput() {
        checkNameSurname(nomeCognomeTextInputEditText.text.toString())
        checkGender()
        checkDateBirth()
        checkEmail()
        checkPasswords()
    }

    private fun checkNameSurname(nomeCognome: String) { // check name and surname
        if (nomeCognome.isEmpty() || !isValidNameSurname(nomeCognome)) {
            nomeCognomeTextInputLayout.error = resources.getString(R.string.empty_name_surname)
            return
        }
    }

    private fun checkGender() { //check gender
        if (genere.editText?.text.toString() == "") {
            genere.error = resources.getString(R.string.gender_not_selected)
            return
        }
    }

    private fun checkDateBirth() { // check date of birth
        if (dataNascitaRegistrazione.editText?.text.toString() == "") { //se la data di nascita è a null
            dataNascitaRegistrazione.error = resources.getString(R.string.enterDateofBirth)
            return

        } else {
            val agePeriod = Period.between(dateOfBirth, LocalDate.now())
            // if user is too young, it appears an error
            if (agePeriod.years < 14) {
                dataNascitaRegistrazione.error = resources.getString(R.string.yearsOld)
                return
            }
        }
    }

    private fun checkPasswords() { // check passwords fields
        if (!isValidPassword(firstPasswordField.editText?.text.toString())) {
            firstPasswordField.error = resources.getString(R.string.invalid_password)
            return
        }

        if (!isValidPassword(confirmPasswordField.editText?.text.toString()) || (confirmPasswordField.editText?.text.toString() != firstPasswordField.editText?.text.toString())) {
            confirmPasswordField.error = resources.getString((R.string.password_check))
            return
        }
    }

    private fun checkEmail() { // check email
        if (!isValidEmail(emailField.editText?.text.toString())) {
            emailField.error = resources.getString(R.string.invalid_email)
            return
        }

    }

    private fun isValidEmail(email: String): Boolean { // reg ex for email
        val EMAIL_PATTERN =
            ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" //regular expression che bisogna rispettare per l'email
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun isValidNameSurname(nameSurname: String): Boolean { // reg ex for name
        val NAMESURNAME_PATTERN = "^([a-zA-Z]{2,}\\s[a-zA-Z]+'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]+)?)"
        val pattern = Pattern.compile(NAMESURNAME_PATTERN)
        val matcher = pattern.matcher(nameSurname)
        return matcher.matches()
    }

    private fun isValidPassword(pwd: String): Boolean { // rex ex for password
        val PASSWORD_PATTERN =
            ("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$")
        val pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher = pattern.matcher(pwd)
        return matcher.matches()
    }
}