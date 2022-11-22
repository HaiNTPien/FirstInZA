package com.example.listwithanimation.view

import android.Manifest
import android.accounts.Account
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listwithanimation.R
import com.example.listwithanimation.SyncService
import com.example.listwithanimation.`interface`.Contact
import com.example.listwithanimation.adapters.ContactAdapter
import com.example.listwithanimation.databinding.ActivityContactBinding
import com.example.listwithanimation.databinding.FragmentContactBinding
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.ListContactModel
import com.example.listwithanimation.presenters.ContactPresenters


class ContactFragment : Fragment(), Contact.View {

    var presenters: ContactPresenters? = null

    private val requestReadContactsPermissions = 100
    private val adapter : ContactAdapter by lazy {
        ContactAdapter()
    }
    private val account = Account("ABC", "vnd.com.app.call")
    private lateinit var binding: FragmentContactBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenters = ContactPresenters(this, ListContactModel())
//        createAccount()
        initListContact()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

//    fun createAccount(){
//        val accountManager = AccountManager.get(this)
//        val success = accountManager.addAccountExplicitly(account, null, null)
//        val extras = intent.extras
//        if (extras != null) {
//            if (success) {
//                val response: AccountAuthenticatorResponse? = extras.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
//                val result = Bundle()
//                result.putString(AccountManager.KEY_ACCOUNT_NAME, "ABC")
//                result.putString(AccountManager.KEY_ACCOUNT_TYPE, "vnd.com.app.call")
//                response?.onResult(result)
//            }
//            finish()
//        }
//    }

//    @SuppressLint("Range")
//    fun handleBackFromSystemContact(){
//        val dataStr = intent.dataString
//        if (dataStr != null){
//            Log.d(" Data return ", dataStr.toString())
//            Toast.makeText(this, dataStr.toString(), Toast.LENGTH_SHORT).show()
//            val dataId = dataStr.split("/")
////            Toast.makeText(this, adapter.getNumberByRawContactID(dataId.last()), Toast.LENGTH_SHORT).show()
//            val cursor = this.contentResolver.query(
//                dataStr.toUri(), arrayOf(
//                    ContactsContract.CommonDataKinds.Phone.NUMBER,
//                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
//                    ContactsContract.Data.SYNC1,
//                    ContactsContract.Data.SYNC2,
//                    ContactsContract.Data.SYNC3,
//                    ContactsContract.Data.SYNC4
//                ),
//                null, null, null
//            )
//            cursor?.moveToFirst()
//
//            val number =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ))
//            val name =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME ))
//            val s1 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC1 ))
//            val s2 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC2 ))
//            val s3 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC3 ))
//            val s4 =
//                cursor?.getString(cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.SYNC4 ))
//
////            Toast.makeText(this, "$name $number $s1 $s2 $s3 $s4", Toast.LENGTH_SHORT).show()
//            cursor?.close()
//        }
//
//    }



    private fun initListContact() {
        val linearLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.rvContact.apply {
            layoutManager = linearLayoutManager
            this@ContactFragment.adapter.onItemClickCallback = {
                ContactManager.deleteAllSystemContact(context)
            }
            this@ContactFragment.adapter.configRecyclerView(this)
        }
        updateContactList(requireActivity(), requireActivity().packageManager)

    }

    override fun move(firstPosition: Int, secondPosition: Int) {

    }

    override fun removeOne(position: Int) {

    }

    override fun addOne(item: ContactModel) {

    }

    override fun setList(lst: List<ContactModel>) {
        binding.rvContact.post {
            adapter.submitList(lst)
        }
    }

    fun updateContactList(context: Context, pm: PackageManager){
        presenters?.onClickSubmitList(context, pm)
    }

}