package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.manuelmacaj.bottomnavigation.R

class ActivitiesFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        requireActivity().title = getString(R.string.activity)

        val listView = view.findViewById(R.id.activitiesList) as ListView

        val something = arrayOf(
            "prova 1",
            "prova 2",
            "prova 3"
        )

        val listViewAdapter: ArrayAdapter<String> = ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, something)

        listView.adapter = listViewAdapter

        return view
    }
}