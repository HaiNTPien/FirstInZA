package com.example.listwithanimation.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.helpers.SharePreferences
import com.example.listwithanimation.helpers.SharePreferences.get
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.LogModel
import com.example.listwithanimation.repositories.ContactRepository
import com.google.gson.Gson

class ContactViewModels: ViewModel() {

    private var repository = ContactRepository.getInstance()
    private var _listContact = MutableLiveData<List<ContactModel>>()
    val listContact : LiveData<List<ContactModel>> = _listContact

    private var _listLog = MutableLiveData<List<LogModel>>()
    val listLog : LiveData<List<LogModel>> = _listLog

    fun fetchContact(context: Context) {
        val lst = repository.getAllContact(context = context)
        _listContact.postValue(lst)
    }
    fun fetchLog(context: Context) {
        val sharePref = SharePreferences.defaultPrefs(context)
        val loggingList = sharePref["dataLog", ""]
        if (Gson().fromJson(loggingList, Array<LogModel>::class.java) == null) {
            _listLog.postValue(listOf())
        } else {
            _listLog.postValue(
                Gson().fromJson(loggingList, Array<LogModel>::class.java).toList()
            )
        }
    }

    fun notifyContactChanged(context: Context){
        val lst = ContactManager.queryContact(context.contentResolver)
//                sharePref["contacts"] = Gson().toJson(lst)
//        if(!ContactManager.syncContact(context = this@SyncService, lst)) {
        if (lst.second) {
            Log.d(" Handler ", " Sync contact")
            ContactManager.syncContact(context = context, lst.first.distinctBy { it.id })
        } else {
            Log.d(" Handler ", " Logging ")
            ContactManager.logChangeInContact(
                context = context,
                newList = lst.first.distinctBy { it.id }.toMutableList()
            )
        }
    }
}