package com.example.myapplication.main

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet?=null
): AppCompatImageView(context, attrs) {

    private var scaleFactor = 1.0f
    private val detector = ScaleGestureDetector(context, object: ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 4.0f)
            invalidate()
            return true
        }
    })

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor, (width/2).toFloat(), (height/2).toFloat())
        super.onDraw(canvas)
        canvas.restore()
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        return detector.onTouchEvent(event) || super.onTouchEvent(event)
    }
}