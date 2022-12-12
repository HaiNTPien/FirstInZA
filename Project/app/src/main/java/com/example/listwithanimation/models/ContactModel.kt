package com.example.listwithanimation.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.set
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class ContactModel(
    var id: String,
    var rawContactId: String,
    var displayName: String,
    var number: MutableList<PhoneInContactModel>,
    var type: String,
    var sectionLabel: String? = null,
    var isSyncLogged: Boolean = false
)

data class PhoneInContactModel(
    var id: String,
    var number: String,
    var phoneLabel: String,
    var accountName: String,
    var accountType: String,
    var isSynced: Boolean = false
) {
    companion object {
        fun getTypeName(type: String): String {
            return when (type) {
                "1" -> "Home"
                "2" -> "Mobile"
                "3" -> "Work"
                else -> "Others"
            }
        }
    }
}

enum class ActionContact {
    ADD_CONTACT,
    DELETE_CONTACT,
    UPDATE_CONTACT,
    SYNC_CONTACT;
    companion object {
        fun getEnum(value: ActionContact): String {
            return when(value) {
                ADD_CONTACT -> "Add Contact"
                DELETE_CONTACT -> "Delete Contact"
                UPDATE_CONTACT -> "Update Contact"
                SYNC_CONTACT -> "Sync Contact"
            }
        }
    }
}

data class LogModel(
    var id: String? = null,
    var action: ActionContact,
    var contactId: String,
    var message: String? = null
)