package com.example.listwithanimation

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.Binder
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import com.example.listwithanimation.adapters.ContactSyncAdapter


class SyncService : Service() {
    private lateinit var mSyncAdapter: ContactSyncAdapter
    override fun onCreate() {
        super.onCreate()
        contentResolver.registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
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
        var intent = Intent()
        intent.setAction("passMessage");
        intent.putExtra("message", needUpdate);
        sendBroadcast(intent);
    }
}