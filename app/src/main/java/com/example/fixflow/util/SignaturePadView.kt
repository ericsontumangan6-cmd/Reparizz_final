package com.example.fixflow.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SignaturePadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#35D6C3") // Primary accent signature line
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }

    private var isEmpty = true

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Disallow parent ScrollView from intercepting touch events while drawing signature
                parent?.requestDisallowInterceptTouchEvent(true)
                path.moveTo(x, y)
                isEmpty = false
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                path.lineTo(x, y)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun clearSignature() {
        path.reset()
        isEmpty = true
        invalidate()
    }

    fun isSignatureEmpty(): Boolean = isEmpty

    fun getSignatureBitmap(): Bitmap? {
        if (isEmpty || width <= 0 || height <= 0) return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawPath(path, paint.apply { color = Color.BLACK })
        return bitmap
    }
}
