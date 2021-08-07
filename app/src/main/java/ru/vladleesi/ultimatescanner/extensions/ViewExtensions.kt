package ru.vladleesi.ultimatescanner.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

fun View.visible() = setVisibilityInner(View.VISIBLE)
fun View.invisible() = setVisibilityInner(View.INVISIBLE)
fun View.gone() = setVisibilityInner(View.INVISIBLE)

private fun View.setVisibilityInner(visibility: Int) {
    if (this.visibility != visibility) this.visibility = visibility
}

fun View.showPopup(popupView: View) {
    val popupWindow = PopupWindow(
        popupView,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    )
    popupWindow.elevation = 4f
    if (popupView.parent != null) {
        (popupView.parent as ViewGroup).removeView(popupView)
    }

    popupWindow.showAsDropDown(this)
//    popupWindow.showAtLocation(this, Gravity.CENTER, 0, 0)
}

fun TextView.copyToClipboard() {
    val clipboard: ClipboardManager =
        context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("copy", text)
    clipboard.setPrimaryClip(clip)
}
