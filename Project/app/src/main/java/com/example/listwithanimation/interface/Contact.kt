package com.example.listwithanimation.`interface`

import android.content.Context
import android.content.pm.PackageManager
import com.example.listwithanimation.models.ContactModel

interface Contact {
    interface View {
        fun move(firstPosition: Int, secondPosition: Int)

        fun removeOne(position: Int)

        fun addOne(item: ContactModel)

        fun setList(lst: List<ContactModel>)
    }

    interface Presenters {
        fun onClickSubmitList(context: Context, packageManager: PackageManager)
    }
    interface Model {

        fun retrievePhoneContact(context: Context, packageManager: PackageManager)
        fun getListContact(withSectionLabel: Boolean): List<ContactModel>
        fun getListContactDistinct(withSectionLabel: Boolean): List<ContactModel>
        fun getContactByID(id: String): ContactModel?
    }
}