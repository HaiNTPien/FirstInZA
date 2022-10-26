package com.example.listwithanimation.adapters

import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.listwithanimation.SlideInDownAnimator

abstract class BaseAdapter<T> : RecyclerView.Adapter<ViewHolder>() {

    private var list = mutableListOf<T>()
    private lateinit var recyclerViewLayoutManager: LinearLayoutManager

    abstract fun areItemTheSame(oldItem: T, newItem: T): Boolean

    protected fun addAll(lst: List<T>) {
        list.clear()
        list.addAll(lst.reversed())
        notifyItemRangeInserted(0, list.size)
    }

    protected fun removeOne(position: Int) {
        if (position in 0 until itemCount) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    protected fun addOne(position: Int, item: T) {
        if (position in 0 until itemCount) {
            list.add(position, item)
            notifyItemInserted(position)
        }

    }

    protected fun moveOne(startPosition: Int, endPosition: Int) {
        if (startPosition in 0 until itemCount && endPosition in 0 until itemCount) {
            val item = list.removeAt(startPosition)
            list.add(endPosition, item)
            notifyItemMoved(startPosition, endPosition)
        }

    }

    protected fun submitDataList(newList: List<T>) {
        val lPosition = recyclerViewLayoutManager.findLastVisibleItemPosition()
        var isChangedDataInRange = false
        if (newList.isEmpty()) {
            addAll(listOf())
            return
        } else {
            if (list.size > newList.size) {
                val oldSize = list.size
                list = list.subList(0, newList.size)
                notifyItemRangeRemoved(newList.size, oldSize)
            }
            val rightBoundIndexNewList = if (newList.isEmpty()) {
                0
            } else {
                if (newList.size <= lPosition) {
                    newList.size - 1
                } else {
                    when (lPosition) {
                        -1 -> 0
                        0 -> newList.size - 1
                        else -> lPosition
                    }
                }
            }
            val rightBoundIndexOldList = if (list.isEmpty()) {
                0
            } else {
                if (list.size <= lPosition) {
                    list.size - 1
                } else {
                    when (lPosition) {
                        -1 -> 0
                        else -> lPosition
                    }
                }
            }

            val oldList = list
            val oldHashSet = list.subList(0, if (oldList.isEmpty()) 0 else rightBoundIndexOldList + 1).toHashSet()
            val newHashSet = newList.subList(0, if (newList.isEmpty()) 0 else rightBoundIndexNewList + 1).toHashSet()
            list = newList.toMutableList()

            for (i in rightBoundIndexOldList downTo 0) {
                if (newHashSet.isNotEmpty() && oldList.isNotEmpty() && !newHashSet.contains(oldList[i])) {
                    oldList.removeAt(i)
                    notifyItemRemoved(i)
                    isChangedDataInRange = true
                }
            }
            for (i in 0..rightBoundIndexNewList) {
                if (oldHashSet.isNotEmpty() && newList.isNotEmpty() && !oldHashSet.contains(newList[i])) {
                    oldList.add(0, newList[i])
                    notifyItemInserted(0)
                    isChangedDataInRange = true
                }
            }

            for (i in 0..rightBoundIndexNewList) {
                if (oldHashSet.isNotEmpty() && newList.isNotEmpty() && oldList.isNotEmpty() && !areItemTheSame(
                        oldList[i],
                        newList[i]
                    ) && oldHashSet.contains(newList[i])
                ) {
                    isChangedDataInRange = true
                    val oldPosition = oldList.indexOf(newList[i])
                    if (oldPosition != -1) {
                        val item = oldList.removeAt(oldPosition)
                        oldList.add(i, item)
                        oldHashSet.remove(newList[i])
                        notifyItemMoved(oldPosition, i)
                    }
                }
            }
            if (!isChangedDataInRange) {
                Handler().postDelayed({
                    notifyDataSetChanged()
                }, 50)
            }
        }

    }


    override fun getItemCount(): Int = list.size

    protected fun getDataSource() = list

    protected fun setLinearLayoutManager(rv: RecyclerView) {
        recyclerViewLayoutManager = rv.layoutManager as LinearLayoutManager
        rv.post {
            (rv.itemAnimator as SlideInDownAnimator).callbackNotifyDataSetChanged = {
                notifyDataSetChanged()
            }
        }
    }
}

