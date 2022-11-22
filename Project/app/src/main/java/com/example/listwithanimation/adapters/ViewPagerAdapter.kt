package com.example.listwithanimation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.listwithanimation.view.ContactFragment
import com.example.listwithanimation.view.LoggingFragment

class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val contactFragment = ContactFragment()
    val loggingFragment = LoggingFragment()
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> contactFragment
            else -> loggingFragment
        }
    }

    override fun getCount(): Int {
        return 2
    }

}