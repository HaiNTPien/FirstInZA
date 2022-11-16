package com.example.listwithanimation.models

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.`interface`.Contact
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
    var isSynced: Boolean = false
)

class ListContactModel : Contact.Model {
    var list = mutableListOf<ContactModel>()


    @SuppressLint("Range", "Recycle")
    override fun retrievePhoneContact(context: Context, packageManager: PackageManager) {
//        Log.d(" Phase ", " 1 ")
        val cr = context.contentResolver
//        ContactManager.queryContactLogging(cr)
        updateList(ContactManager.queryContact(cr).distinctBy { it.id })
        ContactManager.syncContact(cr, getListContactDistinct(false))

        Log.d(" Phase ", " 2 ")
        ContactManager.queryContactLogging(cr)

    }

    fun updateList(newList : List<ContactModel>) {
        for(i in list.size - 1 downTo  0) {
            if(!itemExistInList(list[i], newList)){
                list.removeAt(i)
            }
        }
        for(i in newList) {
            if(!itemExistInList(i, list)){
                list.add(i)
            }
        }
    }
    private fun itemExistInList(item: ContactModel, list: List<ContactModel>) : Boolean {
        return list.any { it.displayName == item.displayName && it.number == item.number }
    }
    override fun getListContact(withSectionLabel: Boolean): List<ContactModel> {
        val returnList = list.sortedBy{
            it.displayName.lowercase(
                Locale.ROOT
            )
        }.toMutableList()
        return if(withSectionLabel) {
            addLabelSection(returnList).toList()
        }else {
            returnList.toList()
        }
    }

    override fun getListContactDistinct(withSectionLabel: Boolean): List<ContactModel> {
        val returnList = list.distinctBy { it.id }.sortedBy{
            it.displayName.lowercase(
                Locale.ROOT
            )
        }.toMutableList()
        return if(withSectionLabel) {
            addLabelSection(returnList).toList()
        }else {
            returnList.toList()
        }
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
                lst.add(i, ContactModel("", "", "", "", "", "",  previousLabel, ""))
            } else {
                if (previousLabel != lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)) {
                    previousLabel = lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)
                    lst.add(i, ContactModel("", "", "", "", "", "",  previousLabel, ""))
                } else {
                    lst.removeAt(i + 1)
                    lst.add(i, ContactModel("", "", "", "", "", "",  previousLabel, ""))
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