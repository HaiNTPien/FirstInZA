package com.example.listwithanimation.view

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.example.listwithanimation.R
import com.example.listwithanimation.SyncService
import com.example.listwithanimation.adapters.ViewPagerAdapter
import com.example.listwithanimation.databinding.ActivityContactBinding
import com.google.android.material.navigation.NavigationBarView


class ContactActivity : AppCompatActivity(){
    private lateinit var binding: ActivityContactBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var needUpdate = false
    private var activityInForeground = true
    private val requestReadContactsPermissions = 100
    private val requestServiceReadAndWriteContactsPermissions = 101
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(" onReceive ", " onReceive ")
            if (intent != null) {
                if (intent.hasExtra("message")) {
                    if(activityInForeground) {
                        if (context != null) {
                            (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity, packageManager)
                        }
                    }else {
                        needUpdate = true
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact)
        val navigation = binding.navigation
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.viewPager.adapter = viewPagerAdapter
        navigation.setOnItemSelectedListener(onNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.contacts
        binding.viewPager.currentItem = 0
        binding.viewPager.offscreenPageLimit = 0
        initService()
    }
    private val onNavigationItemSelectedListener = object :  NavigationBarView.OnItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.contacts -> {
                    binding.viewPager.currentItem = 0
                    return true
                }
                R.id.log -> {
                    binding.viewPager.currentItem = 1
                    return false
                }
            }
            return false
        }

    }
    override fun onStop() {
        super.onStop()
        activityInForeground = false
    }
    override fun onResume() {
        super.onResume()
        Log.d("onResume", "enter")
        if(needUpdate) {
            Log.d("onResume", "need update")
            needUpdate = false
            (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity, packageManager)
//            (viewPagerAdapter.getItem(1) as LoggingFragment).updateLoggingList()
        }
        activityInForeground = true
    }
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("passMessage")
        registerReceiver(broadcastReceiver, intentFilter)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            requestReadContactsPermissions -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity, packageManager)
                }
            }
            requestServiceReadAndWriteContactsPermissions -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val serviceIntent = Intent(this, SyncService::class.java)
                    startService(serviceIntent)
                    registerReceiver()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
    private fun initService() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
                requestServiceReadAndWriteContactsPermissions
            )
        } else {
            val serviceIntent = Intent(this, SyncService::class.java)
            startService(serviceIntent)
            registerReceiver()

        }
    }
}