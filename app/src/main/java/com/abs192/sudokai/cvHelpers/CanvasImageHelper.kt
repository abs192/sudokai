package com.abs192.sudokai.cvHelpers

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.widget.ImageView
import kotlin.math.roundToInt


class CanvasImageHelper {

    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: width, 3: height
     */
    fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray? {
        val ret = IntArray(4)
        if (imageView?.drawable == null) return ret
        // Get image dimensions
// Get image matrix values and place them in an array
        val f = FloatArray(9)
        imageView.imageMatrix.getValues(f)
        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]
        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        val d: Drawable = imageView.drawable
        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight
        // Calculate the actual dimensions
        val actW = (origW * scaleX).roundToInt()
        val actH = (origH * scaleY).roundToInt()
        ret[2] = actW
        ret[3] = actH
        // Get image position
// We assume that the image is centered into ImageView
        val imgViewW: Int = imageView.width
        val imgViewH: Int = imageView.height
        val top = (imgViewH - actH) / 2
        val left = (imgViewW - actW) / 2
        ret[0] = left
        ret[1] = top
        return ret
    }

}