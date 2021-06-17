package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.manuelmacaj.bottomnavigation.Global.Global
import com.manuelmacaj.bottomnavigation.R
import kotlinx.android.synthetic.main.fragment_personal_account.view.*
import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.util.*

class PersonalAccountFragment : Fragment() {

    private val TAG = "PersonalAccountFragment"

    private lateinit var textFullName: TextView
    private lateinit var textEmail: TextView
    private lateinit var textAge : TextView
    private lateinit var textGender: TextView
    private lateinit var textDateofBirth: TextView
    private lateinit var dateOfBirth: LocalDate
   // private var currentTime: Date = Calendar.getInstance().time
    private var currentTime: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_account, container, false)

        requireActivity().title = getString(R.string.account)

        textFullName = view.findViewById(R.id.fullName)
        textEmail = view.findViewById(R.id.email)
        textAge = view.findViewById(R.id.age)
        textGender = view.findViewById(R.id.userGender)
        textDateofBirth = view.findViewById(R.id.userBirthday)

        val arrayDate: List<String> = Global.utenteLoggato?.dataNascita?.split("-")!!

        dateOfBirth = LocalDate.of(arrayDate[0].toInt(), arrayDate[1].toInt(), arrayDate[2].toInt())

        if((currentTime.month <= dateOfBirth.month) && (currentTime.dayOfMonth < dateOfBirth.dayOfMonth))
            textAge.text = ((currentTime.year - dateOfBirth.year) -1).toString()
        else
            textAge.text = (currentTime.year - dateOfBirth.year).toString()

        textAge.append(" years")

        view.ModifyProfile.setOnClickListener {
            openEditProfileActivity()
        }

        return view
    }

    private fun openEditProfileActivity() {
        val intent = Intent(requireActivity(), EditProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        //aggiorno i dati dell'utente che sono stati modificati
        textFullName.text = Global.utenteLoggato?.nomeCognomeUtente
        textEmail.text = Global.utenteLoggato?.emailUtente
        textGender.text = Global.utenteLoggato?.genere
        textDateofBirth.text = Global.utenteLoggato?.dataNascita
    }
}