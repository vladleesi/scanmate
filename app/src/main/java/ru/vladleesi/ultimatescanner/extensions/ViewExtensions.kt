package ru.vladleesi.ultimatescanner.extensions

import android.view.View


fun View.visible() = setVisibilityInner(View.VISIBLE)
fun View.invisible() = setVisibilityInner(View.INVISIBLE)
fun View.gone() = setVisibilityInner(View.INVISIBLE)

private fun View.setVisibilityInner(visibility: Int) {
    if (this.visibility != visibility) this.visibility = visibility
}
