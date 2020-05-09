package com.abs192.sudokai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Size
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.abs192.sudokai.cvHelpers.CV2CanvasUtil
import com.abs192.sudokai.cvHelpers.CVImageHelper
import com.abs192.sudokai.storage.Storage
import com.abs192.sudokai.views.BlankCanvas
import com.abs192.sudokai.views.SudokaiImageDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.util.*


class MainActivity : AppCompatActivity(), SudokaiImageDialog.ImageCropListener {

    private val tag: String = this.javaClass.simpleName

    private val MY_PERMISSIONS_CAMERA: Int = 240
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mSurfaceView: SurfaceView? = null
    private var surfaceTextureListener: TextureView.SurfaceTextureListener? = null
    private var cameraManager: CameraManager? = null

    private var cameraFacing = 0

    private var cameraId: String? = null
    private var backgroundHandler: Handler? = null
    private var cameraDevice: CameraDevice? = null
    private var stateCallback: CameraDevice.StateCallback? = null
    private var textureView: TextureView? = null
    private var blankCanvas: BlankCanvas? = null
    private var startButton: FloatingActionButton? = null

    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var previewSize: Size? = null

    private var frameRectObj: FrameRect? = null

    private var storage = Storage(this)

    private val cv2CanvasUtil = CV2CanvasUtil()
    private val cvImageHelper = CVImageHelper()

    private var imgSize: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        setContentView(R.layout.activity_main)
        textureView = findViewById(R.id.texture_view)

        blankCanvas = findViewById(R.id.blank_canvas)
        blankCanvas?.bringToFront()

        val metrics: DisplayMetrics? = resources?.displayMetrics
        val w = metrics!!.widthPixels

        imgSize = w

        startButton = findViewById(R.id.fab_start)
        startButton?.setOnClickListener { onStartButtonClicked() }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK

        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                setUpCamera()
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            }
        }
        stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                this@MainActivity.cameraDevice = cameraDevice
                createPreviewSession()
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                cameraDevice.close()
                this@MainActivity.cameraDevice = null
            }

            override fun onError(cameraDevice: CameraDevice, error: Int) {
                cameraDevice.close()
                this@MainActivity.cameraDevice = null
            }
        }
    }

    private fun setUpCamera() {
        try {
            for (cameraId in cameraManager!!.cameraIdList) {
                val cameraCharacteristics =
                    cameraManager!!.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
                    val streamConfigurationMap =
                        cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                        )
                    previewSize =
                        streamConfigurationMap!!.getOutputSizes(SurfaceTexture::class.java)[0]
                    this.cameraId = cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                cameraManager!!.openCamera(cameraId!!, stateCallback!!, backgroundHandler)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_CAMERA
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setUpCamera()
                } else {
                    Snackbar
                        .make(
                            findViewById(R.id.relativeLayout),
                            "Camera permission not granted.",
                            Snackbar.LENGTH_LONG
                        ).show()
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private fun openBackgroundThread() {
        val backgroundThread = HandlerThread("sudokai_camera_background_thread")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    override fun onResume() {
        super.onResume()
        openBackgroundThread()
        if (textureView!!.isAvailable) {
            setUpCamera()
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = surfaceTextureListener
        }
        blankCanvas?.showFrame = true
        blankCanvas?.invalidate()
    }

    override fun onStop() {
        super.onStop()
        closeCamera()
        closeBackgroundThread()
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
        closeBackgroundThread()
    }

    private fun closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession!!.close()
            cameraCaptureSession = null
        }
        if (cameraDevice != null) {
            cameraDevice!!.close()
            cameraDevice = null
        }
    }

    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread?.quitSafely()
            backgroundThread = null
            backgroundHandler = null
        }
    }


    private fun createPreviewSession() {
        try {
            val surfaceTexture = textureView!!.surfaceTexture
            surfaceTexture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
            val previewSurface = Surface(surfaceTexture)

            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(previewSurface)
            cameraDevice!!.createCaptureSession(
                Collections.singletonList(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) {
                            return
                        }
                        try {
                            val captureRequest = captureRequestBuilder!!.build()
                            this@MainActivity.cameraCaptureSession = cameraCaptureSession
                            this@MainActivity.cameraCaptureSession!!.setRepeatingRequest(
                                captureRequest,
                                null, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {}
                }, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    var imageDialog: SudokaiImageDialog? = null

    private fun onStartButtonClicked() {
        Thread(Runnable {
            val frameBitmap: Bitmap? = textureView?.bitmap?.let { blankCanvas?.getFrameImage(it) }
            runOnUiThread {
                imageDialog = frameBitmap?.let { showImageDialog(it) }
                blankCanvas?.showFrame = false
                blankCanvas?.invalidate()
            }
        }).start()
    }

    private fun lock() {
        try {
            cameraCaptureSession!!.capture(
                captureRequestBuilder!!.build(),
                null, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    private fun unlock() {
        try {
            cameraCaptureSession!!.setRepeatingRequest(
                captureRequestBuilder!!.build(),
                null, backgroundHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    fun showImageDialog(bitmap: Bitmap): SudokaiImageDialog {
        val dialog = SudokaiImageDialog(this, this, bitmap)
        dialog.show()
        val window: Window? = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        window?.setGravity(Gravity.CENTER)
        return dialog
    }

    override fun cropDone(bitmap: Bitmap, points: Array<Point>) {
        Thread(Runnable {
            val sortedPoints = points.map { cv2CanvasUtil.getCVPoint(it) }
            val finalBitmap = cvImageHelper.wrapPerspective(sortedPoints, imgSize, bitmap)
            finalBitmap?.let { storage.saveImage(it) }
            runOnUiThread {
                val intent = Intent(this, ResultActivity::class.java)
                startActivity(intent)
                imageDialog?.dismiss()
            }
        }).start()

    }

    override fun cropCancelled() {
        blankCanvas?.showFrame = true
        blankCanvas?.invalidate()
    }
}
