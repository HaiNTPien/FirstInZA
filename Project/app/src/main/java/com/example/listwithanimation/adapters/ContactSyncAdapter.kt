package com.example.listwithanimation.adapters

import android.R.attr.accountType
import android.accounts.Account
import android.annotation.SuppressLint
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.AggregationExceptions
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.RawContacts
import android.util.Log
import android.widget.Toast
import com.example.listwithanimation.models.ContactModel
import java.util.ArrayList


class ContactSyncAdapter: AbstractThreadedSyncAdapter {
//    private var mAccountManager: AccountManager? = null
    constructor(context: Context?, autoInitialize: Boolean) : super(context, autoInitialize) {
//        mAccountManager = AccountManager.get(context)
    }
    constructor(context: Context?, autoInitialize: Boolean, allowParallelSyncs: Boolean) : super(
        context,
        autoInitialize,
        allowParallelSyncs
    ) {
//        mAccountManager = AccountManager.get(context)
    }

    var list = mutableListOf<ContactModel>()
    private fun addCallerIsSyncAdapterParameter(uri: Uri, isSyncOperation: Boolean): Uri {
        return if (isSyncOperation) {
            uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .build()
        } else uri

    }

    override fun onSecurityException(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        syncResult: SyncResult?
    ) {
        Log.e(" onPerformSync ", " onSecurityException ")
        super.onSecurityException(account, extras, authority, syncResult)
    }

    override fun onSyncCanceled() {
        super.onSyncCanceled()
        Log.e(" onPerformSync ", " onSyncCanceled ")
    }

    override fun onUnsyncableAccount(): Boolean {
        Log.e(" onPerformSync ", " onUnsyncableAccount ")
        return super.onUnsyncableAccount()
    }
    override fun onSyncCanceled(thread: Thread?) {
        super.onSyncCanceled(thread)
        Log.e(" onPerformSync ", " onSyncCanceled ")
    }
    /**
     * Chạy khi được gọi
     */
    @SuppressLint("Range", "Recycle")
    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
    }

}