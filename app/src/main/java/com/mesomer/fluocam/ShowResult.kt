package com.mesomer.fluocam

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.widget.ExpandableListView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.adapter.MyExpandableAdapter
import org.w3c.dom.Entity
import java.security.KeyStore


class ShowResult : AppCompatActivity() {

    private var db: AppDatabase? = null
    private var myDao: MyDAO? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)

        val expandListView = findViewById<ExpandableListView>(R.id.expand_list)
        val chart = findViewById<LineChart>(R.id.chart)

        //设置图表View的高
        val myDisplay = windowManager.defaultDisplay
        var chartParam = chart.layoutParams
        var point = Point(0, 0)
        myDisplay.getSize(point)
        chartParam.height = point.y / 2
        //数据库
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        //展开表的内容
        var groupList = myDao!!.getAllGroupID()
        var groupArrayList = ArrayList<String>()
        var sampleArrayList = ArrayList<ArrayList<String>>()
        //从数据库读信息
        for (groupid in groupList) {
            groupArrayList.add(groupid)
            var photoReturn = myDao!!.getPhotoByGroupID(groupid)
            var cocentrationArray = ArrayList<String>()
            for (photo in photoReturn) {
                cocentrationArray.add(photo.concentration)
            }
            sampleArrayList.add(cocentrationArray)
        }
        //配置BaseExpandableListAdapter
        val adapter = MyExpandableAdapter(this, groupArrayList, sampleArrayList)
        expandListView.setAdapter(adapter)
        //画图
        var mydata = ArrayList<Pair<Float,Float>>()
        mydata.add(Pair(0f,0f))
        mydata.add(Pair(1f,1f))
        mydata.add(Pair(2f,2f))
        mydata.add(Pair(5f,5f))
        mydata.add(Pair(10f,15f))
        var entries = ArrayList<Entry>()
        for(point in mydata){
            entries.add(Entry(point.first,point.second))
        }
        var datset=LineDataSet(entries,"Lable")
        datset.setColor(Color.BLACK)
        var linedata=LineData(datset)
        chart.data=linedata
        chart.invalidate()


    }

}
