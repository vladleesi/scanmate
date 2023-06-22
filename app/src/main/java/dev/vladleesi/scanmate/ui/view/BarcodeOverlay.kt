package dev.vladleesi.scanmate.ui.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import dev.vladleesi.scanmate.ui.model.scan.ScanResult

class BarcodeOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var barcodes = listOf<FirebaseVisionBarcode>()
    private var scaleFactorX = 1.0f
    private var scaleFactorY = 1.0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            barcodes.forEach { barcode ->
                barcode.boundingBox.let { boundingBox ->
                    boundingBox?.let {
                        val rect = translateRect(boundingBox)
                        val cx = rect.left + (rect.right - rect.left) / 2
                        val cy = rect.top + (rect.bottom - rect.top) / 2
                        drawCircle(cx, cy, BOUNDING_BOX_RADIUS, paint)
                    }
                }
            }
        }
    }

    fun overlay(scanResult: ScanResult) {
        if (isPortraitMode()) {
            scaleFactorY = height.toFloat() / scanResult.imageWidth
            scaleFactorX = width.toFloat() / scanResult.imageHeight
        } else {
            scaleFactorX = width.toFloat() / scanResult.imageWidth
            scaleFactorY = height.toFloat() / scanResult.imageHeight
        }
        barcodes = scanResult.barcodes
        invalidate()
    }

    private fun isPortraitMode(): Boolean {
        val orientation: Int = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun translateX(x: Float): Float = x * scaleFactorX
    private fun translateY(y: Float): Float = y * scaleFactorY

    private fun translateRect(rect: Rect) = RectF(
        translateX(rect.left.toFloat()),
        translateY(rect.top.toFloat()),
        translateX(rect.right.toFloat()),
        translateY(rect.bottom.toFloat())
    )

    companion object {
        private const val BOUNDING_BOX_RADIUS = 16f
    }
}
