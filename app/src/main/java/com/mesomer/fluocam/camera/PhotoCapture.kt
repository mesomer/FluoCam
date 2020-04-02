package com.mesomer.fluocam.camera


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.extensions.BokehPreviewExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.mesomer.fluocam.R
import com.mesomer.fluocam.myview.MyPreviewView
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

//跟踪权限请求
private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class PhotoCapture : AppCompatActivity(), LifecycleOwner {

    private lateinit var container: RelativeLayout
    private lateinit var outputDirectory: File

    private lateinit var viewFinder: MyPreviewView

    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var lastTouchx: Float = 0f
    private var lastTouchy: Float = 0f
    private var displayId: Int = -1
    private lateinit var luminotext: TextView
    private val imageCaptureBuilder = ImageCapture.Builder()
    private lateinit var s_or_tButton: Button
    private lateinit var captureButton: ImageButton
    private lateinit var mySeekBar: SeekBar
    private lateinit var bokehPreview: BokehPreviewExtender
    private var manualMod = false
    private var changed = false
    //true for standard
    private var s_or_t = true

    private val displayManager by lazy {
        this.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }


    private lateinit var cameraExecutor: ExecutorService

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = viewFinder.let { view ->
            if (displayId == this@PhotoCapture.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_capture)
        container = findViewById(R.id.camera_ui_container)
        viewFinder = findViewById(R.id.view_finder)
        s_or_tButton = findViewById(R.id.s_or_t)
        cameraExecutor = Executors.newSingleThreadExecutor()
        displayManager.registerDisplayListener(displayListener, null)
        luminotext = findViewById(R.id.lumo)
        captureButton = findViewById(R.id.capture)
        outputDirectory = getOutputDirectory(this)
        mySeekBar = findViewById(R.id.mySeekbar)
        mySeekBar.max = 5000
        MyCameraCharacter.getHardWareLevel(this)
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        } else {
            viewFinder.post {
                // Keep track of the display in which this view is attached
                displayId = viewFinder.display.displayId
                // Build UI controls
                updateCameraUi()
                // Bind use cases
                bindCameraUseCases()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }


    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        //暂时固定为4：3
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        val rotation = viewFinder.display.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@PhotoCapture)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val previewBuilder = Preview.Builder()
            val previewExtender = Camera2Interop.Extender(previewBuilder)
            val imageCaptureBuilder=ImageCapture.Builder()
            val imageCaptureExtender = Camera2Interop.Extender(imageCaptureBuilder)

            previewExtender.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE,CaptureRequest.CONTROL_AWB_MODE_OFF)
            previewExtender.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE,CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT)
            imageCaptureExtender.setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE,CaptureRequest.CONTROL_AWB_MODE_AUTO)
            
            preview = previewBuilder
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
            imageCapture = imageCaptureBuilder.apply {
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(rotation)
            }.build()

            preview?.setSurfaceProvider(viewFinder.previewSurfaceProvider)
            imageAnalyzer = ImageAnalysis.Builder().apply {
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(rotation)
            }.build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.i(TAG, "Average luminosity:$luma")
                    })
                }
            cameraProvider.unbindAll()
            try {
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this@PhotoCapture))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        /*
        val previewRatio = max(width, height) / min(width, height)
        return if (kotlin.math.abs(previewRatio - RATIO_4_3) <= kotlin.math.abs(previewRatio - RATIO_16_9)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
        */
        return AspectRatio.RATIO_4_3
    }

    private fun getData(currenttime: Long): String {
        val data = Date(currenttime)
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA)
        return format.format(data)
    }

    @SuppressLint("ClickableViewAccessibility", "RestrictedApi")
    private fun updateCameraUi() {
        container.findViewById<RelativeLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        captureButton.setOnClickListener {
            val photoFile = File(
                externalMediaDirs.first(),
                "${getData(System.currentTimeMillis())}.jpg"
            )
            val metadata = ImageCapture.Metadata().apply {
                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }
            val outputOption =
                ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build()
            imageCapture!!.takePicture(
                outputOption,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                        Log.d(TAG + "photo_capture", "Photo capture succeeded: $savedUri")
                    }
                })
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Display flash animation to indicate that photo was captured
                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed(
                        { container.foreground = null }, ANIMATION_FAST_MILLIS
                    )
                }, ANIMATION_SLOW_MILLIS)
            }
        }

        viewFinder.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                return@setOnTouchListener false
            }
            if (event.x != lastTouchx && event.y != lastTouchy) {
                val cameraControl = camera?.cameraControl
                cameraControl?.cancelFocusAndMetering()
                val display = viewFinder.display
                val factory = DisplayOrientedMeteringPointFactory(
                    display,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    display.width.toFloat(),
                    display.height.toFloat()
                )
                val point = factory.createPoint(event.x, event.y)
                Toast.makeText(this, "${event.x},${event.y}", Toast.LENGTH_LONG).show()
                val action = FocusMeteringAction.Builder(point).build()
                cameraControl?.startFocusAndMetering(action)
                lastTouchx = event.x
                lastTouchy = event.y
                val co = "当前亮度" + lumino
                luminotext.text = co
            }
            return@setOnTouchListener true
        }
        s_or_tButton.setOnClickListener {
            if (s_or_t) {
                s_or_t = false
                s_or_tButton.text = "分析"
            } else {
                s_or_t = true
                s_or_tButton.text = "标定"
            }
        }
        mySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                /*
                val Camera2Interop= Camera2Interop.Extender(previewBuilder)
                //Camera2Interop.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF)
                Camera2Interop.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY,5000)
                Camera2Interop.setCaptureRequestOption(CaptureRequest.SENSOR_FRAME_DURATION, 5000)
                preview=previewBuilder.build()
                manualMod=true
                bindCameraUseCases()
                */
                if (!changed) {
                    val camera2Interop = Camera2Interop.Extender(imageCaptureBuilder)
                    //amera2Interop.setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_OFF)
                    camera2Interop.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AWB_MODE,
                        CameraMetadata.CONTROL_AWB_MODE_OFF
                    )
                    camera2Interop.setCaptureRequestOption(
                        CaptureRequest.CONTROL_AF_MODE,
                        CameraMetadata.CONTROL_AF_MODE_OFF
                    )
                    camera2Interop.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, 1000)
                    camera2Interop.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, 250)
                    imageCapture = imageCaptureBuilder.build()
                    bindCameraUseCases()
                    changed = true
                } else
                    changed = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post {
                    bindCameraUseCases()
                    updateCameraUi()
                }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private class LuminosityAnalyzer(listener: LumaListener? = null) : ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) {
                image.close()
                return
            }
            // Keep track of frames analyzed
            val currentTime = System.currentTimeMillis()
            frameTimestamps.push(currentTime)

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            val timestampFirst = frameTimestamps.peekFirst() ?: currentTime
            val timestampLast = frameTimestamps.peekLast() ?: currentTime
            framesPerSecond = 1.0 / ((timestampFirst - timestampLast) /
                    frameTimestamps.size.coerceAtLeast(1).toDouble()) * 1000.0

            // Analysis could take an arbitrarily long amount of time
            // Since we are running in a different thread, it won't stall other use cases

            lastAnalyzedTimestamp = frameTimestamps.first

            // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance plane
            val buffer = image.planes[0].buffer

            // Extract image data from callback object
            val data = buffer.toByteArray()

            // Convert the data into an array of pixel values ranging 0-255
            val pixels = data.map { it.toInt() and 0xFF }

            // Compute average luminance for the image
            val luma = pixels.average()
            lumino = luma
            // Call all listeners with new value
            listeners.forEach { it(luma) }

            image.close()
        }
    }

    companion object {

        private const val TAG = "FluoCam"
        private const val RATIO_4_3 = 4.0 / 3.0
        private const val RATIO_16_9 = 16.0 / 9.0
        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
        private var lumino = 0.0
        private fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}


