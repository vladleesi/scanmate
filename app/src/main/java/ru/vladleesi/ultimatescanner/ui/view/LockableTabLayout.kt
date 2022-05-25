package ru.vladleesi.ultimatescanner.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.tabs.TabLayout

/**
 * [TabLayout] with disabled tab's touching
 */
class LockableTabLayout : TabLayout {

    private var isEnable: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun setEnable(isEnable: Boolean) {
        this.isEnable = isEnable
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = !isEnable

//    override fun onTouchEvent(ev: MotionEvent?): Boolean = !isEnable

//    override fun performClick(): Boolean = !isEnable
}
