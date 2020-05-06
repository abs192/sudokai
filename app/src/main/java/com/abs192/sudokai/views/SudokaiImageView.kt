package com.abs192.sudokai.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import com.abs192.sudokai.R
import kotlin.math.roundToInt


class SudokaiImageView(context: Context?, attributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    private var pointA: Point = Point(0, 0)
    private var pointB: Point = Point(0, 0)
    private var pointC: Point = Point(0, 0)
    private var pointD: Point = Point(0, 0)

    private val framePaint = Paint()
    private val blurPaint = Paint()

    init {

        framePaint.color = context?.let { ContextCompat.getColor(it, R.color.colorAccentDark) }!!
        framePaint.style = Paint.Style.STROKE
//        framePaint.alpha = 100

        //TODO: blur out rest of the area.. no idea how to right now

        framePaint.strokeWidth = 10F
        framePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        framePaint.isAntiAlias = true

        blurPaint.color = Color.BLACK
        blurPaint.alpha = 100
        framePaint.style = Paint.Style.STROKE
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
        setPoints()
    }

    private fun setPoints() {
        val rect = getBitmapPositionInsideImageView()
        rect?.let {
            val l = rect[0]
            val t = rect[1]
            val r = rect[0] + rect[2]
            val b = rect[1] + rect[3]
            pointA = Point(l, t)
            pointB = Point(r, t)
            pointC = Point(l, b)
            pointD = Point(r, b)

        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        Log.d(javaClass.simpleName, "draw $pointA $pointB $pointC $pointD")

        canvas?.drawLine(
            pointA.x.toFloat(),
            pointA.y.toFloat(),
            pointB.x.toFloat(),
            pointB.y.toFloat(),
            framePaint
        )
        canvas?.drawLine(
            pointB.x.toFloat(),
            pointB.y.toFloat(),
            pointD.x.toFloat(),
            pointD.y.toFloat(),
            framePaint
        )
        canvas?.drawLine(
            pointD.x.toFloat(),
            pointD.y.toFloat(),
            pointC.x.toFloat(),
            pointC.y.toFloat(),
            framePaint
        )
        canvas?.drawLine(
            pointC.x.toFloat(),
            pointC.y.toFloat(),
            pointA.x.toFloat(),
            pointA.y.toFloat(),
            framePaint
        )
    }

    fun getDraggerPoints(s: String): Point? {
        when (s) {
            "1" ->
                return pointA
            "2" ->
                return pointB
            "3" ->
                return pointC
            "4" ->
                return pointD
        }
        return null
    }

    fun updateDraggers(s: String, dx: Point) {
        Log.d(javaClass.simpleName, "$s dx ${dx.x} ${dx.y}")
        Log.d(javaClass.simpleName, "update called $pointA $pointB $pointC $pointD")
        when (s) {
            "1" ->
                pointA = Point(pointA.x + dx.x, pointA.y + dx.y)
            "2" ->
                pointB = Point(pointB.x + dx.x, pointB.y + dx.y)
            "3" ->
                pointC = Point(pointC.x + dx.x, pointC.y + dx.y)
            "4" ->
                pointD = Point(pointD.x + dx.x, pointD.y + dx.y)
        }
        this.invalidate()
    }

    fun getBitmapPositionInsideImageView(): IntArray? {
        val ret = IntArray(4)
        if (this.drawable == null) return ret

        val f = FloatArray(9)
        imageMatrix.getValues(f)

        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]

        val origW = drawable.intrinsicWidth
        val origH = drawable.intrinsicHeight

        val actW = (origW * scaleX).roundToInt()
        val actH = (origH * scaleY).roundToInt()
        ret[2] = actW
        ret[3] = actH


        val top = (height - actH) / 2
        val left = (width - actW) / 2
        ret[0] = left
        ret[1] = top
        return ret
    }

    fun getPointsOfFrame(): Array<Point> {
        val pointArray = Array(4) { Point(0, 0) }
        pointArray[0] = pointA
        pointArray[1] = pointB
        pointArray[2] = pointC
        pointArray[3] = pointD

        pointArray.forEach {
            Log.d(javaClass.simpleName, "yo ${it.x} ${it.y}")
        }
        return pointArray
    }

}
