package com.mesomer.fluocam.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Camera
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mesomer.fluocam.R
import com.mesomer.fluocam.myview.MySurfaceView
import kotlinx.android.synthetic.main.activity_camera2.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)
class Camera2Activity : AppCompatActivity() {
    private val mCameraId="0"
    private lateinit var textureView: MySurfaceView
    private lateinit var cameraDevice:CameraDevice
    private lateinit var previewRequsetBuiler:CaptureRequest.Builder
    private lateinit var prviewRequest:CaptureRequest
    private lateinit var captureSession:CameraCaptureSession
    private lateinit var previewSize:Size
    private lateinit var imageReader:ImageReader
    private lateinit var container:RelativeLayout

    private val mSurfaceTextureListener = object :TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera(width,height)
            Log.d("Camera2Activity","openCamera()")
        }

    }
   private val stateCallBack=object : CameraDevice.StateCallback(){
       override fun onOpened(camera: CameraDevice) {
           this@Camera2Activity.cameraDevice=camera
           createCameraPreviewSession()
       }
       override fun onDisconnected(camera: CameraDevice) {
           cameraDevice.close()
       }

       override fun onError(camera: CameraDevice, error: Int) {
           cameraDevice.close()
           this@Camera2Activity.finish()
       }
   }

    @SuppressLint("MissingPermission")
    private fun openCamera(width:Int, height:Int){
        setUpCameraOutputs(width,height)
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.openCamera(mCameraId,stateCallBack,null)
    }
    private fun createCameraPreviewSession(){
        val texture = textureView.surfaceTexture
        texture.setDefaultBufferSize(textureView.width,textureView.height)
        val surface = Surface(texture)

        previewRequsetBuiler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequsetBuiler.addTarget(surface)
        cameraDevice.createCaptureSession(Arrays.asList(surface,imageReader.surface),object :
            CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@Camera2Activity,"配置失败",Toast.LENGTH_SHORT).show()
            }

            override fun onConfigured(session: CameraCaptureSession) {

                captureSession = session
                previewRequsetBuiler.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                previewRequsetBuiler.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO)
                prviewRequest=previewRequsetBuiler.build()
                captureSession.setRepeatingRequest(prviewRequest,null,null)
            }

        },null)
    }
    private fun captureStillPicture() {

        val captureRequestBulder= cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBulder.addTarget(imageReader.surface)
        captureRequestBulder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        captureRequestBulder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        val rotation = windowManager.defaultDisplay.rotation
        captureRequestBulder.set(CaptureRequest.JPEG_ORIENTATION, OREINTATIONS.get(rotation))
        captureSession.stopRepeating()
        captureSession.capture(captureRequestBulder.build(),object :CameraCaptureSession.CaptureCallback(){
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                previewRequsetBuiler.set(CaptureRequest.CONTROL_AF_TRIGGER,CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
                previewRequsetBuiler.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                captureSession.setRepeatingRequest(prviewRequest,null,null)
            }
        },null)
    }

    private fun setUpCameraOutputs(width: Int,height: Int){
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = manager.getCameraCharacteristics(mCameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val largest = Collections.max(listOf(*map.getOutputSizes(ImageFormat.JPEG)),ComparaSizesByArea())
        imageReader= ImageReader.newInstance(largest.width,largest.width,ImageFormat.JPEG,2)
        imageReader.setOnImageAvailableListener({
            reader ->
            val image = reader.acquireNextImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            val file= File(externalMediaDirs.first(), "${getData(System.currentTimeMillis())}.jpg")
            buffer.get(bytes)
            image.use {_->
                FileOutputStream(file).use {
                    output->
                    output.write(bytes)
                    flashScreen()
                }
            }
        },null)
    }
    private class ComparaSizesByArea : Comparator<Size>{
        override fun compare(lhs: Size?, rhs: Size?): Int {
            return java.lang.Long.signum(lhs!!.width.toLong()*lhs.height-rhs!!.width.toLong()*rhs.height)
        }

    }
    private fun getData(currenttime: Long): String {
        val data = Date(currenttime)
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA)
        return format.format(data)
    }
    private fun flashScreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val container=findViewById<RelativeLayout>(R.id.camera_ui_container)
            // Display flash animation to indicate that photo was captured
            container.postDelayed({
                container.foreground = ColorDrawable(Color.WHITE)
                container.postDelayed(
                    { container.foreground = null }, ANIMATION_FAST_MILLIS
                )
            }, ANIMATION_SLOW_MILLIS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        textureView=findViewById(R.id.view_finder)

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)

    }
    companion object{
        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
        private val OREINTATIONS = SparseIntArray()
        init {
            OREINTATIONS.append(Surface.ROTATION_0,90)
            OREINTATIONS.append(Surface.ROTATION_90,0)
            OREINTATIONS.append(Surface.ROTATION_180,270)
            OREINTATIONS.append(Surface.ROTATION_270,180)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                textureView.surfaceTextureListener=mSurfaceTextureListener
                openCamera(textureView.width,textureView.height)
                findViewById<ImageButton>(R.id.capture).setOnClickListener {
                    captureStillPicture()
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
}
