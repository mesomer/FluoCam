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
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO


class ShowResult : AppCompatActivity() {
    private var myHeigth=400
    private var myWeight=400
    private var db: AppDatabase?=null
    private var myDao: MyDAO?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)
        val axis = findViewById<MyAxis>(R.id.myAxis)
       // val w = makeMeasureSpec(0, UNSPECIFIED)
        //val h = makeMeasureSpec(0, UNSPECIFIED)
        //myAxis.measure(w, h)
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        axis.getWidthAndHeight(myHeigth,myWeight)
        axis.setAxis(Point(10,10),50,50)
    }

}
