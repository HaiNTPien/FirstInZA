package com.example.listwithanimation.view

import android.Manifest
import android.accounts.Account
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.R
import com.example.listwithanimation.SyncService
import com.example.listwithanimation.`interface`.Contact
import com.example.listwithanimation.adapters.ContactAdapter
import com.example.listwithanimation.adapters.ContactSyncAdapter
import com.example.listwithanimation.databinding.ActivityContactBinding
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.ListContactModel
import com.example.listwithanimation.presenters.ContactPresenters


class ContactActivity : AppCompatActivity(), Contact.View{
    private lateinit var binding: ActivityContactBinding
    var presenters: ContactPresenters? = null
    private var contactSyncAdapter = ContactSyncAdapter(this, true)
    private val requestReadContactsPermissions = 100
    private val requestServiceReadAndWriteContactsPermissions = 101
    private var needUpdate = false
    private var activityInForeground = true
    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(" onReceive ", " onReceive ")
            if (intent != null) {
                if (intent.hasExtra("message")) {
                    if(activityInForeground) {
                        presenters?.onClickSubmitList(this@ContactActivity, packageManager)
                    }else {
                        needUpdate = true
                    }
                }
            }
        }

    }
    private val adapter : ContactAdapter by lazy {
        ContactAdapter()
    }
    private val account = Account("ABC", "vnd.com.app.call")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact)
        presenters = ContactPresenters(this, ListContactModel())
//        createAccount()
        initListContact()
        initService()
    }

//    fun createAccount(){
//        val accountManager = AccountManager.get(this)
//        val success = accountManager.addAccountExplicitly(account, null, null)
//        val extras = intent.extras
//        if (extras != null) {
//            if (success) {
//                val response: AccountAuthenticatorResponse? = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
//                val result = Bundle()
//                result.putString(AccountManager.KEY_ACCOUNT_NAME, "ABC")
//                result.putString(AccountManager.KEY_ACCOUNT_TYPE, "vnd.com.app.call")
//                response?.onResult(result)
//            }
//            finish()
//        }
//    }

//    @SuppressLint("Range")
//    fun handleBackFromSystemContact(){
//        val dataStr = intent.dataString
//        if (dataStr != null){
//            Log.d(" Data return ", dataStr.toString())
//            Toast.makeText(this, dataStr.toString(), Toast.LENGTH_SHORT).show()
//            val dataId = dataStr.split("/")
////            Toast.makeText(this, adapter.getNumberByRawContactID(dataId.last()), Toast.LENGTH_SHORT).show()
//            val cursor = this.contentResolver.query(
//                dataStr.toUri(), arrayOf(
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                    ContactsContract.Data.SYNC1,
//                    ContactsContract.Data.SYNC2,
//                    ContactsContract.Data.SYNC3,
//                    ContactsContract.Data.SYNC4
//                ),
//                null, null, null
//            )
//            cursor?.moveToFirst()
//
//            val number =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ))
//            val name =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME ))
//            val s1 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC1 ))
//            val s2 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC2 ))
//            val s3 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC3 ))
//            val s4 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC4 ))
//
////            Toast.makeText(this, "$name $number $s1 $s2 $s3 $s4", Toast.LENGTH_SHORT).show()
//            cursor?.close()
//        }
//
//    }

    private fun initService() {
        if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
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

    private fun initListContact() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS),
                requestReadContactsPermissions
            )
        } else {
            presenters?.onClickSubmitList(this, packageManager)
            contactSyncAdapter.onPerformSync(account, null, null, null, null)

        }
//        presenters?.onClickSubmitList(this)
        binding.rvContact.apply {
            layoutManager = linearLayoutManager
            this@ContactActivity.adapter.onItemClickCallback = {
//                val intent = Intent(
//                    Intent.ACTION_DIAL,
//                    Uri.parse("tel:$it")
//                )
//                startActivity(intent)
//                presenters?.onClickSubmitList(this@ContactActivity, packageManager)
                ContactManager.deleteAllSystemContact(context)
            }
            this@ContactActivity.adapter.configRecyclerView(this)
        }


    }

    override fun move(firstPosition: Int, secondPosition: Int) {

    }

    override fun removeOne(position: Int) {

    }

    override fun addOne(item: ContactModel) {

    }

    override fun setList(lst: List<ContactModel>) {
        binding.rvContact.post {
            adapter.submitList(lst)
        }
    }

    override fun onStop() {
        super.onStop()
        activityInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "enter")
        if(needUpdate) {
            Log.d("onResume", "need update")
            needUpdate = false
            presenters?.onClickSubmitList(this@ContactActivity, packageManager)
        }
        activityInForeground = true
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
                    // Permission is granted
                    presenters?.onClickSubmitList(this, packageManager)
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
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("passMessage")
        registerReceiver(broadcastReceiver, intentFilter)
    }

}