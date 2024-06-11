package com.example.fyp_coursework_test.Adapters
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.example.fyp_coursework_test.Fragments.ChatsFragment
import com.example.fyp_coursework_test.Fragments.EventsFragment
import com.example.fyp_coursework_test.Fragments.HomeFragment
import com.example.fyp_coursework_test.Fragments.IsocFragment

// Fragment adapter for the Main Activity's tab layout
class FragmentsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 4
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> IsocFragment()
            2 -> ChatsFragment()
            3 -> EventsFragment()
            else -> HomeFragment()
        }
    }
}