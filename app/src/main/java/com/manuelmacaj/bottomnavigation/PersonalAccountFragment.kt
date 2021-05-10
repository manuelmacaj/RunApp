package com.manuelmacaj.bottomnavigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PersonalAccountFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        val view =  inflater.inflate(R.layout.fragment_personal_account, container, false)

        requireActivity().title = getString(R.string.account)

        return view
    }
}