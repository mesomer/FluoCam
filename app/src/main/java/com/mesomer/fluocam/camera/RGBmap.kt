package com.mesomer.fluocam.camera

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import com.mesomer.fluocam.myview.MyRec

class RGBmap(bitmap:Bitmap,rectangle:MyRec){
    private val startX=rectangle.startX
    private val startY=rectangle.startY
    private val endX=rectangle.endX
    private val endY =rectangle.endY
    private val myBitmap = bitmap
    fun getRArray():Array<Int>{
        var RArray=ArrayList<Int>()
        for (i in startX..endX){
            for(j in startY..endY){
                val red:Int
                red=Color.red(myBitmap.getPixel(i,j))
                RArray.add(red)
            }
        }
        return RArray.toTypedArray()
    }
    fun getGArray():Array<Int>{
        var RArray=ArrayList<Int>()
        for (i in startX..endX){
            for(j in startY..endY){
                val green:Int
                green=Color.green(myBitmap.getPixel(i,j))
                RArray.add(green)
            }
        }
        return RArray.toTypedArray()
    }
    fun getBArray():Array<Int>{
        var BArray=ArrayList<Int>()
        for (i in startX..endX){
            for(j in startY..endY){
                val blue:Int
                blue=Color.blue(myBitmap.getPixel(i,j))
                BArray.add(blue)
            }
        }
        return BArray.toTypedArray()
    }

}