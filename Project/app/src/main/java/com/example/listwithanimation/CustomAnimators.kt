package com.example.listwithanimation

import androidx.recyclerview.widget.RecyclerView


open class SlideInDownAnimator : BaseItemAnimator() {

    override fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {
    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().apply {
            translationY(0F)
            alpha(0f)
            duration = removeDuration
            interpolator = interpolator
            startDelay = getRemoveDelay(holder)
        }.start()
    }

    override fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {
        addDuration = moveDuration
        holder.itemView.translationY = -holder.itemView.height.toFloat()
        holder.itemView.alpha = 1f
    }

    override fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().apply {
            translationY(0f)
            alpha(1f)
            duration = addDuration
            interpolator = interpolator
            startDelay = getAddDelay()
        }.start()
    }

}

