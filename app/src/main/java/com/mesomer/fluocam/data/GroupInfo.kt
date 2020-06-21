package com.mesomer.fluocam.data
import androidx.camera.core.ImageAnalysis
import androidx.room.*
import com.mesomer.fluocam.myview.MyRec

@Entity
data class GroupInfo(
    @PrimaryKey(autoGenerate = false)
    val groupId:String,
    val ISO:Int,
    val exposureTime:Long,
    val analysisArea:MyRec,
    val a:Long,
    val b:Long
)