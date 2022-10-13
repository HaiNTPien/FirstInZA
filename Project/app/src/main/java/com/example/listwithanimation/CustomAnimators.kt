package com.example.listwithanimation

import android.util.Log
import androidx.recyclerview.widget.RecyclerView


open class SlideInDownAnimator : BaseItemAnimator() {

    override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {
        Log.d(" ItemAnimator ", " preAnimateRemoveImpl ")
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        Log.d(" ItemAnimator ", " animateRemoveImpl ")
        holder.itemView.animate().apply {
            translationY(0F)
            alpha(0f)
            duration = removeDuration
            setListener(DefaultRemoveAnimatorListener(holder))
            interpolator = null
            startDelay = getRemoveDelay(holder)
        }.start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {
        Log.d(" ItemAnimator ", " preAnimateAddImpl ")
        addDuration = moveDuration
        holder.itemView.translationZ = 0F
        holder.itemView.translationY = -holder.itemView.height.toFloat()
        holder.itemView.alpha = 1f
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        Log.d(" ItemAnimator ", " animateAddImpl ")
        holder.itemView.animate().apply {
            translationY(0f)
            alpha(1f)
            duration = addDuration
            setListener(DefaultAddAnimatorListener(holder))
            interpolator = null
            startDelay = getAddDelay(holder)
        }.start()
    }

}

