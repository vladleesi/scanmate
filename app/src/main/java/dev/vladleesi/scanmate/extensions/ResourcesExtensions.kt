package dev.vladleesi.scanmate.extensions

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.getDrawableCompat(@DrawableRes drawableResId: Int) =
    ContextCompat.getDrawable(this, drawableResId)

fun Context.getStatusBarHeight(): Float {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resourceId > 0) resources.getDimension(resourceId) else 0f
}
