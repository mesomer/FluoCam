package com.mesomer.fluocam.science

class LinearRegression(ArrayofX: Array<Double>, ArrayofY: Array<Double>) {

    private val arrayofX=ArrayofX
    private val arrayofY=ArrayofY
    private val meanX = mean(ArrayofX)
    private val meanY = mean(ArrayofY)
    val b = getintercept()
    val a = getslope()

    private fun getintercept(): Double {
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

    private fun getslope(): Double {
        return meanY-b*meanX
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
}