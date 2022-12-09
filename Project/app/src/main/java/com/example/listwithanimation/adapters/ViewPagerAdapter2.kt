package com.example.listwithanimation.adapters

import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.PagerAdapter
import com.example.listwithanimation.view.ContactFragment
import com.example.listwithanimation.view.LoadingFragment
import com.example.listwithanimation.view.LoggingFragment

class ViewPagerAdapter2(var fm: FragmentManager, var lstFragment : List<Fragment> = listOf()) : PagerAdapter() {
    private var fragments = ArrayList<Fragment?>()
    private var loadedSet = HashSet<Fragment>()
    private var transaction: FragmentTransaction? = null

    init {
        if(lstFragment.isNotEmpty()) {
            loadedSet.add(lstFragment.first())
        }
    }
    fun getItem(position: Int): Fragment {
        return if(loadedSet.contains(lstFragment[position])) {
            lstFragment[position]
        }else {
            LoadingFragment()
        }
    }

    override fun getCount() = lstFragment.size

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view === view
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (fragments.size > position) {
            val fragment = fragments[position]
            if (fragment != null) {
                return fragment
            }
        }
        beginTransaction()

        val fragment = getItem(position)
        fragment.setMenuVisibility(false)
        transaction?.add(container.id, fragment)
        while (fragments.size <= position) {
            fragments.add(null)
        }
        fragments[position] = fragment

        return fragment
    }

    fun turnOffLoading(pagePosition: Int) {
        if(pagePosition in lstFragment.indices && !loadedSet.contains(lstFragment[pagePosition])) {
            loadedSet.add(lstFragment[pagePosition])
            this.notifyDataSetChanged()
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val fragment = `object` as Fragment
        beginTransaction()
        fragments[position] = null
        transaction?.remove(fragment)
    }

    override fun finishUpdate(container: ViewGroup) {
        commitTransaction()
    }

    private fun beginTransaction() {
        if (transaction == null) {
            transaction = fm.beginTransaction()
        }
    }

    private fun commitTransaction(){

        if (transaction != null) {
            transaction?.commitNowAllowingStateLoss()
            transaction = null
        }
    }
}