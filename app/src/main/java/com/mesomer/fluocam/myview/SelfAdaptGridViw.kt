package com.mesomer.fluocam.myview

import android.content.Context
import android.widget.GridView

class SelfAdaptGridViw(context: Context): GridView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val expandSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE.shr(2),MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, expandSpec)
    }
}