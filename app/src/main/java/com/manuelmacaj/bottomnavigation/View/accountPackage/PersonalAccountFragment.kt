package com.manuelmacaj.bottomnavigation.View.accountPackage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.manuelmacaj.bottomnavigation.Model.Utente
import com.manuelmacaj.bottomnavigation.R

class PersonalAccountFragment : Fragment() {

    private val TAG = "PersonalAccountFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        val view =  inflater.inflate(R.layout.fragment_personal_account, container, false)

        requireActivity().title = getString(R.string.account)


        return view
    }
}