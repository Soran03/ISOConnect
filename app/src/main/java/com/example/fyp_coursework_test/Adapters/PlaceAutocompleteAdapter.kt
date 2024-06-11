package com.example.fyp_coursework_test.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.example.fyp_coursework_test.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class PlaceAutocompleteAdapter(context: Context,
                               private val placesClient: PlacesClient)
    : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_list_item_1) {

    private var results: ArrayList<AutocompletePrediction> = arrayListOf()

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint != null) {

                    // Fetch autocomplete predictions
                    results = getAutocomplete(constraint)

                    filterResults.values = results
                    filterResults.count = results.size
                }

                Log.d("autoCompleteAdapter", "performFiltering: ${results.toString()}")
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    // Update the adapter with the new results
                    Log.d("autoCompleteAdapter", "publishResults: ")
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }

    private fun getAutocomplete(constraint: CharSequence): ArrayList<AutocompletePrediction> {
        val predictions = arrayListOf<AutocompletePrediction>()

        // find autocomplete addresses only in UK
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(constraint.toString())
            .setCountries("GB")
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            for (prediction in response.autocompletePredictions) {
                predictions.add(prediction)
                Log.d("autoCompleteAdapter", "getAutocomplete prediction: ${prediction.toString()} ")

            }
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                // Handle the API exception
            }
        }

        Thread.sleep(500)

        return predictions
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.sample_address_suggestion, parent, false)
        val prediction = getItem(position)
        val textView: TextView = view.findViewById(R.id.textViewSuggestion)
        textView.text = prediction?.getPrimaryText(null).toString()

        return view
    }

    override fun getItem(position: Int): AutocompletePrediction? {
        return results[position]
    }

    override fun getCount(): Int {
        return results.size
    }
    }
