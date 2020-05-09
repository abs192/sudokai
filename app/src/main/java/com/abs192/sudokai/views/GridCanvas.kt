package com.abs192.sudokai.views

import android.animation.TimeAnimator
import android.animation.TimeAnimator.TimeListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GridCanvas(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private val paint = Paint()
    private val paintBig = Paint()
    private val bgPaint = Paint()

    private var mTimeAnimator: TimeAnimator? = null
    var x = 0
    var y = 0

    //    var drawLeft = true
    var drawRight = true
    //    var drawTop = true
    var drawBottom = true

    private var strokeWidth1 = 2F
    private var strokeWidth2 = 12F
    private var bgAlpha = 0F
    private var alphaSign = 1

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paintBig.color = Color.WHITE
        paintBig.style = Paint.Style.STROKE
        setBackgroundColor(Color.TRANSPARENT)

        bgPaint.color = Color.WHITE
        bgPaint.style = Paint.Style.FILL
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mTimeAnimator = TimeAnimator()
        mTimeAnimator?.setTimeListener(TimeListener { _, _, _ ->
            if (!isLaidOut) {
                return@TimeListener
            }
            updateState()
            invalidate()
        })
        mTimeAnimator?.start()
    }

    private fun updateState() {
        if (bgAlpha >= 50) {
            alphaSign = -1
        }
        if (bgAlpha <= 0) {
            alphaSign = 1
        }
        bgAlpha += (alphaSign * 0.25).toFloat()
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mTimeAnimator?.cancel()
        mTimeAnimator?.setTimeListener(null)
        mTimeAnimator?.removeAllListeners()
        mTimeAnimator = null
    }

    var l = 0F
    var r = 0F
    var t = 0F
    var b = 0F

    fun setPosition(x: Int, y: Int) {
        this.x = x
        this.y = y

        if (x == 8) {
            drawBottom = false
        }
        if (y == 8) {
            drawRight = false
        }

        if ((x < 3 && y < 3) ||
            (x > 5 && y < 3) ||
            (x < 3 && y > 5) ||
            (x > 5 && y > 5) ||
            (x in 3..5 && y in 3..5)
        ) {
            bgAlpha = 50F
            alphaSign = -1
        }

        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        l = left.toFloat()
        r = right.toFloat()
        t = top.toFloat()
        b = bottom.toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        bgPaint.alpha = bgAlpha.toInt()

        canvas?.drawRect(l, t, r, b, bgPaint)

        paint.strokeWidth = strokeWidth1
        paintBig.strokeWidth = strokeWidth2

        if (drawRight) {
            if ((y + 1) % 3 == 0) {
                canvas?.drawLine(r, t, r, b, paintBig)
            } else {
                canvas?.drawLine(r, t, r, b, paint)
            }
        }
        if (drawBottom) {
            if ((x + 1) % 3 == 0) {
                canvas?.drawLine(l, b, r, b, paintBig)
            } else {
                canvas?.drawLine(l, b, r, b, paint)
            }
        }
    }

}

