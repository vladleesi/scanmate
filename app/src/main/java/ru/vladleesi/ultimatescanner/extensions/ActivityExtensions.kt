package ru.vladleesi.ultimatescanner.extensions

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Activity.makeStatusBarTransparent() {
    window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        statusBarColor = Color.TRANSPARENT
    }
}

fun Activity.getDrawableCompat(@DrawableRes drawableResId: Int): Drawable? =
    ContextCompat.getDrawable(baseContext, drawableResId)

fun Activity.showToast(text: String?) = Toast.makeText(baseContext, text, Toast.LENGTH_SHORT).show()
