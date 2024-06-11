package com.example.fyp_coursework_test.Fragments

import android.os.AsyncTask
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_VIEW_END
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.ui.res.colorResource
import androidx.core.content.ContextCompat
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Link the Activity to the XML Layout
        binding = FragmentHomeBinding.inflate(layoutInflater)

        // Initialise Firebase Tools
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

        bindPrayerCard()

        return binding.root
    }

    // Sets up the prayer card
    private fun bindPrayerCard() {
        getPrayerData()
        collapsePrayerCard()
    }

    private fun expandPrayerCard() {
        TransitionManager.beginDelayedTransition(binding.homePrayerSectionLayout as ViewGroup) // Animate the transition
        binding.expandPrayerInfo.visibility = View.VISIBLE
        binding.viewMoreButtonCol.setOnClickListener{collapsePrayerCard()}
        binding.viewMoreButtonCol.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_expand_less_24)

    }

    private fun collapsePrayerCard() {
        TransitionManager.beginDelayedTransition(binding.homePrayerSectionLayout as ViewGroup) // Animate the transition
        binding.expandPrayerInfo.visibility = View.GONE
        binding.viewMoreButtonCol.setOnClickListener{expandPrayerCard()}
        binding.viewMoreButtonCol.icon = ContextCompat.getDrawable(requireContext(), R.drawable.baseline_expand_more_24)
    }

    // Get the prayer data from AlAdhan HTML request
    private fun getPrayerData() {
        FetchDataTask().execute("https://api.aladhan.com/v1/timingsByCity?" +
                "city=London&" +
                "country=United Kingdom&" +
                "method=8&" +
                "school=1")
    }

    // fetch data from URL request and manipulate the layout timetable
    private inner class FetchDataTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String {
            val apiUrl = params[0]
            return fetchData(apiUrl)
        }

        override fun onPostExecute(result: String) {
            val jsonResponse = JSONObject(result)
            val timings = jsonResponse.getJSONObject("data").getJSONObject("timings")
            val date = jsonResponse.getJSONObject("data").getJSONObject("date")

            // set prayer time text views
            binding.fajrTime.text = timings.getString("Fajr")
            binding.sunriseTime.text = timings.getString("Sunrise")
            binding.dhuhrTime.text = timings.getString("Dhuhr")
            binding.asrTime.text = timings.getString("Asr")
            binding.maghribTime.text = timings.getString("Maghrib")
            binding.ishaTime.text = timings.getString("Isha")

            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())


            // Logic to find the current prayer time according to the current time
            val prayerTimes = LinkedHashMap<String,String>()
            prayerTimes.put("Fajr", timings.getString("Fajr"))
            prayerTimes.put("Sunrise", timings.getString("Sunrise"))
            prayerTimes.put("Dhuhr", timings.getString("Dhuhr"))
            prayerTimes.put("Asr", timings.getString("Asr"))
            prayerTimes.put("Maghrib", timings.getString("Maghrib"))
            prayerTimes.put("Isha", timings.getString("Isha"))
            prayerTimes.put("Midnight", "23:59")

            var currentPrayerTime: String? = null
            var currentPrayerName: String? = null
            var nextPrayerTime: String? = null

            for ((prayer, time) in prayerTimes) {
                if (currentTime >= time) {
                    // Current time is after or equal to the prayer time
                    currentPrayerTime = time
                    currentPrayerName = prayer
                    nextPrayerTime = time
                } else {
                    // Current time is before the prayer time, break the loop
                    nextPrayerTime = time
                    break
                }
            }

            // Change background to match current prayer
            val currentPrayerBackgroundDrawable = when (currentPrayerName) {
                "Fajr" -> R.drawable.custom_bg_gradient_fajr
                "Sunrise" -> R.drawable.custom_bg_gradient_fajr
                "Dhuhr" -> R.drawable.custom_bg_gradient_dhuhr
                "Asr" -> R.drawable.custom_bg_gradient_asr
                "Maghrib" -> R.drawable.custom_bg_gradient_maghrib
                "Isha" -> R.drawable.custom_bg_gradient_isha
                else -> R.drawable.custom_bg_gradient_isha // Default drawable in case of unknown prayer
            }

            binding.prayerLayoutCol.setBackgroundResource(currentPrayerBackgroundDrawable)

            // Display the current prayer heading
            if (currentPrayerTime != null) {
                binding.homeCurrentPrayerNameCol.text = currentPrayerName
                binding.homeCurrentPrayerTimeCol.text = currentPrayerTime

                val nextPrayerTimeMillis = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(nextPrayerTime!!)?.time
                val currentTimeMillis = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(currentTime)?.time
                val timeLeftMillis = nextPrayerTimeMillis!! - currentTimeMillis!!

                val hoursLeft = TimeUnit.MILLISECONDS.toHours(timeLeftMillis)
                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis) % 60

                val timeLeftString = String.format(Locale.getDefault(), "%02d:%02d", hoursLeft, minutesLeft)

                binding.homeNextPrayerTime.text = "Time until next prayer • $timeLeftString"
            }
            else {
                binding.homeCurrentPrayerNameCol.text = "Error"

            }

            binding.currentDate.text = "Today  •  ${date.get("readable")}"
        }

        // fetch the data from the api URL
        private fun fetchData(apiUrl: String): String {
            val url = URL(apiUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            return try {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                response.toString()
            } finally {
                connection.disconnect()
            }
        }

    }}