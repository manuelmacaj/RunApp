package com.manuelmacaj.bottomnavigation.View.activitiesPackage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.manuelmacaj.bottomnavigation.Model.Corsa
import com.manuelmacaj.bottomnavigation.R

class AdapterActivities (private val context: Context, val data:MutableList<Corsa>) : BaseAdapter(){ // classe che estende base adapter

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var newView = convertView
        if (convertView == null) {
            newView = LayoutInflater.from(context).inflate(R.layout.row, parent, false)
        }
        if (newView != null){
            val day = newView.findViewById<TextView>(R.id.textViewDay)
            val time = newView.findViewById<TextView>(R.id.textViewTime)
            val distance = newView.findViewById<TextView>(R.id.textViewChilometers)
            val averagePale = newView.findViewById<TextView>(R.id.textViewAndaturaMedia)
            day.append(" ${data[position].DataOrarioPartenza}")
            time.append(" ${data[position].tempo}")
            distance.append(" ${data[position].km}")
            averagePale.append(" ${data[position].andaturaMedia}")
        }
        return newView
    }
}