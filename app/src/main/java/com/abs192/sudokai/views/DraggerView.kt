package com.abs192.sudokai.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.WindowInsets

class DraggerView(context: Context?, attributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    public var point: Point? = null
}
