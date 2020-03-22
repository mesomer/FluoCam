package com.mesomer.fluocam.science

import android.util.Log
import kotlin.system.measureNanoTime

class LinearRegression(ArrayofX: Array<Double>, ArrayofY: Array<Double>) {

    private val arrayofX=ArrayofX
    private val arrayofY=ArrayofY
    private val meanX = mean(ArrayofX)
    private val meanY = mean(ArrayofY)
    val a = getslope()
    val b = getintercept()
    val R_2=getR()

    private fun getslope(): Double {
        var DispersionX = getDispersion(arrayofX,meanX)
        var DispersionY =getDispersion(arrayofY,meanY)
        var product = 0.0
        var square = 0.0
        for (i in 0..arrayofX.size-1){
            product+=DispersionX[i]*DispersionY[i]
        }
        for (i in 0..arrayofX.size-1){
            square+=DispersionX[i]*DispersionX[i]
        }
        return product/square

    }

    private fun getintercept(): Double {
        return meanY-a*meanX
    }

    private fun mean(ArrayofNumber: Array<Double>): Double {
        return ArrayofNumber.sum() / ArrayofNumber.size
    }
    private fun getDispersion(ArrayofNumber: Array<Double>, mean:Double):Array<Double>{
        val dispersionArray = ArrayList<Double>()
        for (num in ArrayofNumber){
            val disperson = num-mean
            dispersionArray.add(disperson)
        }
        return dispersionArray.toTypedArray()
    }
    private fun getR():Double{
        var SS_reg=0.0
        var SS_sum=0.0
        for (num in arrayofX){
            val yup=(a*num+b) - meanY
            SS_reg+=yup*yup
            Log.i("linear","x=${num},yup=${yup}")
        }

        for (num in arrayofY){
            SS_sum+=(num-meanY)*(num-meanY)
            Log.i("linear","y=${num}")

        }
        return SS_reg/SS_sum
    }
}