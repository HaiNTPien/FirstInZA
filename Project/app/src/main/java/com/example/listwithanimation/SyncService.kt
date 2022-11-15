package com.example.listwithanimation

import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import com.example.listwithanimation.`interface`.Contact
import com.example.listwithanimation.adapters.ContactSyncAdapter
import com.example.listwithanimation.helpers.ContactManager


class SyncService : Service() {
    private lateinit var mSyncAdapter: ContactSyncAdapter
    override fun onCreate() {
        super.onCreate()
        contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                ContactManager.syncContact(contentResolver, ContactManager.queryContact(contentResolver))
                Log.d("OnChanged Data", "OnChanged Data")
                passMessageToActivity("needUpdate")
            }

            override fun deliverSelfNotifications(): Boolean {
                return true
            }
        })
        mSyncAdapter = ContactSyncAdapter(this, true)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mSyncAdapter.syncAdapterBinder
    }

    fun passMessageToActivity(needUpdate : String){
        val intent = Intent()
        intent.action = "passMessage"
        intent.putExtra("message", needUpdate)
        sendBroadcast(intent)
    }
}