package com.example.listwithanimation

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.listwithanimation.ViewHelper.clear

abstract class BaseItemAnimator : SimpleItemAnimator() {

    private val pendingRemovals = ArrayList<RecyclerView.ViewHolder>()
    private val pendingAdditions = ArrayList<RecyclerView.ViewHolder>()
    private val pendingMoves = ArrayList<MoveInfo>()
    private val pendingChanges = ArrayList<ChangeInfo>()
    private val additionsList = ArrayList<ArrayList<RecyclerView.ViewHolder>>()
    private val movesList = ArrayList<ArrayList<MoveInfo>>()
    private val changesList = ArrayList<ArrayList<ChangeInfo>>()
    protected var addAnimations = ArrayList<RecyclerView.ViewHolder>()
    private val moveAnimations = ArrayList<RecyclerView.ViewHolder>()
    protected var removeAnimations = ArrayList<RecyclerView.ViewHolder>()
    private val changeAnimations = ArrayList<RecyclerView.ViewHolder>()
    private var runnable = Runnable {
        notifyDatasetChanged()
    }
    var handler = Handler()
    protected open fun notifyDatasetChanged() {}

    init {
        supportsChangeAnimations = true
    }

    protected open fun preAnimateRemoveImpl(holder: RecyclerView.ViewHolder) {}

    protected open fun preAnimateAddImpl(holder: RecyclerView.ViewHolder) {}

    protected abstract fun animateRemoveImpl(holder: RecyclerView.ViewHolder)

    protected abstract fun animateAddImpl(holder: RecyclerView.ViewHolder)

    private class MoveInfo(var holder: RecyclerView.ViewHolder, var fromX: Int, var toX: Int, var fromY: Int, var toY: Int)

    private class ChangeInfo(var oldHolder: RecyclerView.ViewHolder? = null, var newHolder: RecyclerView.ViewHolder? = null)

    override fun runPendingAnimations() {
        val removalsPending = pendingRemovals.isNotEmpty()
        val movesPending = pendingMoves.isNotEmpty()
        val changesPending = pendingChanges.isNotEmpty()
        val additionsPending = pendingAdditions.isNotEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            return
        }

        for (holder in pendingRemovals) {
            animateRemoveImpl(holder)
            removeAnimations.add(holder)
        }
        pendingRemovals.clear()

        if (movesPending) {
            val moves = ArrayList(pendingMoves)
            movesList.add(moves)
            pendingMoves.clear()
            val mover = Runnable {
                val removed = movesList.remove(moves)
                if (!removed) {
                    // already canceled
                    return@Runnable
                }
                for (moveInfo in moves) {
                    animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.toX, moveInfo.fromY, moveInfo.toY)
                }
                moves.clear()
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                view.postOnAnimationDelayed(mover, REMOVE_DURATION)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList(pendingChanges)
            changesList.add(changes)
            pendingChanges.clear()
            val changer = Runnable {
                val removed = changesList.remove(changes)
                if (!removed) {
                    // already canceled
                    return@Runnable
                }
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                holder!!.itemView.postOnAnimationDelayed(changer, REMOVE_DURATION)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList(pendingAdditions)
            additionsList.add(additions)
            pendingAdditions.clear()
            val adder = Runnable {
                val removed = additionsList.remove(additions)
                if (!removed) {
                    // already canceled
                    return@Runnable
                }
                for (holder in additions) {
                    animateAddImpl(holder)
                    addAnimations.add(holder)
                }
                additions.clear()
            }
            if (removalsPending) {
                val view = additions[0].itemView
                view.postOnAnimationDelayed(adder, REMOVE_DURATION)
            } else {
                adder.run()
            }
        }

    }

     
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateRemoveImpl(holder)
        pendingRemovals.add(holder)
        return true
    }

    protected fun getRemoveDelay(holder: RecyclerView.ViewHolder): Long {
        return 0
    }


     
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        endAnimation(holder)
        preAnimateAddImpl(holder)
        pendingAdditions.add(holder)
        return true
    }

    protected fun getAddDelay(holder: RecyclerView.ViewHolder): Long {
        return 0
    }

     
    override fun animateMove(
            holder: RecyclerView.ViewHolder,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
    ): Boolean {
        var fX = fromX
        var fY = fromY
        val view = holder.itemView
        fX += view.translationX.toInt()
        fY += view.translationY.toInt()
        endAnimation(holder)
        val deltaX = toX - fX
        val deltaY = toY - fY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            view.translationX = -deltaX.toFloat()
        }
        if (deltaY != 0) {
            view.translationY = -deltaY.toFloat()
        }
        pendingMoves.add(MoveInfo(holder, fX, toX, fY, toY))
        return true
    }

    private fun animateMoveImpl(holder: RecyclerView.ViewHolder, fromX: Int, toX: Int, fromY: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        val animation = view.animate()
        animation.interpolator = null
        if (deltaX != 0) {
            animation.translationX(0f)
        }
        if (deltaY != 0) {
            animation.translationY(0f)
        }
        moveAnimations.add(holder)
        animation.setDuration(MOVE_DURATION).setListener(object : AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(animator: Animator) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationRepeat(animation: Animator) {
            }

             
            override fun onAnimationEnd(animator: Animator) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                moveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

     
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder, fromX: Int, fromY: Int,
        toX: Int, toY: Int
    ): Boolean {
        if (oldHolder === newHolder) {
            return animateMove(oldHolder, fromX, toX, fromY, toY)
        }
        val prevTranslationX = oldHolder.itemView.translationX
        val prevTranslationY = oldHolder.itemView.translationY
        val prevAlpha = oldHolder.itemView.alpha
        endAnimation(oldHolder)
        val deltaX = (toX - fromX - prevTranslationX).toInt()
        val deltaY = (toY - fromY - prevTranslationY).toInt()
        oldHolder.itemView.translationX = prevTranslationX
        oldHolder.itemView.translationY = prevTranslationY
        oldHolder.itemView.alpha = prevAlpha
        endAnimation(newHolder)
        newHolder.itemView.translationX = -deltaX.toFloat()
        newHolder.itemView.translationY = -deltaY.toFloat()
        newHolder.itemView.alpha = 0f
        pendingChanges.add(ChangeInfo(oldHolder, newHolder))
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            changeAnimations.add(holder)
            val oldViewAnim = view.animate().setDuration(CHANGE_DURATION)
                .alpha(0f)
                .translationX(0F)
                .translationY(0F)
            oldViewAnim.setListener(object : AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    dispatchChangeStarting(holder, true)
                }

                 
                override fun onAnimationEnd(animator: Animator) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(holder, true)
                    changeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }

                override fun onAnimationCancel(animation: Animator) {
                }

                override fun onAnimationRepeat(animation: Animator) {
                }
            }).start()
        }
        if (newView != null) {
            changeAnimations.add(newHolder)
            val newViewAnimation = newView.animate()
            newViewAnimation.translationX(0f).translationY(0f).setDuration(CHANGE_DURATION)
                .alpha(1f)
                    .setListener(object : AnimatorListener {
                        override fun onAnimationStart(animator: Animator) {
                            dispatchChangeStarting(newHolder, false)
                        }

                         
                        override fun onAnimationEnd(animator: Animator) {
                            newViewAnimation.setListener(null)
                            newView.alpha = 1f
                            newView.translationX = 0f
                            newView.translationY = 0f
                            dispatchChangeFinished(newHolder, false)
                            changeAnimations.remove(newHolder)
                            dispatchFinishedWhenDone()
                        }

                        override fun onAnimationCancel(animation: Animator) {
                        }

                        override fun onAnimationRepeat(animation: Animator) {
                        }
                    }).start()
        }
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(
            changeInfo: ChangeInfo,
            item: RecyclerView.ViewHolder?
    ): Boolean {
        var oldItem = false
        when {
            changeInfo.newHolder === item -> {
                changeInfo.newHolder = null
            }
            changeInfo.oldHolder === item -> {
                changeInfo.oldHolder = null
                oldItem = true
            }
            else -> {
                return false
            }
        }
        item!!.itemView.alpha = 1f
        item.itemView.translationX = 0f
        item.itemView.translationY = 0f
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimations() {
        for (i in pendingMoves.size - 1 downTo 0) {
            val item = pendingMoves[i]
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            pendingMoves.removeAt(i)
        }

        for (i in pendingRemovals.size - 1 downTo 0) {
            val item = pendingRemovals[i]
            dispatchRemoveFinished(item)
            pendingRemovals.removeAt(i)
        }

        for (i in pendingAdditions.size - 1 downTo 0) {
            val item = pendingAdditions[i]
            clear(item.itemView)
            dispatchAddFinished(item)
            pendingAdditions.removeAt(i)
        }

        for (i in pendingChanges.size - 1 downTo 0) {
            endChangeAnimationIfNecessary(pendingChanges[i])
        }
        pendingChanges.clear()
        if (!isRunning) {
            return
        }

        for (i in movesList.size - 1 downTo 0) {
            val moves = movesList[i]
            for (j in moves.size - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    movesList.remove(moves)
                }
            }
        }

        for (i in additionsList.size - 1 downTo 0) {
            val additions = additionsList[i]

            for (j in additions.size - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                if (j < additions.size) {
                    additions.removeAt(j)
                }
                if (additions.isEmpty()) {
                    additionsList.remove(additions)
                }
            }
        }

        for (i in changesList.size - 1 downTo 0) {
            val changes = changesList[i]
            for (j in changes.size - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    changesList.remove(changes)
                }
            }
        }

        dispatchAnimationsFinished()
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder)
        }
    }


     
    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        view.animate().cancel()
        for (i in pendingMoves.indices.reversed()) {
            val moveInfo = pendingMoves[i]
            if (moveInfo.holder === item) {
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(item)
                pendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(pendingChanges, item)
        if (pendingRemovals.remove(item)) {
            clear(item.itemView)
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            clear(item.itemView)
            dispatchAddFinished(item)
        }
        for (i in changesList.indices.reversed()) {
            val changes = changesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                changesList.removeAt(i)
            }
        }
        for (i in movesList.indices.reversed()) {
            val moves = movesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo = moves[j]
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        movesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in additionsList.indices.reversed()) {
            val additions = additionsList[i]
            if (additions.remove(item)) {
                clear(item.itemView)
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    additionsList.removeAt(i)
                }
            }
        }
        dispatchFinishedWhenDone()

    }

    override fun isRunning(): Boolean {
        return if (pendingAdditions.isNotEmpty() || pendingChanges.isNotEmpty() || pendingMoves.isNotEmpty()
            || pendingRemovals.isNotEmpty()  || movesList.isNotEmpty()
            || additionsList.isNotEmpty() || changesList.isNotEmpty()
            || moveAnimations.isNotEmpty() || removeAnimations.isNotEmpty()
            || addAnimations.isNotEmpty() || changeAnimations.isNotEmpty()
        ) {
            handler.removeCallbacks(runnable)
            true
        }else {
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 50)
            false
        }
    }

     
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    protected inner class DefaultRemoveAnimatorListener(var viewHolder: RecyclerView.ViewHolder) :
        AnimatorListener {
        override fun onAnimationStart(animator: Animator) {
            dispatchRemoveStarting(viewHolder)
        }

        override fun onAnimationCancel(animator: Animator) {
            clear(viewHolder.itemView)
        }

        override fun onAnimationRepeat(animation: Animator) {

        }

         
        override fun onAnimationEnd(animator: Animator) {
            clear(viewHolder.itemView)
            dispatchRemoveFinished(viewHolder)
            removeAnimations.remove(viewHolder)
            dispatchFinishedWhenDone()
        }
    }

    inner class DefaultAddAnimatorListener(var viewHolder: RecyclerView.ViewHolder) :
        AnimatorListener {
        override fun onAnimationStart(animator: Animator) {
            dispatchAddStarting(viewHolder)
        }

        override fun onAnimationCancel(animator: Animator) {
            clear(viewHolder.itemView)
        }

        override fun onAnimationRepeat(animation: Animator) {

        }

         
        override fun onAnimationEnd(animator: Animator) {
            clear(viewHolder.itemView)
            viewHolder.itemView.translationZ = 4F
            addAnimations.remove(viewHolder)
            dispatchAddFinished(viewHolder)
            dispatchFinishedWhenDone()
        }
    }

}

