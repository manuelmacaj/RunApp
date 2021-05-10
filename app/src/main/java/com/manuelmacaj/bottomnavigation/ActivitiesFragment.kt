package com.manuelmacaj.bottomnavigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class ActivitiesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        requireActivity().title = getString(R.string.activity)

        var listView = view.findViewById(R.id.activitiesList) as ListView

        val something = arrayOf(
            "Ciao",
            "miao",
            "bau"
        )

        val listViewAdapter: ArrayAdapter<String> = ArrayAdapter(this.activity!!, android.R.layout.simple_list_item_1, something)

        listView.adapter = listViewAdapter

        return view
    }
}