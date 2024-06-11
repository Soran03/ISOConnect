package com.example.fyp_coursework_test.Fragments

import android.content.Intent
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.fyp_coursework_test.Adapters.CustomEventInfoWindowAdapter
import com.example.fyp_coursework_test.Adapters.PlaceAutocompleteAdapter
import com.example.fyp_coursework_test.Activities.AddEventActivity
import com.example.fyp_coursework_test.Activities.EventDetailsActivity
import com.example.fyp_coursework_test.Models.EventModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.FragmentEventsBinding
import com.google.android.gms.common.api.ApiException

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsFragment : Fragment() {
    private lateinit var binding: FragmentEventsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var eventsList = ArrayList<EventModel>()

    private lateinit var placesClient: PlacesClient

    private var searchedMarker: Marker? = null
    private val defaultZoomLevel = 5f
    private val markerZoomLevel = 15f

    // middle of UK coordinates
    private val callback = OnMapReadyCallback { googleMap ->
        val middleOfUk = LatLng(51.5072, 0.1276)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(middleOfUk, defaultZoomLevel))

        showEvents()

        googleMap.setOnInfoWindowClickListener { marker ->
            Log.d("EventsFragment", "Marker snippet: ${marker.snippet}")

            val eventId = marker.snippet!!.split("| ")[2]
            val intent = Intent(requireContext(), EventDetailsActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Link the Activity to the XML Layout
        binding = FragmentEventsBinding.inflate(layoutInflater)

        // Initialise Firebase Tools
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Google Places
        if (!Places.isInitialized()) {
            Places.initialize(
                requireContext(),
                getString(R.string.google_places_api_key)
            )
        }
        placesClient = Places.createClient(requireContext())

        // Set up the map to hover over UK
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        buttonOnClickListeners()
        setupAutoCompleteTextView()

    }

    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Navigate to the add events activity
        binding.eventsAddButton.setOnClickListener {
            val intent = Intent(requireContext(), AddEventActivity::class.java)
            startActivity(intent)
        }
    }

    // Set up the address search bar using Places Auto Complete
    private fun setupAutoCompleteTextView() {

        // set custom autocompleteAdapter to the search bar
        val autocompleteAdapter = PlaceAutocompleteAdapter(requireContext(), placesClient)
        binding.eventsAddressInputACTextView.setAdapter(autocompleteAdapter)

        // Set click listener for the address suggestions to make it show on the map
        binding.eventsAddressInputACTextView.setOnItemClickListener { parent, _, position, _ ->

            // complete text in the search bar
            val selectedPlace = parent.adapter.getItem(position) as AutocompletePrediction
            binding.eventsAddressInputACTextView.setText(selectedPlace.getPrimaryText(null).toString())

            val placeId = selectedPlace.placeId
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            // request the selected place from Google and pin it on the map temporarily
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { latLng ->
                    val mapFragment =
                        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

                    // zoom in to the selection
                    mapFragment?.getMapAsync { googleMap ->
                        googleMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                latLng,
                                markerZoomLevel
                            )
                        )

                        // Remove existing marker if any
                        searchedMarker?.remove()

                        // Add a new marker
                        searchedMarker = googleMap.addMarker(
                            MarkerOptions().position(latLng)
                                .title(selectedPlace.getPrimaryText(null).toString())
                        )

                        // Set a map click listener to remove the marker and zoom out
                        googleMap.setOnMapClickListener {
                            searchedMarker?.remove()
                            searchedMarker = null
                            googleMap.animateCamera(CameraUpdateFactory.zoomOut())
                        }
                    }
                }
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("PlaceAutocomplete", "Place not found: " + exception.statusCode)
                }
            }
        }
    }

    // fill the eventsList and placeEventMarker()
    private fun showEvents() {
        val reference = database.reference.child("Events")

        // check all events in database and add it to the eventsList
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                eventsList.clear()
                for (snapshot: DataSnapshot in dataSnapshot.children) {
                    val event = snapshot.getValue(EventModel::class.java)

                    // if event is valid add to list
                    if (event != null) {
                        eventsList.add(event)
                    }
                }

                // Place all the event markers after completing the list
                placeEventMarkers()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // places all the markers for each event in the eventList by using the coordinates
    private fun placeEventMarkers() {
        val geocoder = Geocoder(requireContext())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?


        mapFragment?.getMapAsync { googleMap ->

            // Set the info window to be custom for all markers using infoWindowAdapter
            val infoWindowAdapter = CustomEventInfoWindowAdapter(LayoutInflater.from(context))
            googleMap.setInfoWindowAdapter(infoWindowAdapter)

            // try to place individual marker for each event in the list
            for (event in eventsList) {
                try {
                    val addressList = geocoder.getFromLocationName(event.address, 1)
                    if (addressList!!.isNotEmpty()) {
                        val address = addressList[0]
                        val latLng = LatLng(address.latitude, address.longitude)
                        googleMap.addMarker(MarkerOptions()
                            .position(latLng)
                            .title(event.eventName)
                            .snippet("${formatEventDate(event.eventDate)}| ${event.address}| ${event.eventId}"))

                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun formatEventDate(date: Date?): String {
        if (date == null) {
            return ""
        }
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

}
