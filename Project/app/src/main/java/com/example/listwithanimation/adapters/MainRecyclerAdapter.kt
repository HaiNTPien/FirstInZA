package com.example.listwithanimation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.listwithanimation.R
import com.example.listwithanimation.databinding.ContentItemLayoutBinding
import com.example.listwithanimation.databinding.TitleLayoutBinding


class MainRecyclerAdapter : RecyclerView.Adapter<ViewHolder>() {

    private var list = mutableListOf<ItemModel>()

    var onItemClickCallback: ((Int) -> Unit)? = null


    fun addAll(lst: List<ItemModel>) {
        list.clear()
        list.addAll(lst.reversed())
        notifyItemRangeInserted(0, list.size)
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return when (p1) {
            0 -> {
                val titleLayoutBinding: TitleLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(p0.context), R.layout.title_layout, p0, false
                )
                TitleLayoutViewHolder(titleLayoutBinding)
            }
            1 -> {
                val contentItemLayoutBinding: ContentItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(p0.context), R.layout.content_item_layout, p0, false
                )
                ContentLayoutViewHolder(contentItemLayoutBinding)
            }
            else -> {
                val contentItemLayoutBinding: ContentItemLayoutBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(p0.context), R.layout.content_item_layout, p0, false
                )
                ContentLayoutViewHolder(contentItemLayoutBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.apply {
            when (p0) {
                is TitleLayoutViewHolder -> {
                    p0.bind(list[p1])
                }
                is ContentLayoutViewHolder -> {
                    p0.bind(list[p1])
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = list[position].type

    inner class TitleLayoutViewHolder(private var binding: TitleLayoutBinding) :
        ViewHolder(binding.root) {
        fun bind(item: ItemModel) {
            binding.tvTitle.text = item.title
        }

    }

    inner class ContentLayoutViewHolder(private var binding: ContentItemLayoutBinding) :
        ViewHolder(binding.root) {
        fun bind(item: ItemModel) {

            if (item.isPivot) {
                binding.root.layoutParams.height = 0
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

    fun getCurrentDataSet() = list
}

data class ItemModel(
    var id: Int,
    var title: String? = null,
    var name: String? = null,
    var description: String? = null,
    var type: Int = 0,
    var isPivot: Boolean = false
)