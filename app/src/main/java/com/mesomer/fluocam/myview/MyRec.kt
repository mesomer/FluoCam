package com.mesomer.fluocam.myview

class MyRec(centerX: Int, centerY: Int, width: Int, height: Int) {

        val startX = getStart(centerX, width)
        val endX  = getEnd(centerX, width)
        val startY = getStart(centerY,height)
        val endY = getEnd(centerY,height)


    private fun isOdd(number: Int): Boolean {
        return number % 2 == 1
    }

    private fun getStart(center: Int, length: Int): Int {

        if (isOdd(length)) {
            return center - (length - 1) / 2
        } else
            return center - length / 2

    }

    private fun getEnd(center: Int, length: Int): Int {

        if (isOdd(length)) {
            return center + (length- 1) / 2
        } else
            return center + length / 2 - 1

    }

}