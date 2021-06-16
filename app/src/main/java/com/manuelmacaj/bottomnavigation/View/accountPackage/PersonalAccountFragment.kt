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

class PersonalAccountFragment : Fragment() {

    private val TAG = "PersonalAccountFragment"

    private lateinit var textFullName: TextView
    private lateinit var textEmail: TextView
    private lateinit var textGender: TextView
    private lateinit var textDateofBirth: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_personal_account, container, false)

        requireActivity().title = getString(R.string.account)

        textFullName = view.findViewById(R.id.fullName)
        textEmail = view.findViewById(R.id.email)
        textGender = view.findViewById(R.id.userGender)
        textDateofBirth = view.findViewById(R.id.userBirthday)

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