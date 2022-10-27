package com.example.listwithanimation.adapters

import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.listwithanimation.SlideInDownAnimator

abstract class BaseAdapter<T> : RecyclerView.Adapter<ViewHolder>() {

    private var list = mutableListOf<T>()
    private lateinit var recyclerViewLayoutManager: LinearLayoutManager
    private val handler = Handler()
    abstract fun areItemTheSame(oldItem: T, newItem: T): Boolean

    /**
     * Set new list for recyclerview immediately without animations
     */
    protected fun setListImmediately(lst: List<T>) {
        list.clear()
        list.addAll(lst)
        notifyDataSetChanged()
    }

    /**
     * Remove an item from the list with animations
     */
    protected fun removeOne(position: Int) {
        if (position in 0 until itemCount) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Insert an item into the list at the specific position with animation
     */
    protected fun addOne(position: Int, item: T) {
        if (position in 0 until itemCount) {
            list.add(position, item)
            notifyItemInserted(position)
        }

    }

    /**
     * Move an item from the first position to the second position with animations
     */
    protected fun moveOne(startPosition: Int, endPosition: Int) {
        if (startPosition in 0 until itemCount && endPosition in 0 until itemCount) {
            val item = list.removeAt(startPosition)
            list.add(endPosition, item)
            notifyItemMoved(startPosition, endPosition)
        }

    }

    /**
     * Set the new list with animations
     */
    protected fun setList(newList: List<T>) {
        val lPosition = recyclerViewLayoutManager.findLastVisibleItemPosition()
        var isChangedDataInRange = false
        if (newList.isEmpty()) {
            setListImmediately(listOf())
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
                handler.postDelayed({
                    notifyDataSetChanged()
                }, 50)
            }
        }

    }


    override fun getItemCount(): Int = list.size

    /**
     * Get current list
     */
    protected fun getDataSource() = list

    /**
     * Set SlideInDownAnimator() animation to the adapter
     */
    protected fun setLinearLayoutManager(rv: RecyclerView) {
        recyclerViewLayoutManager = rv.layoutManager as LinearLayoutManager
        val animator = SlideInDownAnimator()
        rv.adapter = this
        animator.callbackNotifyDataSetChanged = {
            notifyDataSetChanged()
        }
        rv.itemAnimator = animator
    }
}

