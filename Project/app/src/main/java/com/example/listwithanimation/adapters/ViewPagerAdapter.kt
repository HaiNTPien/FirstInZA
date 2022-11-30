package com.example.listwithanimation.adapters

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.listwithanimation.view.ContactFragment
import com.example.listwithanimation.view.LoadingFragment
import com.example.listwithanimation.view.LoggingFragment
import java.util.*

class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val contactFragment = ContactFragment()
    private val loggingFragment = LoggingFragment()
    private var isLoadingContactFragment = false
    private var isLoadingLoggingFragment = true
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                if (isLoadingContactFragment) {
                    LoadingFragment()
                }else {
                    contactFragment
                }
            }
            else -> {
                if (isLoadingLoggingFragment) {
                    LoadingFragment()
                }else {
                    loggingFragment
                }
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun turnOffLoading(pagePosition: Int){
        if(pagePosition == 0 && isLoadingContactFragment) {
            isLoadingContactFragment = false
            this.notifyDataSetChanged()
        }
        if(pagePosition == 1 && isLoadingLoggingFragment ) {
            isLoadingLoggingFragment = false
            this.notifyDataSetChanged()
        }
    }


}