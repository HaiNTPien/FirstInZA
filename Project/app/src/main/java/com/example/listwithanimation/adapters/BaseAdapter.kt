package com.example.listwithanimation.adapters

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

abstract class BaseAdapter<T> : RecyclerView.Adapter<ViewHolder>() {

    private var list = mutableListOf<T>()

    abstract fun areItemTheSame(oldItem: T, newItem: T): Boolean

    fun addAll(lst: List<T>) {
        list.clear()
        list.addAll(lst.reversed())
        notifyItemRangeInserted(0, list.size)
    }

    fun removeOne(position: Int){
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun addOne(position: Int, item: T){
        list.add(position, item)
        notifyItemInserted(position)
    }

    fun moveOne(startPosition: Int, endPosition: Int) {
        val item = list.removeAt(startPosition)
        list.add(endPosition, item)
        notifyItemMoved(startPosition, endPosition)
    }

    fun submitDataList(newList : List<T>, fPosition: Int, lPosition: Int) {
        var isChangedDataInRange = false
        val oldList = list
        val oldHashSet = list.subList(fPosition, lPosition * 2).toHashSet()
        val newHashSet = newList.subList(fPosition, lPosition * 2).toHashSet()
        list = newList.toMutableList()
        for (i in lPosition downTo fPosition) {
            if (!newHashSet.contains(oldList[i])) {
                notifyItemRemoved(i)
                oldList.removeAt(i)
                isChangedDataInRange = true
            }
        }
        for (i in fPosition..lPosition) {
            if (!oldHashSet.contains(newList[i])) {
                notifyItemInserted(i)
                oldList.add(i, newList[i])
                isChangedDataInRange = true
            }
        }

        for (i in fPosition..lPosition) {
            if (!areItemTheSame(oldList[i], newList[i]) && oldHashSet.contains(newList[i])) {
                isChangedDataInRange = true
                val oldPosition = oldList.indexOf(newList[i])
                oldHashSet.remove(newList[oldPosition])
                notifyItemMoved(oldPosition, i)

            }
        }
        if (!isChangedDataInRange) {
            Handler().postDelayed({
                notifyDataSetChanged()
            }, 50)
        }
    }


    override fun getItemCount(): Int = list.size

    protected fun getDataSource() = list
}

