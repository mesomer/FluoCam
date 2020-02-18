package com.mesomer.fluocam

import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mesomer.fluocam.myview.MyAxis
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.util.Log
import kotlinx.android.synthetic.main.activity_show_result.*
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.UNSPECIFIED
import android.view.View.MeasureSpec.makeMeasureSpec
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View


class ShowResult : AppCompatActivity() {
    private var myHeigth=1500
    private var myWeight=1500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)
        val axis = findViewById<MyAxis>(R.id.myAxis)
       // val w = makeMeasureSpec(0, UNSPECIFIED)
        //val h = makeMeasureSpec(0, UNSPECIFIED)
        //myAxis.measure(w, h)
        axis.getWidthAndHeight(myHeigth,myWeight)
        axis.setAxis(Point(10,10),50,50)
    }

}
