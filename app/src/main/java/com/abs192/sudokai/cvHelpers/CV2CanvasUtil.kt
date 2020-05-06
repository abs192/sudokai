package com.abs192.sudokai.cvHelpers

import org.opencv.core.Point
import org.opencv.core.Rect

class CV2CanvasUtil {

    fun getCanvasRect(rect: Rect): android.graphics.Rect {
        return android.graphics.Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height)
    }

    fun getCVRect(rect: android.graphics.Rect): Rect {
        return Rect(rect.left, rect.top, rect.width(), rect.height())
    }

    fun getCVPoint(point: android.graphics.Point): Point {
        return Point(point.x.toDouble(), point.y.toDouble())
    }
}