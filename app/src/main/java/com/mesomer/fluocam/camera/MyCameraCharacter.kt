package com.mesomer.fluocam.camera

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

class MyCameraCharacter {
    companion object{
        fun getHardWareLevel(activity:Activity){
            val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)
                var result=characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                for(cap in result){
                    when(cap){
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR-> printLog("$cameraId:MANUAL_SENSOR is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE-> printLog("$cameraId: BACKWARD_COMPATIBLE is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW->printLog("$cameraId :RAW is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING->printLog("$cameraId :PRIVATE_REPROCESSING is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_READ_SENSOR_SETTINGS->printLog("$cameraId :READ_SENSOR_SETTINGS is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE->printLog("$cameraId :BURST_CAPTURE is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING->printLog("$cameraId :YUV_REPROCESSING is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT->printLog("$cameraId :DEPTH_OUTPUT is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO->printLog("$cameraId :CONSTRAINED_HIGH_SPEED_VIDEO is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MOTION_TRACKING->printLog("$cameraId :MOTION_TRACKING is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA->printLog("$cameraId :LOGICAL_MULTI_CAMERA is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MONOCHROME->printLog("$cameraId :MONOCHROME is Available")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING->printLog("$cameraId :MANUAL_POST_PROCESSING is Available")
                    }
                }


            }
        }
        private fun printLog(content:String){
            Log.d("CameraUtil", "REQUEST_AVAILABLE_CAPABILITIES：$content")!!
        }
    }

}