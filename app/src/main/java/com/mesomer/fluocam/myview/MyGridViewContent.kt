package com.mesomer.fluocam.myview

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout as LinearLayout1

class MyGridViewContent(context: Context) : androidx.appcompat.widget.AppCompatImageView(context) {
//正方形自定义GridView
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0,widthMeasureSpec), View.getDefaultSize(0,heightMeasureSpec))
        val childWidthSize = measuredWidth
        val mywidth=MeasureSpec.makeMeasureSpec(childWidthSize,MeasureSpec.EXACTLY)
        val myheight=mywidth
        super.onMeasure(mywidth, myheight)
    }
}