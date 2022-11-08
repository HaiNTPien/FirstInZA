package com.example.listwithanimation.presenters

import android.content.Context
import android.content.pm.PackageManager
import com.example.listwithanimation.`interface`.Contact
import kotlinx.coroutines.runBlocking

class ContactPresenters(
    private var mainView: Contact.View?,
    private val model: Contact.Model): Contact.Presenters {
    override fun onClickSubmitList(context: Context, packageManager: PackageManager) {
//        if(model.getListContact(true).isEmpty()) {
            runBlocking {
                model.retrievePhoneContact(context, packageManager)
            }
            mainView?.setList(model.getListContactDistinct(true))
//        }else {
//            mainView?.setList(model.getListContact(true))
//        }
    }
}