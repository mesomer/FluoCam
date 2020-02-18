package com.mesomer.fluocam.myview

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class MyTextureView(context: Context,attrs:AttributeSet):TextureView(context, attrs){
    private var mRatioWidth = 0
    private var mRatioHeight = 0

    fun setAspectRatio(width:Int,height:Int){
        mRatioWidth=width
        mRatioHeight=height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (mRatioHeight==0||mRatioWidth==0){
            setMeasuredDimension(width,height)
        }else{
            if (width<height*mRatioHeight/mRatioHeight)
                setMeasuredDimension(width,width*mRatioHeight/mRatioWidth)
            else
                setMeasuredDimension(height*mRatioWidth/mRatioHeight,height)
        }
    }
}
