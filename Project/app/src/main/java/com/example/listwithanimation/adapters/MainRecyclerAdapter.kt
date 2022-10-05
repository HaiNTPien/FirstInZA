package com.example.listwithanimation.adapters

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.listwithanimation.R
import com.example.listwithanimation.databinding.ContentItemLayoutBinding
import com.example.listwithanimation.databinding.TitleLayoutBinding
import java.util.*
import kotlin.math.abs


class MainRecyclerAdapter() : RecyclerView.Adapter<ViewHolder>() {

    var list = mutableListOf<ItemModel>()
    private lateinit var context: Context
    private var lastPosition = -1
    var onItemClickCallback: ((Int) -> Unit)? = null

    fun addAll(lst: List<ItemModel>) {
//        differ.submitList(lst.reversed())
        list.clear()
        list.addAll(lst.reversed())
        notifyItemRangeInserted(0, list.size)
    }
    fun setActivityContext(context: Context) {
        this.context = context
    }



    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return when (p1) {
            0 -> {
                val titleLayoutBinding: TitleLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(p0.context), R.layout.title_layout, p0, false)
                TitleLayoutViewHolder(titleLayoutBinding)
            }
            else -> {
                val contentItemLayoutBinding: ContentItemLayoutBinding = DataBindingUtil.inflate(LayoutInflater.from(p0.context), R.layout.content_item_layout, p0, false)
                ContentLayoutViewHolder(contentItemLayoutBinding)
            }
        }
    }

    override fun getItemCount(): Int {
//        return differ.currentList.size
        return list.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.apply {
                when (p0) {
                        is TitleLayoutViewHolder -> {
                            p0.bind(list[p1])
//                           p0.bind(differ.currentList[p1])
                        }
                        is ContentLayoutViewHolder -> {
                            p0.bind(list[p1])
//                            p0.bind(differ.currentList[p1])
                        }
                    }
                }
//            setAnimation(p0.itemView, p1)
        }
    fun setAnimation(viewToAnimate: View) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            animation.duration = 500
    }
    fun setRemovalAnimation(viewToAnimate: View) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right)
            animation.duration = 500
            viewToAnimate.startAnimation(animation)
    }
    override fun getItemViewType(position: Int): Int = list[position].type

    inner class TitleLayoutViewHolder(private var binding: TitleLayoutBinding) : ViewHolder(binding.root) {
        fun bind(item: ItemModel) {
            binding.tvTitle.text = item.title
        }

    }

    inner class ContentLayoutViewHolder(private var binding: ContentItemLayoutBinding) : ViewHolder(binding.root) {
        fun bind(item: ItemModel) {
            binding.tvName.text = item.name
            binding.tvDescription.text = item.description

            binding.root.setOnClickListener {
                onItemClickCallback?.invoke(adapterPosition)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ItemModel>(){
        override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return  oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, differCallback)

    fun getCurrentDataSet() = list
}

data class ItemModel(var id: Int, var title: String? = null, var name: String? = null, var description: String ?= null, var type :Int = 0)