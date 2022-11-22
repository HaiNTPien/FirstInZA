package com.example.listwithanimation.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.`interface`.Contact
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.get
import com.example.listwithanimation.helpers.SharePreferences.set
import com.google.gson.Gson
import java.util.*

data class ContactModel(
    var id: String,
    var displayName: String,
    var number: String,
    var type: String,
    var accountName: String,
    var accountType: String,
    var sectionLabel: String? = null,
    var rawContactId: String,
    var isSynced: Boolean = false,
    var isSyncLogged: Boolean = false
)

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
                else -> "Unknown"
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
//        Log.d(" Phase ", " 1 ")
        val cr = context.contentResolver
//        ContactManager.queryContactLogging(cr)
        val sharePref = SharePreferences.defaultPrefs(context)
        val lst: List<ContactModel> =
            ContactManager.queryContact(cr, context).first.distinctBy { it.id }
//        }else {
//            lst = Gson().fromJson(sharePref.getString("contacts", null), Array<ContactModel>::class.java).toList()
//            Log.d("contact data ", "load from share pref")
//        }
        updateList(lst)
        ContactManager.syncContact(context, getListContactDistinct(false))
        if (sharePref.getString("contacts", null) == null) {
            sharePref["contacts"] = Gson().toJson(lst)
        }
        if (sharePref.getString("dataLog", null) == null) {
            sharePref["dataLog"] = Gson().toJson(listOf<LogModel>())
        }
//        ContactManager.logChangeInContact(lst.toMutableList(), context)
//        Log.d("Phase ", " 2 ")
//        ContactManager.queryContactLogging(cr)

    }

    fun updateList(newList: List<ContactModel>) {
        for (i in list.size - 1 downTo 0) {
            if (!itemExistInList(list[i], newList)) {
                list.removeAt(i)
            }
        }
        for (i in newList) {
            if (!itemExistInList(i, list)) {
                list.add(i)
            }
        }
    }

    private fun itemExistInList(item: ContactModel, list: List<ContactModel>): Boolean {
        return list.any { it.displayName == item.displayName && it.number == item.number }
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
                lst.add(i, ContactModel("", "", "", "", "", "", previousLabel, ""))
            } else {
                if (previousLabel != lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)) {
                    previousLabel = lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)
                    lst.add(i, ContactModel("", "", "", "", "", "", previousLabel, ""))
                } else {
                    lst.removeAt(i + 1)
                    lst.add(i, ContactModel("", "", "", "", "", "", previousLabel, ""))
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