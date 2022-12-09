package com.example.listwithanimation.view

import android.Manifest
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.listwithanimation.utils.APP_ACCOUNT_NAME
import com.example.listwithanimation.utils.APP_ACCOUNT_TYPE
import com.example.listwithanimation.R
import com.example.listwithanimation.SyncService
import com.example.listwithanimation.adapters.ViewPagerAdapter
import com.example.listwithanimation.databinding.ActivityContactBinding
import com.example.listwithanimation.viewmodels.ContactViewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class ContactActivity : AppCompatActivity(){
    private lateinit var binding: ActivityContactBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var needUpdate = false
    private lateinit var navigation : BottomNavigationView
    private var activityInForeground = true
    private val requestReadContactsPermissions = 100
    private val requestServiceReadAndWriteContactsPermissions = 101
    private val account = Account(APP_ACCOUNT_NAME, APP_ACCOUNT_TYPE)
    lateinit var viewModel : ContactViewModels
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.hasExtra("message")) {
                    if(activityInForeground) {
                        if (context != null) {
                            (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity)
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
        viewModel = ViewModelProvider(this)[ContactViewModels::class.java]
        navigation = binding.navigation
        askForPermissions()
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
                    return true
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
        if(needUpdate) {
            needUpdate = false
            (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity)
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
                    (viewPagerAdapter.getItem(0) as ContactFragment).updateContactList(this@ContactActivity)
                }
            }
            requestServiceReadAndWriteContactsPermissions -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setViewPagerAdapter()
                    createAccount()
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
    private fun askForPermissions() {
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
            setViewPagerAdapter()
            createAccount()
            val serviceIntent = Intent(this, SyncService::class.java)
            startService(serviceIntent)
            registerReceiver()
        }
    }
    private fun createAccount(){
        val accountManager = AccountManager.get(this)
        val success = accountManager.addAccountExplicitly(account, null, null)
        val extras = intent.extras
        if (extras != null) {
            if (success) {
                val response: AccountAuthenticatorResponse? = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
                val result = Bundle()
                result.putString(AccountManager.KEY_ACCOUNT_NAME, APP_ACCOUNT_NAME)
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, APP_ACCOUNT_TYPE)
                response?.onResult(result)
            }
            finish()
        }
    }

    private fun setViewPagerAdapter(){
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.notifyDataSetChanged()
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                viewPagerAdapter.turnOffLoading(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.adapter?.notifyDataSetChanged()
        navigation.setOnItemSelectedListener(onNavigationItemSelectedListener)
    }

}