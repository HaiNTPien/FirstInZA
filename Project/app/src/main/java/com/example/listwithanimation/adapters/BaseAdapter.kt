package com.example.listwithanimation.adapters

import android.os.Handler
import android.util.Log
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
        synchronized(this) {
            if(position in 0 until itemCount) {
                list.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun addOne(position: Int, item: T){
        synchronized(this) {
            if(position in 0 until itemCount) {
                list.add(position, item)
                notifyItemInserted(position)
            }
        }
    }

    fun moveOne(startPosition: Int, endPosition: Int) {
        synchronized(this) {
            if( startPosition in 0 until itemCount && endPosition in 0 until itemCount) {
                val item = list.removeAt(startPosition)
                list.add(endPosition, item)
                notifyItemMoved(startPosition, endPosition)
            }
        }
    }

    fun submitDataList(newList : List<T>, fPosition: Int, lPosition: Int) {
        synchronized(this) {
            var isChangedDataInRange = false
            Log.d(" submit list ", "$fPosition $lPosition")
            if(list.size > newList.size) {
                val oldSize = list.size
                list = list.subList(0, newList.size)
                notifyItemRangeRemoved(newList.size, oldSize)
            }
            var leftBoundIndexNewList = if(fPosition == -1) 0 else fPosition
            var rightBoundIndexNewList = if(newList.isEmpty()) {
                0
            }else {
                if(newList.size <= lPosition) {
                    newList.size - 1
                } else {
                    when(lPosition) {
                        -1 -> 0
                        0 -> newList.size - 1
                        else -> lPosition
                    }
                }
            }
            if(leftBoundIndexNewList > rightBoundIndexNewList) {leftBoundIndexNewList = rightBoundIndexNewList}
            var leftBoundIndexOldList = if(fPosition == -1) 0 else fPosition
            var rightBoundIndexOldList = if(list.isEmpty()) {
                0
            } else {
                if(list.size <= lPosition) {
                    list.size - 1
                } else {
                    when(lPosition) {
                        -1 -> 0
                        else -> lPosition
                    }
                }
            }
            if(leftBoundIndexOldList > rightBoundIndexOldList) {leftBoundIndexOldList = rightBoundIndexOldList}

//            val deltaOldList = rightBoundIndexOldList - leftBoundIndexOldList
//            val deltaNewList = rightBoundIndexNewList - leftBoundIndexNewList
//            if(deltaOldList > deltaNewList) {
//                val oldSize = list.size
//                list = list.subList(0, rightBoundIndexOldList)
//                notifyItemRangeRemoved(rightBoundIndexOldList, oldSize - 1)
//            }
            Log.d(" submit data old", rightBoundIndexOldList.toString() + " " + leftBoundIndexOldList.toString() + " "  + list.size.toString())
            Log.d(" submit data new", rightBoundIndexNewList.toString() + " " + leftBoundIndexNewList.toString() + " " + newList.size.toString())

            val oldList = list
//        val oldHashSet = list.subList(0, 15).toHashSet()
//        val newHashSet = newList.subList(0, 15).toHashSet()

            val oldHashSet = list.subList(0, if(oldList.isEmpty()) 0 else rightBoundIndexOldList + 1).toHashSet()
            val newHashSet = newList.subList(0, if(newList.isEmpty()) 0 else rightBoundIndexNewList + 1).toHashSet()
            list = newList.toMutableList()
            for (i in rightBoundIndexOldList   downTo 0) {
                if (newHashSet.isNotEmpty() && oldList.isNotEmpty() && !newHashSet.contains(oldList[i]) ) {
                    oldList.removeAt(i)
                    notifyItemRemoved(i)
                    isChangedDataInRange = true
                }
            }
            for (i in 0 .. rightBoundIndexNewList) {
                if (oldHashSet.isNotEmpty() && newList.isNotEmpty() && !oldHashSet.contains(newList[i])) {
                    oldList.add(0, newList[i])
                    notifyItemInserted(0)
                    isChangedDataInRange = true
                }
            }


            Log.d(" submit data newListSize", (newList.size).toString())
            Log.d(" submit data oldListSize", (oldList.size).toString())
            Log.d(" submit data compare size", (newList.size == oldList.size).toString())
            for (i in 0 .. rightBoundIndexNewList) {
                if (oldHashSet.isNotEmpty() && newList.isNotEmpty()  && oldList.isNotEmpty() && !areItemTheSame(oldList[i], newList[i]) && oldHashSet.contains(newList[i])) {
                    isChangedDataInRange = true
                    val oldPosition = oldList.indexOf(newList[i])
                    if (oldPosition != -1) {
                        val item = oldList.removeAt(oldPosition)
                        oldList.add(i , item)
                        oldHashSet.remove(newList[i])
                        notifyItemMoved(oldPosition, i)
                    }
                }
            }
            Log.d(" submit data compare ", (list == oldList).toString())
            if (!isChangedDataInRange) {
                Handler().postDelayed({
                    notifyDataSetChanged()
                }, 250)
            }
        }
    }


    override fun getItemCount(): Int = list.size

    protected fun getDataSource() = list
}

