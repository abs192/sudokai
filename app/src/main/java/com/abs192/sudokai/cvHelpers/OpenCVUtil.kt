package com.abs192.sudokai.cvHelpers

import android.content.Context
import android.util.Log
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader

class OpenCVUtil {

    fun initOpenCV(
        tag: String,
        context: Context,
        loaderCallBackInterface: LoaderCallbackInterface
    ): Boolean {
        try {
            if (!OpenCVLoader.initDebug()) {
                Log.d(
                    tag,
                    "Internal OpenCV library not found. Using OpenCV Manager for initialization"
                )
                OpenCVLoader.initAsync(
                    OpenCVLoader.OPENCV_VERSION_3_4_0,
                    context,
                    loaderCallBackInterface
                )
            } else {
                Log.d(tag, "OpenCV library found inside package. Using it!")
                loaderCallBackInterface.onManagerConnected(LoaderCallbackInterface.SUCCESS)
            }
        } catch (e: java.lang.Exception) {
            Log.e(tag, "Error init opencv" + e.message)
            return false
        }
        return true
    }
}