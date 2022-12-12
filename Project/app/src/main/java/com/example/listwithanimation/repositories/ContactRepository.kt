package com.example.listwithanimation.repositories

import android.content.Context
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.set
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.LogModel
import com.example.listwithanimation.utils.ListUtil
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking

class ContactRepository {

    companion object {
        private var INSTANCE: ContactRepository? = null
        fun getInstance() = INSTANCE
            ?: ContactRepository().also {
                INSTANCE = it
            }
    }


    fun getAllContact(context: Context): List<ContactModel> {
        return ListUtil.addLabelSection(retrievePhoneContact(context).toMutableList())
    }

    private fun retrievePhoneContact(context: Context): List<ContactModel> {
        val cr = context.contentResolver
        val lst: List<ContactModel> = ContactManager.queryContact(cr).first.distinctBy { it.id }
        val sharePref = SharePreferences.defaultPrefs(context)
        if (sharePref.getString("contacts", null) == null) {
            sharePref["contacts"] = Gson().toJson(lst)
        }
        if (sharePref.getString("dataLog", null) == null) {
            sharePref["dataLog"] = Gson().toJson(listOf<LogModel>())
        }
        return lst
    }
}