package com.mesomer.fluocam.myview

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.view.View

class MySurfaceView :TextureView{
    constructor(context: Context): super(context){
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet){
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr){
    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes:Int):super(context, attributeSet, defStyleAttr,defStyleRes){

    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0,widthMeasureSpec), View.getDefaultSize(0,heightMeasureSpec))
        val childWidthSize = measuredWidth
        val mywidth=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY)
        val myheight=MeasureSpec.makeMeasureSpec(4*childWidthSize/3,MeasureSpec.EXACTLY)
        super.onMeasure(mywidth, myheight)
    }
}