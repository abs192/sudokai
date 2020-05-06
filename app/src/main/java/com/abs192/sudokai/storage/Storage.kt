package com.abs192.sudokai.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


class Storage(private val context: Context) {

    private val imgFileName = "sudokai_img_bitmap_file"
    private val squareImgFileName = "sudokai_square_img_bitmap_file"

    fun saveImage(bitmap: Bitmap) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, bos)
        val bitmapData: ByteArray = bos.toByteArray()
        context.openFileOutput(imgFileName, Context.MODE_PRIVATE).use {
            it.write(bitmapData)
        }
    }

    fun saveSquareImage(bitmap: Bitmap, numberX: Int, numberY : Int) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, bos)
        val bitmapData: ByteArray = bos.toByteArray()
        context.openFileOutput(squareImgFileName + "$numberX$numberY", Context.MODE_PRIVATE).use {
            it.write(bitmapData)
        }
    }

    fun getSquareImage(numberX: Int, numberY: Int): Bitmap? {
        return context.openFileInput(squareImgFileName + "$numberX$numberY").use {
            BitmapFactory.decodeStream(it)
        }
    }


    fun getImage(): Bitmap? {
        return context.openFileInput(imgFileName).use {
            BitmapFactory.decodeStream(it)
        }
    }

}