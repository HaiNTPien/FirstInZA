package com.example.listwithanimation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.listwithanimation.databinding.ContentItemLayoutBinding
import com.example.listwithanimation.databinding.TitleLayoutBinding
import com.example.listwithanimation.models.ItemModel


class MainRecyclerAdapter : BaseAdapter<ItemModel>() {
    var onItemClickCallback: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return when (p1) {
            0 -> {
                val titleLayoutBinding: TitleLayoutBinding = TitleLayoutBinding.inflate(
                    LayoutInflater.from(p0.context), p0, false
                )
                TitleLayoutViewHolder(titleLayoutBinding)
            }
            else -> {
                val contentItemLayoutBinding: ContentItemLayoutBinding = ContentItemLayoutBinding.inflate(
                    LayoutInflater.from(p0.context), p0, false
                )
                ContentLayoutViewHolder(contentItemLayoutBinding)
            }
        }
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.apply {
            when (p0) {
                is TitleLayoutViewHolder -> {
                    p0.bind(getDataSource()[p1])
                }
                is ContentLayoutViewHolder -> {
                    p0.bind(getDataSource()[p1])
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = getDataSource()[position].type

    inner class TitleLayoutViewHolder(private var binding: TitleLayoutBinding) :
        ViewHolder(binding.root) {
        fun bind(item: ItemModel) {
            binding.tvTitle.text = item.title
            binding.root.translationZ = 4F
        }

    }

    inner class ContentLayoutViewHolder(private var binding: ContentItemLayoutBinding) :
        ViewHolder(binding.root) {
        fun bind(item: ItemModel) {
            binding.root.translationZ = 4F

            if (item.isPivot) {
                binding.root.layoutParams.height = 1
            } else {
                binding.root.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT
                )
                binding.tvName.text = item.name
                binding.tvDescription.text = item.description

                binding.root.setOnClickListener {
                    onItemClickCallback?.invoke(adapterPosition)
                }

            }
        }
    }

    override fun areItemTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
        return oldItem.id == newItem.id
    }

    fun configRecyclerView(rv: RecyclerView) {
        setLinearLayoutManager(rv)
    }

    fun addAllItem(lst: List<ItemModel>) {
        setListImmediately(lst)
    }
    fun removeItem(position: Int){
        removeOne(position)
    }

    fun addItem(position: Int, item: ItemModel){
        addOne(position, item)
    }

    fun moveItem(startPosition: Int, endPosition: Int) {
        moveOne(startPosition, endPosition)

    }

    fun submitList(newList : List<ItemModel>) {
        setList(newList)
    }
}
