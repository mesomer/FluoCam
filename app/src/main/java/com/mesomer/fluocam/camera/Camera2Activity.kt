package com.mesomer.fluocam.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.R
import com.mesomer.fluocam.data.Photo
import com.mesomer.fluocam.myview.MySurfaceView
import com.mesomer.fluocam.science.Conversion
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates
private const val FILEPATH= "filepath"
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class Camera2Activity : AppCompatActivity() {
    private val mCameraId = "0"
    private lateinit var textureView: MySurfaceView
    private lateinit var cameraDevice: CameraDevice
    private lateinit var previewRequsetBuiler: CaptureRequest.Builder
    private lateinit var prviewRequest: CaptureRequest
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var imageReader: ImageReader
    private lateinit var container: RelativeLayout
    private lateinit var manager: CameraManager
    private lateinit var seekBar: SeekBar
    private lateinit var valueNow: TextView
    private lateinit var myCameraCharacter: MyCameraCharacter
    private lateinit var modeBotton:Button
    private lateinit var concentrationEditText:EditText
    private lateinit var groupIdEditText: EditText
    private lateinit var radio:RadioGroup
    private lateinit var myThread: MyThread
    private var settedISO=50
    private var settedEtime=Conversion().msTons(1)
    private var manualmod=false
    private var captureMode=true
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }
    private val imageReaderHandler = Handler(imageReaderThread.looper)
    private var minISO by Delegates.notNull<Int>()
    private var maxISO by Delegates.notNull<Int>()
    private var minExposure by Delegates.notNull<Long>()
    private var maxExposure by Delegates.notNull<Long>()
    private var rationStaus: Int = 1
    private var textcontent = ""
    private var db: AppDatabase?=null
    private var myDao: MyDAO?=null
    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {
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
            openCamera()
            Log.d("Camera2Activity", "openCamera()")
        }

    }
    private val stateCallBack = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            this@Camera2Activity.cameraDevice = camera
            createCameraPreviewSession()
            Log.d("Camera2Activity", "createCameraPreviewSession()")
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
    private fun openCamera() {
        setUpCameraOutputs()
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.openCamera(mCameraId, stateCallBack, null)
    }

    private fun createCameraPreviewSession() {
        val texture = textureView.surfaceTexture
        texture.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(texture)
        previewRequsetBuiler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        previewRequsetBuiler.addTarget(surface)

        cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object :
            CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@Camera2Activity, "配置失败", Toast.LENGTH_SHORT).show()
            }

            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                previewRequsetBuiler.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                previewRequsetBuiler.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                prviewRequest = previewRequsetBuiler.build()
                Log.d("Camera2Activity", "3A Mode")
                captureSession.setRepeatingRequest(prviewRequest, null, cameraHandler)
            }
        }, cameraHandler)
    }

    private fun captureStillPicture() {

        val captureRequestBulder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBulder.addTarget(imageReader.surface)
        captureRequestBulder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        val rotation = windowManager.defaultDisplay.rotation
        captureRequestBulder.set(CaptureRequest.JPEG_ORIENTATION, OREINTATIONS.get(rotation))
        if (manualmod){
            captureRequestBulder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_OFF)
            captureRequestBulder.set(CaptureRequest.SENSOR_SENSITIVITY, settedISO)
            captureRequestBulder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, settedEtime)
        }
        captureSession.stopRepeating()
        captureSession.capture(
            captureRequestBulder.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    previewRequsetBuiler.set(
                        CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
                    )
                    captureSession.setRepeatingRequest(prviewRequest, null,cameraHandler)
                }
            },
            null
        )
    }
    private fun setUpCameraOutputs() {
        manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = manager.getCameraCharacteristics(mCameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val largestJpg = Collections.max(listOf(*map!!.getOutputSizes(ImageFormat.JPEG)), ComparaSizesByArea())
        imageReader = ImageReader.newInstance(largestJpg.width, largestJpg.height, ImageFormat.JPEG, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            val fileJpg = File(externalMediaDirs.first(), "${getData(System.currentTimeMillis())}.jpg")
            buffer.get(bytes)
            image.use {
                FileOutputStream(fileJpg).use { output ->
                        output.write(bytes)
                        flashScreen()
                }
            }
            if (captureMode&&groupIdEditText.text.isNotEmpty()&&concentrationEditText.text.isNotEmpty()){
                sendMessage(0x111,filePath = fileJpg.path)
            }else if (groupIdEditText.text.isEmpty()||concentrationEditText.text.isEmpty()){
                Toast.makeText(this,"缺少信息，请手动添加",Toast.LENGTH_SHORT).show()
            }
        }, imageReaderHandler)
     }

    private class ComparaSizesByArea : Comparator<Size> {
        override fun compare(lhs: Size?, rhs: Size?): Int {
            return java.lang.Long.signum(lhs!!.width.toLong() * lhs.height - rhs!!.width.toLong() * rhs.height)
        }

    }

    private fun getData(currentTime: Long): String {
        val data = Date(currentTime)
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA)
        return format.format(data)
    }

    private fun flashScreen() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        textureView = findViewById(R.id.view_finder)
        container = findViewById(R.id.camera_ui_container)
        seekBar = findViewById(R.id.mySeekbar)
        modeBotton = findViewById(R.id.s_or_t)
        radio = findViewById(R.id.selecter)
        concentrationEditText=findViewById(R.id.concentration_text)
        valueNow = findViewById(R.id.lumo)
        groupIdEditText=findViewById(R.id.group_text)
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        myCameraCharacter = MyCameraCharacter(this@Camera2Activity)
        minISO = myCameraCharacter.lowerISO
        maxISO = 2000
        minExposure = myCameraCharacter.lowerExposure
        maxExposure = Conversion().msTons(2000)
        seekBar.max=1000
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        myThread = MyThread()
        myThread.start()
        seekBar.setOnSeekBarChangeListener(seekBarListener)
        radio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.etimeSelected -> {
                    rationStaus = 0
                    val etimenow=previewRequsetBuiler.get(CaptureRequest.SENSOR_EXPOSURE_TIME)!!
                    textcontent = "Exposure Time :${Conversion().nsToms(etimenow)}"
                    valueNow.text = textcontent
                    seekBar.progress=((etimenow-minExposure)*1000/(maxExposure-minExposure)).toInt()
                }

                R.id.isoSelected -> {
                    rationStaus = 1
                    val nowISO=previewRequsetBuiler.get(CaptureRequest.SENSOR_SENSITIVITY)!!
                    textcontent = "ISO :$nowISO"
                    valueNow.text = textcontent
                    seekBar.progress=(nowISO-minISO)*1000/(maxISO-minISO)
                }
            }
        }
        modeBotton.setOnClickListener {
            captureMode=!captureMode
            if (captureMode){
                modeBotton.text="标定"
                concentrationEditText.visibility=View.VISIBLE
            }
            else{
                modeBotton.text="检测"
                concentrationEditText.visibility= View.INVISIBLE
            }
        }
    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        @RequiresApi(Build.VERSION_CODES.M)
        var locked=true
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!locked){
                previewRequsetBuiler.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CameraMetadata.CONTROL_AE_MODE_OFF
                )
                previewRequsetBuiler.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                when (rationStaus) {
                    1 -> {
                        manualmod=true
                        settedISO = minISO + progress * (maxISO - minISO) / 1000
                        previewRequsetBuiler.set(CaptureRequest.SENSOR_SENSITIVITY, settedISO)
                        textcontent = "ISO: $settedISO"
                        valueNow.text = textcontent
                    }
                    0 -> {
                        manualmod=true
                        settedEtime = minExposure + progress * (maxExposure - minExposure) / 1000
                        previewRequsetBuiler.set(CaptureRequest.SENSOR_EXPOSURE_TIME, settedEtime)
                        textcontent = "Exposure Time: ${Conversion().nsToms(settedEtime)}"
                        valueNow.text = textcontent
                    }
                }
                prviewRequest = previewRequsetBuiler.build()
                captureSession.setRepeatingRequest(prviewRequest, null, cameraHandler)
                Log.d("Camera2Activity", "ManualMode")
            }
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            locked=false
        }
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            locked=true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("ActivityCamera2", "--Startted--")

    }

    override fun onDestroy() {
        Log.d("ActivityCamera2", "--Destroyed--")
        super.onDestroy()
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }

    override fun onResume() {
        Log.d("ActivityCamera2", "--Resumed--")
        super.onResume()
    }

    @SuppressLint("MissingPermission")
    override fun onRestart() {
        super.onRestart()
        rationStaus = 1
        manager.openCamera(mCameraId, stateCallBack, null)
        Log.d("ActivityCamera2", "--Restarted--")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ActivityCamera2", "--Paused--")
    }

    override fun onStop() {
        super.onStop()
        cameraDevice.close()
        Log.d("ActivityCamera2", "--stopped--")
    }

    companion object {
        private const val ANIMATION_FAST_MILLIS = 50L
        private const val ANIMATION_SLOW_MILLIS = 100L
        private val OREINTATIONS = SparseIntArray()

        init {
            OREINTATIONS.append(Surface.ROTATION_0, 90)
            OREINTATIONS.append(Surface.ROTATION_90, 0)
            OREINTATIONS.append(Surface.ROTATION_180, 270)
            OREINTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                textureView.surfaceTextureListener = mSurfaceTextureListener
                openCamera()
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

    internal inner class MyThread : Thread(){
        lateinit var mHandler:Handler
        override fun run() {
            Looper.prepare()
            mHandler= @SuppressLint("HandlerLeak")
            object : Handler(){
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                    if (msg?.what==0x111){
                        val phtoto = Photo(
                            concentration = concentrationEditText.text.toString(),
                            groupID = groupIdEditText.text.toString(),
                            IsStander = captureMode,
                            name = "",
                            path = msg.data.getString(FILEPATH)!!
                        )
                        myDao!!.insertPhoto(phtoto)
                    }
                }
            }
        }
    }
    private fun sendMessage(msgnum: Int =0x111, filePath: String) {
        val msg = Message()
        msg.what = msgnum
        val bundle = Bundle()
        bundle.putString(FILEPATH, filePath)
        msg.data = bundle
        myThread.mHandler.sendMessage(msg)
    }
}

