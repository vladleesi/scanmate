package ru.vladleesi.ultimatescanner.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class SwipeViewPager : ViewPager {

    private var initialXValue = 0f
    private var direction: SwipeDirection

    private var mListener: OnSwipeOutListener? = null

    init {
        direction = SwipeDirection.ALL
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setOnSwipeOutListener(listener: OnSwipeOutListener?) {
        mListener = listener
    }

    fun setAllowedSwipeDirection(direction: SwipeDirection) {
        this.direction = direction
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipeAllowed(event)) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (isSwipeAllowed(event)) {
            super.onInterceptTouchEvent(event)
        } else false
    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        if (direction === SwipeDirection.ALL) return true

        if (direction === SwipeDirection.NONE) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            initialXValue = event.x
            return true
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            try {
                val diffX = event.x - initialXValue
                if (diffX > 0 && direction === SwipeDirection.FROM_LEFT_TO_RIGHT) {
                    // swipe from left to right detected
                    mListener?.onSwipeFromLeftToRight()
                    return false
                } else if (diffX < 0 && direction === SwipeDirection.FROM_RIGHT_TO_LEFT) {
                    // swipe from right to left detected
                    mListener?.onSwipeFromRightToLeft()
                    return false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
        return true
    }

    interface OnSwipeOutListener {
        fun onSwipeFromLeftToRight()
        fun onSwipeFromRightToLeft()
    }
}
