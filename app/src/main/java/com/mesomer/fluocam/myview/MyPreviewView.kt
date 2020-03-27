package com.mesomer.fluocam.myview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.camera.view.PreviewView
import java.util.jar.Attributes

class MyPreviewView:PreviewView{

    constructor(context: Context): super(context){
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet){
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr){
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0,widthMeasureSpec), View.getDefaultSize(0,heightMeasureSpec))
        val childWidthSize = measuredWidth
        val childHeight=measuredHeight
        val mywidth=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY)
        val myheight=MeasureSpec.makeMeasureSpec(4*childWidthSize/3,MeasureSpec.EXACTLY)
        super.onMeasure(mywidth, myheight)
    }
}