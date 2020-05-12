package com.mesomer.fluocam.myview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView

class MyImageView(context: Context,attrs: AttributeSet? = null) : androidx.appcompat.widget.AppCompatImageView(context){

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0,widthMeasureSpec), View.getDefaultSize(0,heightMeasureSpec))
        val childWidthSize = measuredWidth
        val mywidth=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY)
        val myheight=MeasureSpec.makeMeasureSpec(4*childWidthSize/3,MeasureSpec.EXACTLY)
        super.onMeasure(mywidth, myheight)
    }

}