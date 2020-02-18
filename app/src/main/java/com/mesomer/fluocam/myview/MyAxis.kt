package com.mesomer.fluocam.myview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.util.Log
import android.view.View


class MyAxis(context: Context, set: AttributeSet) : View(context, set) {

    private val paint = Paint()
    private val DEFAULT_GRIDNUM: Int = 100


    private var havefocus = false
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private var wGridNum: Int = DEFAULT_GRIDNUM
    private var hGridNum: Int = DEFAULT_GRIDNUM
    private var GridWidth: Float = 0f
    private var GridHeight: Float = 0f
    private var OriginPoint = FPoint(0f, 0f)
    private var endOfX = FPoint(0f, 0f)
    private var endOfY = FPoint(0f, 0f)

    /**输入的坐标轴长度是0-100的浮点数，表示占屏幕的百分比.原点坐标也对应*/
    fun setAxis(
        OriginPosition: Point,
        LengthOfX: Int,
        LengthOfY: Int
    ) {
        OriginPoint.x = relativeToAbsolute(OriginPosition.x, viewWidth.toFloat(), wGridNum)
        OriginPoint.y =
            relativeToAbsolute(convertYAxis(OriginPosition.y), viewHeight.toFloat(), hGridNum)
        endOfX.x = relativeToAbsolute(OriginPosition.x + LengthOfX, viewWidth.toFloat(), wGridNum)
        endOfX.y =
            relativeToAbsolute(convertYAxis(OriginPosition.y), viewHeight.toFloat(), hGridNum)
        endOfY.x = relativeToAbsolute(OriginPosition.x, viewWidth.toFloat(), wGridNum)
        endOfY.y = relativeToAbsolute(
            convertYAxis(OriginPosition.y + LengthOfY),
            viewHeight.toFloat(),
            hGridNum
        )
    }
    fun getWidthAndHeight(mywidth:Int,myheight:Int){
        viewWidth = mywidth
        viewHeight = myheight
    }
    fun setGrid(WGridNum: Int, HGridNum: Int) {
        wGridNum = WGridNum
        hGridNum = HGridNum
        measureGrid()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //背景色
        canvas?.drawColor(Color.WHITE)
        //抗锯齿
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        //绘制类型：边缘
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f

        canvas!!.drawLine(OriginPoint.x, OriginPoint.y, endOfX.x, endOfX.y, paint)
        canvas.drawLine(OriginPoint.x, OriginPoint.y, endOfY.x, endOfY.y, paint)
        Log.d("Axis", "OriginX (${OriginPoint.x},${OriginPoint.y})")
        Log.d("Axis", "X轴 (${endOfX.x}, ${endOfX.y})")
        Log.d("Axis", "y轴 (${(endOfY.x)}, ${(endOfY.y)})")
    }


    private fun measureGrid() {
        Log.d("Axis", "View高${viewHeight}, View宽${(viewWidth)}")
        GridHeight = viewHeight / hGridNum.toFloat()
        GridWidth = viewWidth / wGridNum.toFloat()
    }

    /**将相对坐标转换为绝对坐标*/
    private fun relativeToAbsolute(relativeValue: Int, absoluteLength: Float, gridNum: Int): Float {
        if (relativeValue <= gridNum)
            return relativeValue * absoluteLength / gridNum
        else
            return absoluteLength
    }

    private fun convertYAxis(y: Int): Int {
        if (y < hGridNum)
            return hGridNum - y
        else
            return 0
    }
    private class FPoint(x: Float, y: Float) {
        var x = x
        var y = y
    }
}