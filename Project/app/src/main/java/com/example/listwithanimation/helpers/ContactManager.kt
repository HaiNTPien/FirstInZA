package com.example.listwithanimation.helpers

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import android.util.Log
import com.example.listwithanimation.utils.APP_ACCOUNT_NAME
import com.example.listwithanimation.utils.APP_ACCOUNT_TYPE
import com.example.listwithanimation.utils.APP_MIMETYPE
import com.example.listwithanimation.helpers.SharePreferences.get
import com.example.listwithanimation.helpers.SharePreferences.set
import com.example.listwithanimation.models.ActionContact
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.LogModel
import com.example.listwithanimation.models.PhoneInContactModel
import com.google.gson.Gson


class ContactManager {
    companion object {
        @SuppressLint("Range", "Recycle")
        fun queryContact(contentResolver: ContentResolver): Pair<MutableList<ContactModel>, Boolean> {
            val contactLst = mutableListOf<ContactModel>()
            val queryResultLst = mutableListOf<ContactModel>()
            val removeRawDataLst = mutableListOf<Triple<String, String, String>>()
            var needSyncOperation = false
            val removeContactLst = mutableListOf<String>()
            var cur: Cursor? = null
            try {
                cur = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )
            }catch (_: Exception) {

            }

            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id =
                        cur.getString(cur.getColumnIndex(RawContacts.CONTACT_ID)) + ""
                    val name =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) + ""
                    val number =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + ""
                    val type =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) + ""
                    val accountName =
                        cur.getString(cur.getColumnIndex(RawContacts.ACCOUNT_NAME)) + ""
                    val accountType =
                        cur.getString(cur.getColumnIndex(RawContacts.ACCOUNT_TYPE)) + ""
                    val rawContactId =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)) + ""
                    val phoneId =
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)) + ""
                    val positionInList = itemExistInListContact(id, queryResultLst)
                    if (positionInList != -1) {
                        if (accountType == APP_ACCOUNT_TYPE) {
                            queryResultLst[positionInList].number.add(PhoneInContactModel(phoneId, number, type, accountName, accountType))
                        }else {
                            queryResultLst[positionInList].number.add(0, PhoneInContactModel(phoneId, number, type, accountName, accountType))
                        }
                    } else {
                        var phoneTypeTemp = ""
                        when (accountType) {
                            "vnd.sec.contact.phone" -> {
                                phoneTypeTemp = "From Phone"

                            }
                            "vnd.sec.contact.sim" -> {
                                phoneTypeTemp = "From Sim 1"

                            }
                            "vnd.sec.contact.sim2" -> {
                                phoneTypeTemp = "From Sim 2"

                            }
                            "com.google" -> {
                                phoneTypeTemp = "From Google Account : $accountName"

                            }
                            "com.samsung.android.exchange" -> {
                                phoneTypeTemp = "From Microsoft Account : $accountName"

                            }
                            "com.osp.app.signin" -> {
                                phoneTypeTemp = "From Samsung Account : $accountName"
                            }
                            APP_ACCOUNT_TYPE -> {
                                phoneTypeTemp = "Custom type"

                            }
                        }
                        queryResultLst.add(ContactModel(id, rawContactId, name, mutableListOf(PhoneInContactModel(phoneId, number, type, accountName, accountType)), phoneTypeTemp))
                    }
                }
            }

            for (contact in queryResultLst) {
                val removeCustomTypeRowLst = mutableListOf<PhoneInContactModel>()
                for (phoneNumber in contact.number) {
                    val existItemPosition = getPositionByNumber(phoneNumber.number, contact.number.toList())
                    if (existItemPosition != -1 && contact.number[existItemPosition].id != phoneNumber.id) {
                        if (phoneNumber.accountName == APP_ACCOUNT_NAME && phoneNumber.accountType == APP_ACCOUNT_TYPE) {
                            contact.number[existItemPosition].isSynced = true
                            removeCustomTypeRowLst.add(phoneNumber)
                        }
                    } else {
                        if (phoneNumber.accountName == APP_ACCOUNT_NAME && phoneNumber.accountType == APP_ACCOUNT_TYPE) {
                            removeCustomTypeRowLst.add(phoneNumber)
                            removeRawDataLst.add(Triple(phoneNumber.number, phoneNumber.id, contact.displayName))
                        }
                    }
                }
                for (removeItem in removeCustomTypeRowLst) {
                    contact.number.remove(removeItem)
                }
                if (contact.number.isEmpty()) {
                    removeContactLst.add(contact.id)
                }
                contactLst.add(contact)
            }

            if (removeRawDataLst.isNotEmpty()) {
                for (removeItem in removeRawDataLst) {
                    deleteRawDataInContact(contentResolver, removeItem.first, removeItem.second)
                }
            }
            if (removeContactLst.isNotEmpty()) {
                for (i in removeContactLst) {
                    deleteAContact(contentResolver, i)
                }
            }
            for (contact in contactLst) {
                for (phoneNumber in contact.number) {
                    if (!phoneNumber.isSynced) {
                        needSyncOperation = true
                    }
                }
            }

            return Pair(contactLst, needSyncOperation)
        }

        private fun isNeedSyncContact(contactLst: MutableList<ContactModel>): Boolean {
            var needSyncOperation = false
            val hashSet = HashSet<PhoneInContactModel>()
            for (contact in contactLst) {
                hashSet.addAll(contact.number)
            }
            for(numberPhone in hashSet.toList()){
                if(!numberPhone.isSynced){
                    needSyncOperation = true
                    break
                }
            }
            return needSyncOperation
        }

        fun logChangeInContact(newList: MutableList<ContactModel>, context: Context) {
            val sharePref = SharePreferences.defaultPrefs(context)
            val oldLst: MutableList<ContactModel> = Gson().fromJson(sharePref["contacts", ""], Array<ContactModel>::class.java).toMutableList()
            val oldLog = Gson().fromJson(sharePref["dataLog", ""], Array<LogModel>::class.java).toMutableList()
            for (i in oldLst.size - 1 downTo 0) {
                val itemPositionInNewList = getPositionInListContactByRawContactId(oldLst[i].rawContactId, newList)
                if (itemPositionInNewList == -1) {
                    for (j in oldLst[i].number) {
                        oldLog.add(
                            LogModel(
                                id = null,
                                ActionContact.DELETE_CONTACT,
                                oldLst[i].id,
                                "Remove contact ${oldLst[i].displayName} with number phone: ${j.number}"
                            )
                        )
                    }
                    oldLst.removeAt(i)
                } else {
                    val removePhoneLst = mutableListOf<PhoneInContactModel>()
                    for (j in oldLst[i].number) {
                        if (itemExistInListPhone(
                                j.id,
                                newList[itemPositionInNewList].number
                            ) == -1
                        ) {
                            oldLog.add(
                                LogModel(
                                    id = null,
                                    ActionContact.DELETE_CONTACT,
                                    oldLst[i].id,
                                    "Remove contact ${oldLst[i].displayName} with number phone: ${j.number}"
                                )
                            )
                            removePhoneLst.add(j)
                        }
                    }
                    for (j in removePhoneLst) {
                        oldLst[i].number.remove(j)
                    }
                }
            }
            for (i in newList) {
                val position = getPositionInListContactByRawContactId(i.rawContactId, oldLst)
                if (position == -1) {
                    for (j in i.number) {
                        oldLog.add(
                            LogModel(
                                id = null,
                                ActionContact.ADD_CONTACT,
                                i.id,
                                "Add contact ${i.displayName} with number phone: ${j.number}"
                            )
                        )

                    }
                    oldLst.add(i)
                    if (!i.isSyncLogged) {
                        for (j in i.number) {
                            if (j.isSynced) {
                                oldLog.add(
                                    LogModel(
                                        null,
                                        ActionContact.SYNC_CONTACT,
                                        i.id,
                                        "Synced contact ${i.displayName} with number phone: ${j.number}"
                                    )
                                )
                            }
                        }
                        i.isSyncLogged = true
                    }
                } else {
                    if (oldLst[position].displayName != i.displayName) {
                        oldLog.add(
                            LogModel(
                                id = null,
                                ActionContact.UPDATE_CONTACT,
                                i.id,
                                "Update contact name from ${oldLst[position].displayName} to ${i.displayName}"
                            )
                        )
                    }
                    for (j in i.number) {
                        if (itemExistInListPhone(j.id, oldLst[position].number) == -1) {
                            oldLog.add(
                                LogModel(
                                    id = null,
                                    ActionContact.ADD_CONTACT,
                                    i.id,
                                    "Add contact ${i.displayName} with number phone: ${j.number}"
                                )
                            )
                            oldLog.add(
                                LogModel(
                                    null,
                                    ActionContact.SYNC_CONTACT,
                                    i.id,
                                    "Synced contact ${i.displayName} with number phone: ${j.number}"
                                )
                            )
                        } else {
                            if (oldLst[position].number[itemExistInListPhone(
                                    j.id,
                                    oldLst[position].number
                                )].number != j.number
                            ) {
                                oldLog.add(
                                    LogModel(
                                        id = null,
                                        ActionContact.UPDATE_CONTACT,
                                        j.id,
                                        "Update contact ${i.displayName} with new number phone: ${j.number}"
                                    )
                                )
                            }

                        }
                    }
                }
            }
            sharePref["dataLog"] = Gson().toJson(oldLog.toList())
            sharePref["contacts"] = Gson().toJson(newList.toList())
        }

        private fun itemExistInListContact(id: String, list: List<ContactModel>): Int {
            return list.withIndex().firstOrNull { id == it.value.id }?.index ?: -1
        }

        private fun getPositionInListContactByRawContactId(
            rawContactId: String,
            list: List<ContactModel>
        ): Int {
            return list.withIndex().firstOrNull { rawContactId == it.value.rawContactId }?.index
                ?: -1
        }

        private fun itemExistInListPhone(id: String, list: List<PhoneInContactModel>): Int {
            return list.withIndex().firstOrNull { id == it.value.id }?.index ?: -1
        }

        private fun getPositionByNumber(number: String, list: List<PhoneInContactModel>): Int {
            return list.withIndex().firstOrNull { number == it.value.number }?.index ?: -1
        }

        @SuppressLint("Range", "Recycle")
        fun queryContactLogging(contentResolver: ContentResolver) {
            val cur = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cur!!.count > 0) {
                while (cur.moveToNext()) {
                    Log.d(
                        " Phone _ID",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)) + " "
                    )
                    Log.d(
                        " Phone CONTACT_ID",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) + " "
                    )
                    Log.d(
                        " Phone DISPLAY_NAME",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) + " "
                    )
                    Log.d(
                        " Phone NUMBER",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + " "
                    )
                    Log.d(
                        " Phone MIMETYPE",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.MIMETYPE))
                    )
                    Log.d(
                        " Phone DATA1",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)) + " "
                    )
                    Log.d(
                        " Phone DATA2",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA2)) + " "
                    )
                    Log.d(
                        " Phone DATA3",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA3)) + " "
                    )
                    Log.d(
                        " Phone DATA4",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA4)) + " "
                    )
                    Log.d(
                        " Phone DATA5",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA5)) + " "
                    )
                    Log.d(
                        " Phone DATA6",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA6)) + " "
                    )
                    Log.d(
                        " Phone DATA7",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA7)) + " "
                    )
                    Log.d(
                        " Phone DATA8",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA8)) + " "
                    )
                    Log.d(
                        " Phone DATA9",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA9)) + " "
                    )
                    Log.d(
                        " Phone DATA10",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA10)) + " "
                    )
                    Log.d(
                        " Phone DATA11",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA11)) + " "
                    )
                    Log.d(
                        " Phone DATA12",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA12)) + " "
                    )
                    Log.d(
                        " Phone DATA13",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA13)) + " "
                    )
                    Log.d(
                        " Phone DATA14",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA14)) + " "
                    )
                    Log.d(
                        " Phone DATA15",
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA15)) + " "
                    )
                }
            }
            val cur2 =
                contentResolver.query(ContactsContract.Data.CONTENT_URI, null, null, null, null)
            if (cur2!!.count > 0) {
                while (cur2.moveToNext()) {
                    Log.d(
                        " Data _ID",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data._ID))
                    )
                    Log.d(
                        " Data MIMETYPE",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.MIMETYPE))
                    )
                    Log.d(
                        " Data DATA1",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA1)) + " "
                    )
                    Log.d(
                        " Data DATA2",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA2)) + " "
                    )
                    Log.d(
                        " Data DATA3",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA3)) + " "
                    )
                    Log.d(
                        " Data DATA4",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA4)) + " "
                    )
                    Log.d(
                        " Data DATA5",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA5)) + " "
                    )
                    Log.d(
                        " Data DATA6",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA6)) + " "
                    )
                    Log.d(
                        " Data DATA7",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA7)) + " "
                    )
                    Log.d(
                        " Data DATA8",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA8)) + " "
                    )
                    Log.d(
                        " Data DATA9",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA9)) + " "
                    )
                    Log.d(
                        " Data DATA10",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA10)) + " "
                    )
                    Log.d(
                        " Data DATA11",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA11)) + " "
                    )
                    Log.d(
                        " Data DATA12",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA12)) + " "
                    )
                    Log.d(
                        " Data DATA13",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA13)) + " "
                    )
                    Log.d(
                        " Data DATA14",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA14)) + " "
                    )
//                    Log.d(" Data DATA15", cur2.getString(cur2.getColumnIndex(ContactsContract.Data.DATA15)) + " ")
                    Log.d(
                        " Data CONTACT_ID",
                        cur2.getString(cur2.getColumnIndex(ContactsContract.Data.CONTACT_ID)) + " "
                    )
                    Log.d(
                        " Data RAW_CONTACT_ID", cur2.getString(
                            cur2.getColumnIndex(
                                ContactsContract.Data.RAW_CONTACT_ID
                            )
                        )
                    )
                }
            }
            val cur3 = contentResolver.query(
                RawContacts.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            if (cur3!!.count > 0) {
                while (cur3.moveToNext()) {
                    Log.d(
                        " RawContacts Name",
                        cur3.getString(cur3.getColumnIndex(RawContacts.ACCOUNT_NAME)) + " "
                    )
                    Log.d(
                        " RawContacts Type",
                        cur3.getString(cur3.getColumnIndex(RawContacts.ACCOUNT_TYPE)) + " "
                    )
                    Log.d(
                        " RawContacts CONTACT_ID", cur3.getString(
                            cur3.getColumnIndex(
                                RawContacts.CONTACT_ID
                            )
                        ) + " "
                    )
                    Log.d(
                        " RawContacts _ID",
                        cur3.getString(cur3.getColumnIndex(RawContacts._ID)) + " "
                    )
                    Log.d(
                        " RawContacts VERSION", cur3.getString(
                            cur3.getColumnIndex(
                                RawContacts.VERSION
                            )
                        ) + " "
                    )
                }
            }
            val cur4 =
                contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur4!!.count > 0) {
                while (cur4.moveToNext()) {
                    Log.d(
                        " Contacts _ID",
                        cur4.getString(cur4.getColumnIndex(ContactsContract.Contacts._ID)) + " "
                    )
                    Log.d(
                        " Contacts DISPLAY_NAME", cur4.getString(
                            cur4.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME
                            )
                        ) + " "
                    )
                    Log.d(
                        " Contacts NAME_RAW_CONTACT_ID", cur4.getString(
                            cur4.getColumnIndex(
                                ContactsContract.Contacts.NAME_RAW_CONTACT_ID
                            )
                        ) + " "
                    )
                    Log.d(
                        " Contacts NAME_RAW_CONTACT_ID", cur4.getString(
                            cur4.getColumnIndex(
                                ContactsContract.Contacts.NAME_RAW_CONTACT_ID
                            )
                        ) + " "
                    )
                }
            }
        }

        private fun addCallerIsSyncAdapterParameter(
            uri: Uri,
            isSyncOperation: Boolean = true
        ): Uri {
            return if (isSyncOperation) {
                uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build()
            } else uri
        }

        fun syncContact(context: Context, list: List<ContactModel>) {
            for (i in list) {
                for (j in i.number) {
                    val ops = ArrayList<ContentProviderOperation>()
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI))
                            .withValue(RawContacts.ACCOUNT_NAME, APP_ACCOUNT_NAME)
                            .withValue(RawContacts.ACCOUNT_TYPE, APP_ACCOUNT_TYPE)
                            .build()
                    )

                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                            )
                            .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                i.displayName
                            )
                            .build()
                    )
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(
                                ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            )
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, j.number)
                            .withValue(
                                ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                            )
                            .build()
                    )
                    ops.add(
                        ContentProviderOperation.newInsert(
                            addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI)
                        )
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(
                                ContactsContract.Data.MIMETYPE, APP_MIMETYPE
                            )
                            .withValue(ContactsContract.Data.DATA1, j.number)
                            .withValue(ContactsContract.Data.DATA4, " Call " + j.number)
                            .withValue(ContactsContract.Data.SYNC1, j.number)
                            .withValue(ContactsContract.Data.SYNC2, i.id)
                            .build()
                    )
                    try {
                        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        @SuppressLint("Range", "Recycle")
        private fun deleteRawDataInContact(
            contentResolver: ContentResolver,
            numberPhone: String,
            deleteId: String
        ) {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newDelete(
                    addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI,
                        true
                    )
                )
                    .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? OR " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                        arrayOf(deleteId, APP_MIMETYPE, numberPhone)
                    ).build()
            )
            ops.add(
                ContentProviderOperation.newDelete(
                    addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI,
                        true
                    )
                )
                    .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ? OR " + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                        arrayOf(deleteId, APP_MIMETYPE, numberPhone)
                    ).build()
            )
            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }

        private fun deleteAContact(contentResolver: ContentResolver, deleteId: String) {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newDelete(
                    addCallerIsSyncAdapterParameter(
                        RawContacts.CONTENT_URI,
                        true
                    )
                )
                    .withSelection(
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(deleteId)
                    ).build()
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }

        private fun updateRawContact(
            contentResolver: ContentResolver,
            rawContactId: String,
            number: String,
            name: String
        ) {
            val ops = ArrayList<ContentProviderOperation>()
            ops.add(
                ContentProviderOperation.newUpdate(
                    addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI)
                )
                    .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + " =? ",
                        arrayOf(rawContactId)
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )
            ops.add(
                ContentProviderOperation.newUpdate(
                    addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI)
                )
                    .withSelection(
                        ContactsContract.Data.RAW_CONTACT_ID + " =? ",
                        arrayOf(rawContactId)
                    )
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    )
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
                ContentProviderOperation.newDelete(
                    addCallerIsSyncAdapterParameter(
                        RawContacts.CONTENT_URI
                    )
                )
                    .withSelection(RawContacts._ID + ">? ", arrayOf("-1")).build()
            )

            ops.add(
                ContentProviderOperation.newDelete(
                    addCallerIsSyncAdapterParameter(
                        RawContacts.CONTENT_URI
                    )
                )
                    .withSelection(RawContacts._ID + ">? ", arrayOf("-1")).build()
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            } catch (e: RemoteException) {
                e.printStackTrace()
            } catch (e: OperationApplicationException) {
                e.printStackTrace()
            }
        }

        fun addNumberPhoneToContact(
            contentResolver: ContentResolver,
            rawContactId: String,
            number: String
        ) {
            val ops = ArrayList<ContentProviderOperation>()

            ops.add(
                ContentProviderOperation.newInsert(
                    addCallerIsSyncAdapterParameter(
                        ContactsContract.Data.CONTENT_URI
                    )
                )
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK
                    )
                    .build()
            )

            try {
                contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}