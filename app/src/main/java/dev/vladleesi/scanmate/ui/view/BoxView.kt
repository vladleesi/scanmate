package dev.vladleesi.scanmate.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.content.ContextCompat.getColor
import dev.vladleesi.scanmate.R

class BoxView(context: Context?) : View(context) {

    private var paint: Paint = Paint().apply {
        isAntiAlias = true
        color = context?.let { getColor(it, R.color.gray_light) } ?: Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val radiusArr = floatArrayOf(
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS,
        STROKE_CORNER_RADIUS
    )

    private lateinit var rect: Rect
    private val path: Path = Path()

    fun rect(ocrBox: List<List<Float>>) {
        val left = ocrBox[0][0]
        val top = ocrBox[1][1]
        val right = ocrBox[2][0]
        val bottom = ocrBox[3][1]
        // TODO: Remove path?
        path.addRoundRect(
            RectF(left, top, right, bottom),
            radiusArr,
            Path.Direction.CW
        )
        rect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        invalidate()
    }

    public override fun onDraw(canvas: Canvas) {
//        canvas.drawPath(path, paint)
        canvas.drawRect(rect, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

//        val width = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY)
//        val height = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY)

        val desiredWidth = rect.width()
        val desiredHeight = rect.height()

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // Measure Width
        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                // Must be this size
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                // Can't be bigger than...
                desiredWidth.coerceAtMost(widthSize)
            }
            else -> {
                // Be whatever you want
                desiredWidth
            }
        }

        // Measure Height
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                // Must be this size
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                // Can't be bigger than...
                desiredHeight.coerceAtMost(heightSize)
            }
            else -> {
                // Be whatever you want
                desiredHeight
            }
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    fun getDefaultViewSize(size: Int, measureSpec: Int): Int {

        // Parameter Description:
        // The first parameter size: the default size provided
        // The second parameter: width/height measurement specifications (including mode & measurement size)

        // Set the default size
        var result = size

        // Get width/height measurement specifications mode & measurement size
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = size
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> result = specSize
        }

        // Return the width/height value of View
        return result
    }

    companion object {
        private const val STROKE_WIDTH = 3.5f
        private const val STROKE_CORNER_RADIUS = 10f
    }
}
