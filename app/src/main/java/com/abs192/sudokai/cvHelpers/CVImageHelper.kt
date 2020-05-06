package com.abs192.sudokai.cvHelpers

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters


class CVImageHelper {

    private val tag = this.javaClass.toString()

    private fun getContours(matSrc: Mat): List<MatOfPoint> {

        val contours: List<MatOfPoint> = ArrayList()
        Imgproc.findContours(
            matSrc,
            contours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )
        return contours
    }

    private fun readBoard(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 4)

        val original = Mat()
        mat.copyTo(original)

        Imgproc.GaussianBlur(mat, mat, Size(3.0, 3.0), 1.0)
        Imgproc.Canny(mat, mat, 80.0, 100.0)

        val contours = getContours(mat)

        var maxVal = 0.0
        var maxValIdx = -1
        for (contourIdx in contours.indices) {
            val contourArea = Imgproc.contourArea(contours[contourIdx])
            if (maxVal < contourArea) {
                maxVal = contourArea
                maxValIdx = contourIdx
            }
        }

        if (maxValIdx == -1) {
            return original
        }
        val boardMatOfPoint = contours[maxValIdx]
        return Mat(original, Imgproc.boundingRect(boardMatOfPoint))
    }
//
//    private fun readSquares(mat: Mat) {
//
//        val squares =
//

//        val boardMatOfPoint = contours[maxValIdx]
//        val cornerPoints = ArrayList<Point>()
//        if (contours.isNotEmpty()) {
//            Log.d(tag, "board: ${boardMatOfPoint.size()}")
//            Log.d(tag, "board: ${boardMatOfPoint[0, 0]}")
//
//            val corners = findCorners(boardMatOfPoint.toArray())
//            Log.d(tag, "corners: $corners")
//
//            cornerPoints.add(Point(corners[0], corners[1]))
//            cornerPoints.add(Point(corners[0], corners[3]))
//            cornerPoints.add(Point(corners[2], corners[1]))
//            cornerPoints.add(Point(corners[2], corners[3]))
//            cornerPoints.forEach {
//                Imgproc.circle(
//                    mat, it,
//                    10,
//                    Scalar(255.0, 0.0, 0.0),
//                    5
//                )
//            }
//
//            val allSquares = findSquares(original, cornerPoints)
//
//            for ((i, a) in allSquares.withIndex()) {
//                val width =
//                    a[2].toInt() - a[0].toInt()
//                val height =
//                    a[3].toInt() - a[1].toInt()
//                val roi = Rect(
//                    a[0].toInt(),
//                    a[1].toInt(), width, height
//                )
//                val squareA = Mat(mat, roi)
//                val squareBitmap =
//                    Bitmap.createBitmap(squareA.cols(), squareA.rows(), Bitmap.Config.ARGB_8888)
//
//                Utils.matToBitmap(squareA, squareBitmap)
////                val result = classifier.recognizeImage(squareBitmap)
////                Log.d(tag, "result $i " + result[0].title)
//            }
//    }


    private fun get81Squares(mat: Mat): ArrayList<Rect> {

        val xWidth = mat.rows() / 9
        val yHeight = mat.cols() / 9

        Log.d(tag, "x $xWidth y $yHeight")
        if (xWidth < 9 || yHeight < 9) {
            return ArrayList()
        }

        val rects = ArrayList<Rect>()
        for (j in 0 until 9) {
            for (i in 0 until 9) {
                val x = i.toDouble() * xWidth
                val y = j.toDouble() * yHeight
                rects.add(
//                    Rect(
//                        Point(x, y),
//                        Point(x + xWidth, y + yHeight)
//                    )
                    Rect(x.toInt(), y.toInt(), xWidth, yHeight)
                )
            }
        }
        return rects
    }

    fun roi(bitmap: Bitmap, boundingRect: Rect): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        val rect1 = Mat(mat, boundingRect)
        val resultBitmap =
            Bitmap.createBitmap(rect1.cols(), rect1.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rect1, resultBitmap)
        return resultBitmap
    }

    fun getSquares(bitmap: Bitmap): ArrayList<Bitmap> {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        val array = get81Squares(mat).map {
            transformMatToBitmap(it, mat)
        }
        return ArrayList(array)
    }

    private fun transformMatToBitmap(roi: Rect, mat: Mat): Bitmap {
        Log.d(tag, "mat : ${mat.rows()} ${mat.cols()}")
        Log.d(tag, "roi : ${roi.x} ${roi.y} ${roi.height} ${roi.width}")
        val roiMat = Mat(mat, roi)
        val roiBitmap =
            Bitmap.createBitmap(roiMat.cols(), roiMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(roiMat, roiBitmap)
        return roiBitmap
    }

    fun wrapPerspective(sortedPoints: List<Point>, size: Int, bitmap: Bitmap): Bitmap? {

        sortedPoints.forEach {
            Log.d(javaClass.simpleName, "${it.x} ${it.y}")
        }
        val originalImg = Mat()
        Utils.bitmapToMat(bitmap, originalImg)

        val dst = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(size.toDouble() , 0.0),
            Point(0.0, size.toDouble()),
            Point(size.toDouble(), size.toDouble())
        )

        val destImage = Mat(Size(size.toDouble(), size.toDouble()), CvType.CV_8UC4)
        val warpMat = Imgproc.getPerspectiveTransform(
            Converters.vector_Point2f_to_Mat(sortedPoints), dst
        )

        Imgproc.warpPerspective(
            originalImg,
            destImage,
            warpMat,
            originalImg.size(),
            Imgproc.INTER_CUBIC
        )

        val resBitmap =
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(destImage, resBitmap)
        return resBitmap
    }

    fun preprocess(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY, 4)

        val original = Mat()
        mat.copyTo(original)

        Imgproc.GaussianBlur(mat, mat, Size(3.0, 3.0), 1.0)

        val resBitmap =
            Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, resBitmap)
        return resBitmap
    }

}
