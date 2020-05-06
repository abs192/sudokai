package com.abs192.sudokai

import android.util.Log

class FrameRect(var displayWidth: Int, var displayHeight: Int) {

    var x: Int = 0
    var y: Int = 0

    var width: Int = 0
    var height: Int = 0

    var minWidth: Int
    private var minHeight: Int
    var maxWidth: Int
    private var maxHeight: Int

    init {
        width = displayWidth
        height = width

        minWidth = width
        minHeight = width
        maxWidth = displayWidth
        maxHeight = displayWidth
        calculate()
    }

    private fun calculate() {
        Log.d(
            "asas", "$width, $height}"
        )
        Log.d(
            "asas", "$minWidth, $maxWidth}"
        )

        x = (displayWidth / 2) - (width / 2)
        y = (displayHeight / 2) - (height / 2)
    }

}