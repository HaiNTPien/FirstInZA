package com.example.listwithanimation.helpers

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.OperationApplicationException
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import com.example.listwithanimation.models.ContactModel
import java.util.ArrayList

class ContactManager {
    companion object {
        @SuppressLint("Range", "Recycle")
        fun queryContact(contentResolver: ContentResolver): MutableList<ContactModel> {
            val lst = mutableListOf<ContactModel>()
            val lstAllQueryResult = mutableListOf<ContactModel>()
            val removeLst = mutableListOf<String>()
            val updateLst = mutableListOf<Triple<String, String, String>>()
            val cur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            if (cur!!.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID)) + ""
                    val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) + ""
                    val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + ""
                    val accountName = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)) + ""
                    val accountType = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)) + ""
                    val rawContactId = cur.getString(cur.getColumnIndex(ContactsContract.RawContacts._ID)) + ""
//                    if(itemExistInList(id, name, number, lst) != -1) {
//                        if(accountName == "abc" && accountType == "vnd.com.app.call") {
//                            lst[itemExistInList(id, name, number, lst)].isSynced = true
//                        }
//
//                    }else {
                        when(accountType) {
                            "vnd.sec.contact.phone" -> {
                                lstAllQueryResult.add(0, ContactModel(id, name, number, "From Phone", accountName, accountType, rawContactId = rawContactId))
                            }
                            "vnd.sec.contact.sim" -> {
                                lstAllQueryResult.add(0, ContactModel(id, name, number, "From Sim 1", accountName, accountType, rawContactId = rawContactId))
                            }
                            "vnd.sec.contact.sim2" -> {
                                lstAllQueryResult.add(0, ContactModel(id, name, number, "From Sim 2", accountName, accountType, rawContactId = rawContactId))
                            }
                            "com.google" -> {
                                lstAllQueryResult.add(0 ,
                                    ContactModel(id, name, number,
                                        "From Google Account : $accountName"
                                        , accountName, accountType, rawContactId = rawContactId)
                                )
                            }
                            "com.samsung.android.exchange" -> {
                                lstAllQueryResult.add(0,
                                    ContactModel(id, name, number,
                                        "From Microsoft Account : $accountName"
                                        , accountName, accountType, rawContactId = rawContactId)
                                )
                            }
                            "com.osp.app.signin" ->{
                                lstAllQueryResult.add(0 ,
                                    ContactModel(id, name, number,
                                        "From Samsung Account : $accountName"
                                        , accountName, accountType, rawContactId = rawContactId)
                                )
                            }
                            "vnd.com.app.call" ->{
                                lstAllQueryResult.add(ContactModel(id, name, number, "Custom type", accountName, accountType, rawContactId = rawContactId))
                            }
                    }


                }
            }
//            var lstSize = lstAllQueryResult.size
//            var index = 0
//            while (index < lstSize) {
//                if(lstAllQueryResult[index].accountType == "vnd.com.app.call") {
//                    lstSize--
//                    val item = lstAllQueryResult.removeAt(index)
//                    lstAllQueryResult.add(item)
//                }
//                index++
//            }

            for(i in lstAllQueryResult) {
                val existItemPosition = itemExistInList(i.id, i.displayName, i.number, lst)
                if(existItemPosition != -1) {
                    if(i.accountName == "abc" && i.accountType == "vnd.com.app.call") {
                        lst[existItemPosition].isSynced = true
                    }
                }else {
                    if(i.accountName == "abc" && i.accountType == "vnd.com.app.call") {
                        val itemNeedUpdate = isItemNeedUpdate(i.id, lst)
                        if(itemNeedUpdate != -1) {
//                            removeLst.add(i.id)
                            updateLst.add(Triple(i.rawContactId, lst[itemNeedUpdate].number, lst[itemNeedUpdate].displayName))
                        }else {
                            removeLst.add(i.id)
                        }
                    }else {
                        lst.add(i)
                    }
                }
            }
            if(updateLst.isNotEmpty()) {
                for (i in updateLst) {
                    updateRawContact(contentResolver, i.first, i.second, i.third)
                }
            }
            if(removeLst.isNotEmpty()) {
                for (i in removeLst) {
                    deleteAContact(contentResolver, i)
                }
            }
            Log.d(" return List ", lst.size.toString())
            return lst
        }
        private fun isItemNeedUpdate(id: String, list: List<ContactModel>) : Int {
            return list.withIndex().firstOrNull  { id == it.value.id }?.index ?: -1
        }
        private fun itemExistInList(id: String, name: String, number: String, list: List<ContactModel>) : Int {
            return list.withIndex().firstOrNull  { it.value.displayName == name && it.value.number == number && id == it.value.id }?.index ?: -1
        }
        @SuppressLint("Range", "Recycle")
        fun queryContactLogging(contentResolver: ContentResolver) {
            val cur = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
            if (cur!!.count > 0) {
                while (cur.moveToNext()) {
                    Log.d(" Phone _ID", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)) + " " )
                    Log.d(" Phone CONTACT_ID", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) + " " )
                    Log.d(" Phone DISPLAY_NAME", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) + " " )
                    Log.d(" Phone NUMBER", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + " ")
                    Log.d(" Phone MIMETYPE", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.MIMETYPE)))
                    Log.d(" Phone DATA1", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)) + " ")
                    Log.d(" Phone DATA2", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2)) + " ")
                    Log.d(" Phone DATA3", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA3)) + " ")
                    Log.d(" Phone DATA4", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA4)) + " ")
                    Log.d(" Phone DATA5", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA5)) + " ")
                    Log.d(" Phone DATA6", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA6)) + " ")
                    Log.d(" Phone DATA7", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA7)) + " ")
                    Log.d(" Phone DATA8", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA8)) + " ")
                    Log.d(" Phone DATA9", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA9)) + " ")
                    Log.d(" Phone DATA10", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA10)) + " ")
                    Log.d(" Phone DATA11", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA11)) + " ")
                    Log.d(" Phone DATA12", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA12)) + " ")
                    Log.d(" Phone DATA13", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA13)) + " ")
                    Log.d(" Phone DATA14", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA14)) + " ")
                    Log.d(" Phone DATA15", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA15)) + " ")
                }
            }
            val cur2 = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, null, null, null)
            if (cur2!!.count > 0) {
                while (cur2.moveToNext()) {
                    Log.d(" Data _ID", cur2.getString(cur2.getColumnIndex(ContactsContract.Data._ID)))
                    Log.d(" Data MIMETYPE", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.MIMETYPE)))
                    Log.d(" Data DATA1", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA1)) + " ")
                    Log.d(" Data DATA2", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA2)) + " ")
                    Log.d(" Data DATA3", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA3)) + " ")
                    Log.d(" Data DATA4", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA4)) + " ")
                    Log.d(" Data DATA5", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA5)) + " ")
                    Log.d(" Data DATA6", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA6)) + " ")
                    Log.d(" Data DATA7", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA7)) + " ")
                    Log.d(" Data DATA8", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA8)) + " ")
                    Log.d(" Data DATA9", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA9)) + " ")
                    Log.d(" Data DATA10", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA10)) + " ")
                    Log.d(" Data DATA11", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA11)) + " ")
                    Log.d(" Data DATA12", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA12)) + " ")
                    Log.d(" Data DATA13", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA13)) + " ")
                    Log.d(" Data DATA14", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA14)) + " ")
//                    Log.d(" Data DATA15", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA15)) + " ")
                    Log.d(" Data CONTACT_ID", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.CONTACT_ID)) + " ")
                    Log.d(" Data RAW_CONTACT_ID", cur2.getString(cur2.getColumnIndex(
                        ContactsContract.Data.RAW_CONTACT_ID)))
                }
            }
            val cur3 = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null)
            if (cur3!!.count > 0) {
                while (cur3.moveToNext()) {
                    Log.d(" RawContacts Name", cur3.getString(cur3.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)) + " ")
                    Log.d(" RawContacts Type", cur3.getString(cur3.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)) + " ")
                    Log.d(" RawContacts CONTACT_ID", cur3.getString(cur3.getColumnIndex(
                        ContactsContract.RawContacts.CONTACT_ID)) + " ")
                    Log.d(" RawContacts _ID", cur3.getString(cur3.getColumnIndex(ContactsContract.RawContacts._ID)) + " ")
                    Log.d(" RawContacts VERSION", cur3.getString(cur3.getColumnIndex(
                        ContactsContract.RawContacts.VERSION)) + " ")
//                Log.d(" RawContacts CONTENT_ITEM_TYPE", cur3.getString(cur3.getColumnIndex(ContactsContract.RawContacts.CONTENT_ITEM_TYPE)) + " ")
                }
            }
            val cur4 = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur4!!.count > 0) {
                while (cur4.moveToNext()) {
                    Log.d(" Contacts _ID", cur4.getString(cur4.getColumnIndex(ContactsContract.Contacts._ID)) + " ")
                    Log.d(" Contacts DISPLAY_NAME", cur4.getString(cur4.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME)) + " ")
                    Log.d(" Contacts NAME_RAW_CONTACT_ID", cur4.getString(cur4.getColumnIndex(
                        ContactsContract.Contacts.NAME_RAW_CONTACT_ID)) + " ")
                    Log.d(" Contacts NAME_RAW_CONTACT_ID", cur4.getString(cur4.getColumnIndex(
                        ContactsContract.Contacts.NAME_RAW_CONTACT_ID)) + " ")
                }
            }
        }
        private fun addCallerIsSyncAdapterParameter(uri: Uri, isSyncOperation: Boolean = true): Uri {
            return if (isSyncOperation) {
                uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build()
            } else uri
        }
        fun syncContact(contentResolver: ContentResolver, list: List<ContactModel>) {
            for (i in list) {
                if (!i.isSynced) {
                    val ops = ArrayList<ContentProviderOperation>()
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(
                                ContactsContract.RawContacts.CONTENT_URI, true)
                        )
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "abc")
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "vnd.com.app.call")
                            .build()
                    )

                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, i.displayName)
                            .build()
                    )
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, i.number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build()
                    )
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.com.app.call")
                            .withValue(ContactsContract.Data.DATA1, i.number)
                            .withValue(ContactsContract.Data.DATA4, " Call " + i.number)
                            .withValue(ContactsContract.Data.SYNC1, i.number)
                            .withValue(ContactsContract.Data.SYNC2, i.id)
                            .build()
                    )
                    try {
                        val results = contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                        if (results.isEmpty())
                            return
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    i.isSynced = true
                }
            }
        }
        private fun deleteAContact(contentResolver: ContentResolver, deleteId: String) {
            val ops = ArrayList<ContentProviderOperation>()
            ops.add(
                ContentProviderOperation.newDelete(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI, true))
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(deleteId)).build())
            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }
        private fun updateRawContact(contentResolver: ContentResolver, rawContactId: String, number: String, name: String) {
            val ops = ArrayList<ContentProviderOperation>()
            ops.add(
                ContentProviderOperation.newUpdate(
                    addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true)
                )
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + " =? ", arrayOf(rawContactId))
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )
            ops.add(
                ContentProviderOperation.newUpdate(
                    addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true)
                )
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + " =? ", arrayOf(rawContactId))
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )
            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }
        fun deleteAllSystemContact(context: Context) {
            val contentResolver = context.contentResolver
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newDelete(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI, true))
                    .withSelection(ContactsContract.RawContacts._ID + ">? ", arrayOf("-1")).build()
            )

            ops.add(
                ContentProviderOperation.newDelete(addCallerIsSyncAdapterParameter(ContactsContract.RawContacts.CONTENT_URI, true))
                    .withSelection(ContactsContract.RawContacts._ID + ">? ", arrayOf("-1")).build()
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }
    }
}