package com.example.myapplication.main

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.annotation.DrawableRes
import com.example.myapplication.R
import android.graphics.BitmapFactory

class ZoomableLibraryMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var bitmap: Bitmap? = null
    private val bitmapMatrix = Matrix()

    private var currentScale = 1f
    private val minScale = 1f
    private val maxScale = 4f

    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    private val paintHighlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val paintHighlightFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 255, 0, 0)
        style = Paint.Style.FILL
    }

    private var shelfLocations: List<ShelfLocation> = emptyList()
    private var selectedShelf: ShelfLocation? = null

    init {
        scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val newScale = (currentScale * scaleFactor).coerceIn(minScale, maxScale)

                    val factor = newScale / currentScale
                    currentScale = newScale

                    bitmapMatrix.postScale(
                        factor,
                        factor,
                        detector.focusX,
                        detector.focusY
                    )
                    invalidate()
                    return true
                }
            })

        gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    bitmapMatrix.postTranslate(-distanceX, -distanceY)
                    invalidate()
                    return true
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val targetScale = if (currentScale < 2f) 2f else 1f
                    val factor = targetScale / currentScale
                    currentScale = targetScale
                    bitmapMatrix.postScale(factor, factor, e.x, e.y)
                    invalidate()
                    return true
                }
            })
    }

    fun setMapImage(@DrawableRes resId: Int) {
        val maxSize = 1024
        val bmp = decodeSampledBitmapFromResource(resId, maxSize, maxSize)
        bitmap = bmp ?: return

        resetMatrixToCenter()
        invalidate()
    }

    private fun decodeSampledBitmapFromResource(
        @DrawableRes resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeResource(resources, resId, options)

        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565

        return BitmapFactory.decodeResource(resources, resId, options)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        while (height / inSampleSize > reqHeight || width / inSampleSize > reqWidth) {
            inSampleSize *= 2
        }

        if (inSampleSize < 1) inSampleSize = 1
        return inSampleSize
    }

    fun setShelfLocations(locations: List<ShelfLocation>) {
        shelfLocations = locations
        invalidate()
    }

    fun highlightShelf(shelfCode: String) {
        val shelf = shelfLocations.firstOrNull { it.shelfCode == shelfCode }
        selectedShelf = shelf
        shelf?.let { focusOnShelf(it) }
        invalidate()
    }

    private fun focusOnShelf(shelf: ShelfLocation) {
        val bmp = bitmap ?: return

        val rectImage = RectF(
            shelf.rect.left * bmp.width,
            shelf.rect.top * bmp.height,
            shelf.rect.right * bmp.width,
            shelf.rect.bottom * bmp.height
        )

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        val rectWidth = rectImage.width()
        val rectHeight = rectImage.height()

        val scaleX = (viewWidth * 0.4f) / rectWidth
        val scaleY = (viewHeight * 0.4f) / rectHeight
        val targetScale = (scaleX.coerceAtMost(scaleY)).coerceIn(minScale, maxScale)

        currentScale = targetScale

        bitmapMatrix.reset()
        bitmapMatrix.postScale(targetScale, targetScale)

        val centerImageX = (rectImage.left + rectImage.right) / 2f
        val centerImageY = (rectImage.top + rectImage.bottom) / 2f

        val centerScaledX = centerImageX * targetScale
        val centerScaledY = centerImageY * targetScale

        val dx = viewWidth / 2f - centerScaledX
        val dy = viewHeight / 2f - centerScaledY

        bitmapMatrix.postTranslate(dx, dy)
    }

    private fun resetMatrixToCenter() {
        val bmp = bitmap ?: return
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        if (viewWidth == 0f || viewHeight == 0f) return

        bitmapMatrix.reset()

        val scale = minOf(
            viewWidth / bmp.width.toFloat(),
            viewHeight / bmp.height.toFloat()
        )

        currentScale = scale

        val dx = (viewWidth - bmp.width * scale) / 2f
        val dy = (viewHeight - bmp.height * scale) / 2f

        bitmapMatrix.postScale(scale, scale)
        bitmapMatrix.postTranslate(dx, dy)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetMatrixToCenter()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        canvas.save()
        canvas.concat(bitmapMatrix)
        canvas.drawBitmap(bmp, 0f, 0f, null)

        selectedShelf?.let { shelf ->
            val rectNorm = shelf.rect
            val rect = RectF(
                rectNorm.left * bmp.width,
                rectNorm.top * bmp.height,
                rectNorm.right * bmp.width,
                rectNorm.bottom * bmp.height
            )
            canvas.drawRect(rect, paintHighlightFill)
            canvas.drawRect(rect, paintHighlight)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }


    fun smoothZoom(factor: Float) {
        val bmp = bitmap ?: return
        val cx = width / 2f
        val cy = height / 2f

        val newScale = (currentScale * factor).coerceIn(minScale, maxScale)
        val appliedFactor = newScale / currentScale

        currentScale = newScale
        bitmapMatrix.postScale(appliedFactor, appliedFactor, cx, cy)
        invalidate()
    }
}
