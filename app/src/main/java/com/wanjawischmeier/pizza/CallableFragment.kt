package com.wanjawischmeier.pizza

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task

abstract class CallableFragment : Fragment() {
    companion object {
        lateinit var topBubble: ConstraintLayout
        lateinit var bottomLayout: ConstraintLayout
    }

    var topBubbleVisible: Boolean
        get() = topBubble.alpha == 1f
        set(value) {
            if (value == topBubbleVisible) return

            topBubble.animate()
                .alpha(if (value) 1f else 0f)
                .duration = resources.getInteger(R.integer.animation_duration_fragment).toLong()
        }

    var bottomLayoutVisible: Boolean
        get() = bottomLayout.translationY == 0f
        set(value) {
            if (value == bottomLayoutVisible) return

            var start = 0f
            var end = bottomLayout.height.toFloat()

            if (value) {
                start = end
                end = 0f
                bottomLayout.isGone = false
            }

            bottomLayout.translationY = start
            bottomLayout.animate()
                .translationY(end)
                .withEndAction {
                    if (!value) {
                        bottomLayout.isGone = true
                    }
                }
                .duration = resources.getInteger(R.integer.animation_duration_fragment).toLong()
        }

    abstract fun onShow(): Task<Unit>?

    abstract fun onHide()
}