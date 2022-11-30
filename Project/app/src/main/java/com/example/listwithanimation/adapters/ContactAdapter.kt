package com.example.listwithanimation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.listwithanimation.R
import com.example.listwithanimation.databinding.ContactItemLayoutBinding
import com.example.listwithanimation.databinding.ContentItemLayoutBinding
import com.example.listwithanimation.databinding.SectionItemLayoutBinding
import com.example.listwithanimation.models.ContactModel
import com.example.listwithanimation.models.ItemModel
import com.example.listwithanimation.models.PhoneInContactModel

class ContactAdapter : BaseAdapter<ContactModel>() {
    var onItemClickCallback: ((ContactModel) -> Unit)? = null

    override fun areItemTheSame(oldItem: ContactModel, newItem: ContactModel): Boolean {
        return if(oldItem.id == "" && newItem.id == "") {
            true
        }else {
            oldItem.displayName == newItem.displayName && oldItem.id == newItem.id && oldItem.number == newItem.number
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {

                val sectionItemLayoutBinding: SectionItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context), R.layout.section_item_layout, parent, false
                )
                return SectionLayoutViewHolder(sectionItemLayoutBinding)
            }
            1 -> {

                val contactItemLayoutBinding: ContactItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context), R.layout.contact_item_layout, parent, false
                )
                return ContactLayoutViewHolder(contactItemLayoutBinding)
            }
            else -> {

                val contactItemLayoutBinding: ContactItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context), R.layout.contact_item_layout, parent, false
                )
                return ContactLayoutViewHolder(contactItemLayoutBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionLayoutViewHolder -> {
                (holder as SectionLayoutViewHolder).bind(getDataSource()[position])
            }
            is ContactLayoutViewHolder -> {
                (holder as ContactLayoutViewHolder).bind(getDataSource()[position])
            }
        }

    }

    inner class ContactLayoutViewHolder(private var binding: ContactItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactModel) {
            binding.root.translationZ = 4F
            binding.tvName.text = item.displayName
            var displayNumberPhone = ""
            for (i in 0 until item.number.size) {
                displayNumberPhone += if(i == item.number.size - 1) {
                    "${PhoneInContactModel.getTypeName(item.number[i].phoneLabel)}: ${item.number[i].number}"
                }else {
                    "${PhoneInContactModel.getTypeName(item.number[i].phoneLabel)}: ${item.number[i].number}\n"
                }
            }
            binding.tvPhoneNumber.text = displayNumberPhone
            binding.tvFrom.text = item.type
            binding.root.setOnClickListener {
                onItemClickCallback?.invoke(item)
            }

        }
    }

    inner class SectionLayoutViewHolder(private var binding: SectionItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactModel) {
            binding.root.translationZ = 4F
            binding.tvLabelSection.text = item.sectionLabel.toString()

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getDataSource()[position].sectionLabel == null) {
            1
        } else {
            0
        }
    }

    fun configRecyclerView(rv: RecyclerView) {
        setLinearLayoutManager(rv)
    }

    fun addAllItem(lst: List<ContactModel>) {
        setListImmediately(lst)
    }

    fun removeItem(position: Int) {
        removeOne(position)
    }

    fun addItem(position: Int, item: ContactModel) {
        addOne(position, item)
    }

    fun moveItem(startPosition: Int, endPosition: Int) {
        moveOne(startPosition, endPosition)

    }

    fun submitList(newList: List<ContactModel>) {
        setList(newList)
    }

}