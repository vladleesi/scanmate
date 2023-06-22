package dev.vladleesi.scanmate.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import dev.vladleesi.scanmate.R

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

fun ViewPager.addOnPageSelected(infinityScroll: Boolean, action: (position: Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

        private val lastPosition = adapter?.count?.minus(1) ?: -1
        private var currentPosition = 0
        private var isDragged = false

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            // ignore
        }

        override fun onPageSelected(position: Int) {
            currentPosition = position
            action(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (infinityScroll) {
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> Log.d(TAG, "ViewPager.SCROLL_STATE_IDLE")
                    ViewPager.SCROLL_STATE_DRAGGING -> Log.d(TAG, "ViewPager.SCROLL_STATE_DRAGGING")
                    ViewPager.SCROLL_STATE_SETTLING -> Log.d(TAG, "ViewPager.SCROLL_STATE_SETTLING")
                }

                if (state == ViewPager.SCROLL_STATE_IDLE && isDragged && currentPosition == lastPosition) {
                    setCurrentItem(0, true)
                }

                if (state == ViewPager.SCROLL_STATE_IDLE && isDragged && currentPosition == 0) {
                    setCurrentItem(lastPosition, true)
                }

                isDragged = state == ViewPager.SCROLL_STATE_DRAGGING
            }
        }
    })
}

fun TabLayout.setMargin(start: Int?, end: Int?) {
    getChildAt(0)
        .let { it as? ViewGroup }
        ?.let { viewGroup ->
            repeat(viewGroup.childCount) { index ->
                viewGroup.getChildAt(index).updateLayoutParams {
                    if (this is ViewGroup.MarginLayoutParams) {
                        start?.let { marginStart = start }
                        end?.let { marginStart = end }
                    }
                }
            }
        }
}

const val TAG = "ViewExtensions"
