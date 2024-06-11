package com.example.fyp_coursework_test.Activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_coursework_test.Adapters.PlaceAutocompleteAdapter
import com.example.fyp_coursework_test.Models.EventModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivityAddEventBinding
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
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddEventActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityAddEventBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var placesClient: PlacesClient

    private val calendar = Calendar.getInstance()

    private var searchedMarker: Marker? = null
    private val defaultZoomLevel = 5f
    private val markerZoomLevel = 15f

    private val callback = OnMapReadyCallback { googleMap ->
        val middleOfUk = LatLng(53.8, -4.5)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(middleOfUk, defaultZoomLevel))
    }

    private lateinit var selectedAddress: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Link the Activity to the XML Layout
            binding = ActivityAddEventBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            // Initialize Places
            if (!Places.isInitialized()) {
                Places.initialize(this, getString(R.string.google_places_api_key)) // Google Places API Key
            }
            placesClient = Places.createClient(this)

            // Set up the map to hover over UK
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapAddEvent) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)

            buttonOnClickListeners()
            setupAutoCompleteTextView2()
        }
    }


    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // return to main activity
        binding.addEventButton.setOnClickListener{
            finish()
        }

        // Show the Date Picker
        binding.addEventDateInputEditText.setOnClickListener{
            showDatePicker()
        }

        // Add event to database
        binding.addEventButton.setOnClickListener{
            if (checkAllInputs()) addEventToFirebase()
        }

    }

    // Set up the address search bar using Places Auto Complete
    private fun setupAutoCompleteTextView2() {

        // set custom autocompleteAdapter to the search bar
        val autocompleteAdapter = PlaceAutocompleteAdapter(this, placesClient)
        binding.addEventAddressInputEditText.setAdapter(autocompleteAdapter)

        // Set click listener for the address suggestions to make it show on the map
        binding.addEventAddressInputEditText.setOnItemClickListener { parent, _, position, _ ->

            // complete text in the search bar
            val selectedPlace = parent.adapter.getItem(position) as AutocompletePrediction
            binding.addEventAddressInputEditText.setText(selectedPlace.getPrimaryText(null).toString())

            // make the selected address the one going to database
            selectedAddress = selectedPlace.getFullText(null).toString()

            val placeId = selectedPlace.placeId
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            // request the selected place from Google and pin it on the map temporarily
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place
                place.latLng?.let { latLng ->
                    val mapFragment = supportFragmentManager.findFragmentById(R.id.mapAddEvent) as SupportMapFragment?

                    // zoom in to the selection
                    mapFragment?.getMapAsync { googleMap ->
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, markerZoomLevel))

                        // Remove existing marker if any
                        searchedMarker?.remove()

                        // Add a new marker and keep a reference to it
                        searchedMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(selectedPlace.getPrimaryText(null).toString()))

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

    // Display the android date picker menu and put the selected date in the text field
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.DatePickerTheme,
            {DatePicker, year: Int, monthOfYear: Int,  dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.addEventDateInputEditText.text = Editable.Factory.getInstance().newEditable(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)


        )
        datePickerDialog.show()
    }

    // add the event details to database and return to main activity
    private fun addEventToFirebase() {
        val userID = auth.currentUser!!.uid
        val eventTitle = binding.addEventTitleInputEditText.text.toString()
        val eventDateString = binding.addEventDateInputEditText.text.toString()

        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val eventDate = dateFormat.parse(eventDateString) // Parsing the date string
            val eventId = database.reference.child("Events").push().key ?: return

            val event = EventModel(
                eventId = eventId,
                userID = userID,
                eventName = eventTitle,
                address = selectedAddress,
                eventDate = eventDate ?: Date()
            )

            database.reference.child("Events").child(eventId).setValue(event)

            val intent = Intent(this@AddEventActivity, MainActivity::class.java)
            startActivity(intent)

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    // verify all text fields are valid
    private fun checkAllInputs() : Boolean {
        val title = binding.addEventTitleInputEditText.text.toString().trim()
        val address = binding.addEventAddressInputEditText.text.toString().trim()
        val date = binding.addEventDateInputEditText.text.toString().trim()

        if (title.isEmpty() || address.isEmpty() || date.isEmpty()) {
            Toast.makeText(this@AddEventActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


}