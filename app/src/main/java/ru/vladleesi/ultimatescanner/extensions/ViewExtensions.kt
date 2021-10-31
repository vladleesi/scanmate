package ru.vladleesi.ultimatescanner.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import ru.vladleesi.ultimatescanner.R

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

fun ImageView.setByteArray(byteArray: ByteArray?) {
    byteArray?.let {
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        setImageBitmap(bmp)
    }
}

fun View.animate(@AnimRes animResId: Int) {
    startAnimation(AnimationUtils.loadAnimation(context, animResId))
}

fun View.showWithAnim() {
    animate(R.anim.enlarge)
    visible()
}

fun View.showWithAnim(delay: Long) {
    postDelayed({ showWithAnim() }, delay)
}

fun View.hideWithAnim() {
    animate(R.anim.shrink)
    postOnAnimation { gone() }
}

fun View.hideWithAnim(delay: Long = 0) {
    postDelayed({ hideWithAnim() }, delay)
}

fun ViewPager.addOnPageSelected(action: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            // ignore
        }

        override fun onPageSelected(position: Int) {
            action(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            // ignore
        }
    })
}
