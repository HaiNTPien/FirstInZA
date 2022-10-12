package com.example.listwithanimation

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

object ViewHelper {
    @JvmStatic
    fun clear(v: View) {
        v.apply {
            alpha = 1f
            scaleY = 1f
            scaleX = 1f
            translationY = 0f
            translationX = 0f
            rotation = 0f
            rotationY = 0f
            rotationX = 0f
            pivotY = v.measuredHeight / 2f
            pivotX = v.measuredWidth / 2f
            animate().setInterpolator(null).startDelay = 0
        }
    }
}
