package com.example.listwithanimation

import android.animation.AnimatorInflater
import android.content.Context
import android.view.animation.BounceInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator


class CustomAnimators(context: Context) : SimpleItemAnimator() {
    var context: Context

    init {
        this.context = context
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val set = AnimatorInflater.loadAnimator(
            context,
            R.animator.item_slide_out
        )
        set.interpolator = BounceInterpolator()
        set.setTarget(holder.itemView)
        set.start()
        return true
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        val set = AnimatorInflater.loadAnimator(
            context,
            R.animator.item_slide_in
        )
        set.interpolator = BounceInterpolator()
        set.setTarget(holder.itemView)
        set.start()
        return true
    }

    override fun animateMove(
        holder: RecyclerView.ViewHolder,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        return false
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        fromLeft: Int,
        fromTop: Int,
        toLeft: Int,
        toTop: Int
    ): Boolean {
        return false
    }

    override fun runPendingAnimations() {}
    override fun endAnimation(item: RecyclerView.ViewHolder) {}
    override fun endAnimations() {}
    override fun isRunning(): Boolean {
        return false
    }
}