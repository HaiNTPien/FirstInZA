package com.example.listwithanimation

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter


open class SlideInDownAnimator : BaseItemAnimator() {

    init {
        addDuration = ADD_DURATION
        moveDuration = MOVE_DURATION
        removeDuration = REMOVE_DURATION
        changeDuration = CHANGE_DURATION
    }
    var callbackNotifyDataSetChanged: (()->Unit)? = null

    override fun notifyDatasetChanged() {
        callbackNotifyDataSetChanged?.invoke()
    }

    override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().apply {
            translationY(0F)
            alpha(0f)
            duration = REMOVE_DURATION
            setListener(DefaultRemoveAnimatorListener(holder))
            interpolator = null
            startDelay = getRemoveDelay(holder)
        }.start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {
        holder.itemView.translationZ = 0F
        holder.itemView.translationY = -holder.itemView.height.toFloat()
        holder.itemView.alpha = 1f
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().apply {
            translationY(0f)
            alpha(1f)
            duration = ADD_DURATION
            setListener(DefaultAddAnimatorListener(holder))
            interpolator = null
            startDelay = getAddDelay(holder)
        }.start()
    }


}

