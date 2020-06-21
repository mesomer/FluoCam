package com.mesomer.fluocam.camera

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range

class MyCameraCharacter(activity: Activity) {
        private val manager:CameraManager
    init {
         manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val characteristics = manager.getCameraCharacteristics("0")
    val upperISO=getISoRange().upper
    val lowerISO=getISoRange().lower
    val upperExposure=getExposureRange().upper
    val lowerExposure=getExposureRange().lower
    val fpsRange=characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
    fun getHardWareLevel() {
        for (cameraId in manager.cameraIdList) {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val result = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            val level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            //LEGACY, LIMITED, FULL, and LEVEL_3 are 2, 0, 1, and 3
            Log.d("CameraUtil", "Camera${cameraId}.Capabilities Level=$level")
            for (cap in result!!) {
                when (cap) {
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> printLog("$cameraId:MANUAL_SENSOR is Available")
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE -> printLog(
                        "$cameraId: BACKWARD_COMPATIBLE is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW -> printLog("$cameraId :RAW is Available")
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING -> printLog(
                        "$cameraId :PRIVATE_REPROCESSING is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS -> printLog(
                        "$cameraId :READ_SENSOR_SETTINGS is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> printLog("$cameraId :BURST_CAPTURE is Available")
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING -> printLog(
                        "$cameraId :YUV_REPROCESSING is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT -> printLog("$cameraId :DEPTH_OUTPUT is Available")
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO -> printLog(
                        "$cameraId :CONSTRAINED_HIGH_SPEED_VIDEO is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING -> printLog(
                        "$cameraId :MOTION_TRACKING is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> printLog(
                        "$cameraId :LOGICAL_MULTI_CAMERA is Available"
                    )
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME -> printLog("$cameraId :MONOCHROME is Available")
                    CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING -> printLog(
                        "$cameraId :MANUAL_POST_PROCESSING is Available"
                    )
                }
            }
        }
    }

    private fun printLog(content: String) {
        Log.d("CameraUtil", "REQUEST_AVAILABLE_CAPABILITIES：$content")
    }

    fun getISoRange():Range<Int> {
        return characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)!!
    }
    fun getExposureRange():Range<Long>{
        return characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)!!
    }

}

