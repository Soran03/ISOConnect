package com.example.fyp_coursework_test.Adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.fyp_coursework_test.Activities.EventDetailsActivity
import com.example.fyp_coursework_test.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CustomEventInfoWindowAdapter(val inflater: LayoutInflater) : GoogleMap.InfoWindowAdapter {

    private val view = inflater.inflate(R.layout.sample_event_info_window, null)

    // Use the default frame for the info window
    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    // bind the event data to layout
    override fun getInfoContents(marker: Marker): View {
        val title = view.findViewById<TextView>(R.id.windowEventTitle)
        val date = view.findViewById<TextView>(R.id.windowEventDate)
        val address = view.findViewById<TextView>(R.id.windowEventAddress)
        val viewMore = view.findViewById<TextView>(R.id.windowEventShowMoreBtn)

        title.text = marker.title
        date.text = marker.snippet!!.split("|")[0]
        address.text = marker.snippet!!.split("|")[1]

        return view
    }



}
