package com.example.listwithanimation.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.`interface`.Contact
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

class ListContactModel : Contact.Model {

    var list = mutableListOf<ContactModel>()

    @SuppressLint("Range", "Recycle")
    override fun retrievePhoneContact(context: Context, packageManager: PackageManager) {
        val cr = context.contentResolver
        val sharePref = SharePreferences.defaultPrefs(context)
        val lst: List<ContactModel> = ContactManager.queryContact(cr).first.distinctBy { it.id }
        list = lst.toMutableList()
        if (sharePref.getString("contacts", null) == null) {
            sharePref["contacts"] = Gson().toJson(lst)
        }
        if (sharePref.getString("dataLog", null) == null) {
            sharePref["dataLog"] = Gson().toJson(listOf<LogModel>())
        }

    }

    override fun getListContact(withSectionLabel: Boolean): List<ContactModel> {
        val returnList = list.sortedBy {
            it.displayName.lowercase(
                Locale.ROOT
            )
        }.toMutableList()
        return if (withSectionLabel) {
            addLabelSection(returnList).toList()
        } else {
            returnList.toList()
        }
    }

    override fun getListContactDistinct(withSectionLabel: Boolean): List<ContactModel> {
        val returnList = list.distinctBy { it.id }.sortedBy {
            it.displayName.lowercase(
                Locale.ROOT
            )
        }.toMutableList()
        return if (withSectionLabel) {
            addLabelSection(returnList).toList()
        } else {
            returnList.toList()
        }
    }

    override fun getContactByID(id: String): ContactModel? {
        val position = list.withIndex().firstOrNull { id == it.value.id }?.index ?: -1
        return if (position != -1) list[position] else
            null
    }

    private fun String.onlyLetters() = all { it.isLetter() }

    private fun addLabelSection(lst: MutableList<ContactModel>): List<ContactModel> {
        var previousLabel = ""
        var countSpecialName = 0
        for (i in lst.size - 1 downTo 0) {
            if (!lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT).onlyLetters()) {
                countSpecialName++
                if (previousLabel == "#") {
                    lst.removeAt(i + 1)
                }
                previousLabel = "#"
                lst.add(i, ContactModel("", "","", mutableListOf(), "", previousLabel))
            } else {
                if (previousLabel != lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)) {
                    previousLabel = lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)
                    lst.add(i, ContactModel("", "","", mutableListOf(), "",  previousLabel))
                } else {
                    lst.removeAt(i + 1)
                    lst.add(i, ContactModel("", "" ,"", mutableListOf(), "", previousLabel))
                }
            }

        }
        return if (countSpecialName > 0) {
            val secondList = lst.subList(0, countSpecialName + 1)
            val firstList = lst.subList(countSpecialName + 1, lst.size)
            firstList + secondList
        } else {
            lst
        }
    }
}