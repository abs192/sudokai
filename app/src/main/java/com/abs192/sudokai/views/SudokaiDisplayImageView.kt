package com.abs192.sudokai.views

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.abs192.sudokai.R
import com.abs192.sudokai.storage.Storage
import kotlin.math.roundToInt


class SudokaiDisplayImageView(context: Context?) : View(context) {

    var xToShow = -1
    var yToShow = -1
    var oldSize = 0

    var newSize = 0
    private val PROPERTY_PADDING_SIZE = "anim_padding"

    private val paint = Paint()
    private var padSize = 9

    // Adding a gap of 9px
    // So new small square width = old square width - 10

    private var bitmapArray: ArrayList<Bitmap>

    private var srcRect: Rect

    private var animator: ValueAnimator = ValueAnimator()

    init {
        val storage = context?.let { Storage(it) }!!
        bitmapArray = arrayListOf()
        for (i in 0 until 81) {
            bitmapArray.add(storage.getSquareImage(i / 9, i % 9)!!)
        }
        oldSize = bitmapArray[0].width
        srcRect = Rect(0, 0, oldSize, oldSize)

        Log.d(javaClass.simpleName, "Size : $oldSize $newSize")
    }

    private fun initAnim() {
        val propertyPadding: PropertyValuesHolder =
            PropertyValuesHolder.ofInt(PROPERTY_PADDING_SIZE, 0, 9)

        animator.setValues(propertyPadding)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            padSize = animation.getAnimatedValue(PROPERTY_PADDING_SIZE) as Int
            newSize = oldSize - (padSize * 10 / 9)
            invalidate()
        }
        animator.start()
    }

    //TODO: Use this for displaying squares as well
    fun showPosition(x: Int, y: Int) {
        this.xToShow = x
        this.yToShow = y
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        initAnim()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        bitmapArray.forEachIndexed { index, bitmap ->

            val row = index % 9
            val col = index / 9
            val destRect = Rect(
                (padSize * (row + 1)) + (row * newSize),
                (padSize * (col + 1)) + (col * newSize),
                (padSize * (row + 1)) + ((row + 1) * newSize),
                (padSize * (col + 1)) + ((col + 1) * newSize)
            )
            Log.d(
                javaClass.simpleName,
                "$index $row $col ${destRect.left} ${destRect.right} ${destRect.top} ${destRect.bottom}"
            )
            canvas?.drawBitmap(
                bitmap,
                srcRect,
                destRect,
                paint
            )
        }
    }

}
