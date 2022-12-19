package com.example.listwithanimation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.listwithanimation.databinding.LoggingItemLayoutBinding
import com.example.listwithanimation.models.ActionContact
import com.example.listwithanimation.models.LogModel

class LoggingAdapter: BaseAdapter<LogModel>() {
    var onItemClickCallback: (() -> Unit)? = null
    override fun areItemTheSame(oldItem: LogModel, newItem: LogModel): Boolean {
        return oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                val loggingItemLayoutBinding: LoggingItemLayoutBinding = LoggingItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return LoggingViewHolder(loggingItemLayoutBinding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LoggingViewHolder).bind(getDataSource()[position])
    }

    inner class LoggingViewHolder(private var binding: LoggingItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogModel) {
            binding.root.translationZ = 4F
            binding.tvAction.text = ActionContact.getEnum(item.action)
            binding.tvDescription.text = item.message.toString()
            binding.root.setOnClickListener {
                onItemClickCallback?.invoke()
            }
        }
    }
    fun submitList(newList: List<LogModel>) {
        setList(newList)
    }

    fun configRecyclerView(rv: RecyclerView) {
        setLinearLayoutManager(rv)
    }
}