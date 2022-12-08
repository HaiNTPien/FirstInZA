package com.example.listwithanimation.view

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listwithanimation.adapters.ContactAdapter
import com.example.listwithanimation.databinding.FragmentContactBinding
import com.example.listwithanimation.helpers.ContactManager
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.viewmodels.ContactViewModels


class ContactFragment : Fragment(){

    private lateinit var viewModel : ViewModel
    private val adapter : ContactAdapter by lazy {
        ContactAdapter()
    }
    private lateinit var binding: FragmentContactBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[ContactViewModels::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListContact()
    }


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

    fun move(firstPosition: Int, secondPosition: Int) {

    }

    fun removeOne(position: Int) {

    }

    fun addOne(item: ContactModel) {

    }

    fun setList(lst: List<ContactModel>) {
        binding.rvContact.post {
            adapter.submitList(lst)
        }
    }

    fun updateContactList(context: Context, pm: PackageManager){
    }

}