package com.abs192.sudokai.views

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.abs192.sudokai.FrameRect
import com.abs192.sudokai.R
import com.abs192.sudokai.cvHelpers.CV2CanvasUtil
import com.abs192.sudokai.cvHelpers.CVImageHelper
import com.abs192.sudokai.cvHelpers.OpenCVUtil
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface


class BlankCanvas(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {

    private val tag = this.accessibilityClassName.toString()

    private val frameCornerRadius = 5F

    private val metrics: DisplayMetrics? = context?.resources?.displayMetrics

    private var displayWidth = 0
    private var displayHeight = 0

    private val cvImageHelper = CVImageHelper()
    private val cV2CanvasUtil = CV2CanvasUtil()
    private val framePaint = Paint()

    var showFrame = true

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(tag, "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    private lateinit var frameRect: Rect
    private lateinit var frameRectObj: FrameRect

    private val PROPERTY_FRAME_RADIUS = "radius"
    private val PROPERTY_FRAME_STROKE_WIDTH = "stroke_width"
    private val PROPERTY_FRAME_ROTATE = "rotate"

    private var animator: ValueAnimator

    var frameSize = 0
    private var rotate = 0
    var strokeWidth = 0

    init {
        displayWidth = metrics!!.widthPixels
        displayHeight = metrics.heightPixels

        this.setBackgroundColor(Color.TRANSPARENT)
        context?.let { OpenCVUtil().initOpenCV(tag, it, mLoaderCallback) }

        framePaint.color = context?.let { ContextCompat.getColor(it, R.color.colorAccentDark) }!!
        framePaint.style = Paint.Style.STROKE

        val propertyRadius: PropertyValuesHolder =
            PropertyValuesHolder.ofInt(PROPERTY_FRAME_RADIUS, 0, metrics!!.widthPixels / 2)
        val propertyStrokeWidth: PropertyValuesHolder =
            PropertyValuesHolder.ofInt(PROPERTY_FRAME_STROKE_WIDTH, 500, 5)
        val propertyRotate: PropertyValuesHolder =
            PropertyValuesHolder.ofInt(PROPERTY_FRAME_ROTATE, 0, 360)

        animator = ValueAnimator()
        animator.setValues(propertyRadius, propertyRotate, propertyStrokeWidth)
        animator.duration = 250
        animator.addUpdateListener { animation ->
            frameSize = animation.getAnimatedValue(PROPERTY_FRAME_RADIUS) as Int
            rotate = animation.getAnimatedValue(PROPERTY_FRAME_ROTATE) as Int
            strokeWidth = animation.getAnimatedValue(PROPERTY_FRAME_STROKE_WIDTH) as Int
            invalidate()
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        drawFrame(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setFrameRect()
    }

    private fun setFrameRect() {

        frameRectObj = FrameRect(displayWidth, displayHeight)
        frameRect = Rect(
            frameRectObj.x,
            frameRectObj.y,
            frameRectObj.x + frameRectObj.width,
            frameRectObj.y + frameRectObj.height
        )
        animator.start()
    }

    private fun drawFrame(canvas: Canvas?) {

        framePaint.strokeWidth = strokeWidth.toFloat()
        val viewWidth = displayWidth / 2
        val viewHeight = displayHeight / 2

        val leftTopX = viewWidth - frameSize
        val leftTopY = viewHeight - frameSize

        val rightBotX = viewWidth + frameSize
        val rightBotY = viewHeight + frameSize

        if (showFrame) {
            canvas?.drawRoundRect(
                leftTopX.toFloat(),
                leftTopY.toFloat(),
                rightBotX.toFloat(),
                rightBotY.toFloat(),
                frameCornerRadius,
                frameCornerRadius,
                framePaint
            )
        }
    }

    fun getFrameImage(bitmap: Bitmap): Bitmap? {
        Log.d(tag, "h w : ${frameRect.height()} ${frameRect.width()}")
        Log.d(
            tag,
            "l r t b : ${frameRect.left} ${frameRect.right} ${frameRect.top} ${frameRect.bottom}"
        )
        return cvImageHelper.roi(bitmap, cV2CanvasUtil.getCVRect(frameRect))
    }

}