package com.example.listwithanimation

import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.listwithanimation.adapters.ContactSyncAdapter
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.set
import com.google.gson.Gson


class SyncService : Service() {
    private lateinit var mSyncAdapter: ContactSyncAdapter
    private var handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {
        val lst = ContactManager.queryContact(contentResolver)
//                sharePref["contacts"] = Gson().toJson(lst)
//        if(!ContactManager.syncContact(context = this@SyncService, lst)) {
        if (lst.second) {
            Log.d(" Handler ", " Sync contact")
            ContactManager.syncContact(context = this@SyncService, lst.first.distinctBy { it.id })
        }else {
        Log.d(" Handler ", " Logging ")
        ContactManager.logChangeInContact(
            context = this@SyncService,
            newList = lst.first.distinctBy { it.id }.toMutableList()
        )
        }
        passMessageToActivity("needUpdate")
    }

    override fun onCreate() {
        super.onCreate()
        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            object : ContentObserver(null) {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
//                val sharePref = SharePreferences.defaultPrefs(this@SyncService)
                    if (handler.hasCallbacks(runnable)) {
                        Log.d(" Handler ", " Has Callback")
                        handler.removeCallbacks(runnable)
                        Log.d(" Handler ", " Remove Callback")
                        handler.postDelayed(runnable, 400)
                        Log.d(" Handler ", " add callback")
                    } else {
                        Log.d(" Handler ", " Not Have Callback")
                        handler.postDelayed(runnable, 400)
                        Log.d(" Handler ", " add callback")
                    }

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

    fun passMessageToActivity(needUpdate: String) {
        val intent = Intent()
        intent.action = "passMessage"
        intent.putExtra("message", needUpdate)
        sendBroadcast(intent)
    }
}