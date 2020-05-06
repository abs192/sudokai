package com.abs192.sudokai.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.abs192.sudokai.R


class SudokaiImageDialog(
    context: Context, private var listener: ImageCropListener, private var bitmap: Bitmap
) :
    Dialog(context) {

    private var imageView: SudokaiImageView? = null
    private var hintTextView: TextView? = null
    private var dragger1: ImageView? = null
    private var dragger2: ImageView? = null
    private var dragger3: ImageView? = null
    private var dragger4: ImageView? = null

    private var pointA = Point(0, 0)
    private var pointB = Point(0, 0)
    private var pointC = Point(0, 0)
    private var pointD = Point(0, 0)

    private var imgButtonDiscard: ImageButton? = null
    private var imgButtonSave: ImageButton? = null

    private var progressBar: ProgressBar? = null

    var dX: Float? = null
    var dY: Float? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.layout_image_dialog)
        imageView = findViewById(R.id.sudokaiImageView)
        hintTextView = findViewById(R.id.textHint)

        dragger1 = findViewById(R.id.dragger1)
        dragger2 = findViewById(R.id.dragger2)
        dragger3 = findViewById(R.id.dragger3)
        dragger4 = findViewById(R.id.dragger4)
        imageView?.setImageBitmap(bitmap)

        val imageCoords = getCoordinates(imageView as View)
        Log.d(javaClass.simpleName, "IMG COORDS ${imageCoords.x} ${imageCoords.y}")
        setDraggersPos()

        val onTouchListener = View.OnTouchListener { view, motionEvent ->
            view.performClick()

            if (motionEvent.action == MotionEvent.ACTION_UP) {
                updateImageView(view.tag, getCoordinates(view))
                setDraggersPos()
            }
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                setDraggersPos()
                dX = view.x - motionEvent.rawX
                dY = view.y - motionEvent.rawY
            }
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                val newX = motionEvent.rawX + dX!!
                val newY = motionEvent.rawY + dY!!
                view.animate().x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start()
                updateImageView(view.tag, getCoordinates(view))
                setDraggersPos()
            }
            return@OnTouchListener true
        }

        dragger1?.setOnTouchListener(onTouchListener)
        dragger2?.setOnTouchListener(onTouchListener)
        dragger3?.setOnTouchListener(onTouchListener)
        dragger4?.setOnTouchListener(onTouchListener)

        imgButtonDiscard = findViewById(R.id.imageDialogLayoutDiscard)
        imgButtonSave = findViewById(R.id.imageDialogLayoutSave)

        imgButtonDiscard?.setOnClickListener {
            this.dismiss()
            listener.cropCancelled()
        }

        imgButtonSave?.setOnClickListener {
            imageView?.getPointsOfFrame()?.let { listener.cropDone(bitmap, it) }
            imageView?.alpha = 0.5F
            hintTextView?.alpha = 0.5F
            progressBar?.visibility = View.VISIBLE

            imgButtonDiscard?.isEnabled = false
            imgButtonSave?.isEnabled = false
        }

        // Try removing this later
        updateImageView("1", Point(0, 0))
    }

    private fun setDraggersPos() {
        dragger1?.let { getCoordinates(it) }?.let { pointA = it }
        dragger2?.let { getCoordinates(it) }?.let { pointB = it }
        dragger3?.let { getCoordinates(it) }?.let { pointC = it }
        dragger4?.let { getCoordinates(it) }?.let { pointD = it }
    }

    private fun updateImageView(tag: Any?, coordinates: Point) {
        Log.d(javaClass.simpleName, "update ${coordinates.x} ${coordinates.y}")
        val p = getPoint(tag as String)
        val dxX = p?.x?.let { coordinates.x.minus(it) }
        val dxY = p?.y?.let { coordinates.y.minus(it) }

        val dx = Point(dxX!!, dxY!!)
        imageView?.updateDraggers(tag, dx)
//        updatePoint(tag, coordinates)
    }

    private fun getPoint(s: String): Point? {
        when (s) {
            "1" -> return pointA
            "2" -> return pointB
            "3" -> return pointC
            "4" -> return pointD
        }
        return null
    }

    private fun updatePoint(s: String, point: Point) {
        when (s) {
            "1" -> pointA = Point(point)
            "2" -> pointB = Point(point)
            "3" -> pointC = Point(point)
            "4" -> pointD = Point(point)
        }
    }

    private fun getCoordinates(v: View): Point {

        val point = IntArray(2)
        v.getLocationInWindow(point)
//        Log.d(this.javaClass.simpleName, "dragger w ${point[0]} ${point[1]}")
        return Point(point[0], point[1])
    }

    override fun onBackPressed() {
        super.onBackPressed()
        dismiss()
        listener.cropCancelled()
    }

    interface ImageCropListener {
        fun cropDone(bitmap: Bitmap, points: Array<Point>)
        fun cropCancelled()
    }

}