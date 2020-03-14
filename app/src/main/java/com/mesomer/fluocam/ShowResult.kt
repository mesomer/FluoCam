package com.mesomer.fluocam

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.adapter.MyExpandableAdapter
import com.mesomer.fluocam.data.Photo
import java.io.File
import java.text.ParsePosition
import java.util.*
import kotlin.collections.ArrayList

const val GROUP_NUM="groupnum"
class ShowResult : AppCompatActivity() {

    private lateinit var db : AppDatabase
    private lateinit var myDao: MyDAO
    private lateinit var myThread: MyThread
    private lateinit var chart: LineChart
    private lateinit var expandListView: ExpandableListView

    private lateinit var groupArrayList : ArrayList<String>
    private lateinit var sampleArrayList : ArrayList<ArrayList<String>>

    private lateinit var groupList: List<String>

    internal inner class MyThread : Thread() {
        lateinit var mHandler: Handler
        override fun run() {
            Looper.prepare()
            mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                    if (msg?.what == 0x222) {
                        //获得符合条件的Photo()
                        val groupNum = msg.data.getInt(GROUP_NUM)
                        val photoReturn = myDao.getPhotoByGroupID(groupArrayList[groupNum])
                        for(photo in photoReturn){
                            val photoBitmap=Glide.with(this@ShowResult).asBitmap().load(File(photo.path)).submit().get()
                            Log.i("bitmap","Bitmap.width="+photoBitmap.width.toString()+"Bitmap.heigth="+photoBitmap.height.toString())
                            val pixel_1545_2022=photoBitmap.getPixel(1544,2022)
                            val red=Color.red(pixel_1545_2022)
                            val green=Color.green(pixel_1545_2022)
                            val blue=Color.blue(pixel_1545_2022)
                            val alpha=Color.alpha(pixel_1545_2022)
                            Log.i("color","R="+red+",G="+green+" ,B="+blue+", A="+alpha)
                        }
                        //分类
                        runOnUiThread {
                            //画统计图
                        }
                    }
                    if (msg?.what == 0x111) {
                        groupList = myDao.getAllGroupID()
                         groupArrayList = ArrayList<String>()
                         sampleArrayList = ArrayList<ArrayList<String>>()
                        //从数据库读信息
                        for (groupid in groupList) {
                            groupArrayList.add(groupid)
                            var photoReturn = myDao.getPhotoByGroupID(groupid)
                            var cocentrationArray = ArrayList<String>()
                            for (photo in photoReturn) {
                                cocentrationArray.add(photo.concentration)
                            }
                            sampleArrayList.add(cocentrationArray)
                        }
                        runOnUiThread {
                            val adapter = MyExpandableAdapter(
                                this@ShowResult,
                                groupArrayList,
                                sampleArrayList
                            )
                            expandListView.setAdapter(adapter)
                        }
                    }
                }
            }
            Looper.loop()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)
        expandListView = findViewById(R.id.expand_list)
        chart = findViewById(R.id.chart)

        db = AppDatabase.getAppDataBase(context = this)!!
        myDao= db.myDao()

        myThread = MyThread()
        myThread.start()

        expandListView.setOnGroupExpandListener { groupPosition -> sendmessage(0x222,groupPosition)  }
        //设置图表View的高
        setChartViewSize(chart)
        //延时一段时间更新UI
        Timer().schedule(object : TimerTask(){
            override fun run() {
                sendmessage(0x111)
            }
        },100)


        //画图
        drawLine() 

    }

    private fun setChartViewSize(view: View) {
        val myDisplay = windowManager.defaultDisplay
        var viewParam = view.layoutParams
        var point = Point(0, 0)
        myDisplay.getSize(point)
        viewParam.height = point.y / 2
    }
    private fun sendmessage(msgnum:Int){
        val msg = Message()
        msg.what = msgnum
        myThread.mHandler.sendMessage(msg)
    }
    private fun sendmessage(msgnum:Int,groupNum:Int){
        val msg = Message()
        msg.what = msgnum
        val bundle = Bundle()
        bundle.putInt(GROUP_NUM,groupNum)
        msg.data = bundle
        myThread.mHandler.sendMessage(msg)
    }
    fun drawLine(){
        var mydata = ArrayList<Pair<Float, Float>>()
        mydata.add(Pair(0f, 0f))
        mydata.add(Pair(1f, 1f))
        mydata.add(Pair(2f, 2f))
        mydata.add(Pair(5f, 5f))
        mydata.add(Pair(10f, 15f))
        var entries = ArrayList<Entry>()
        for (point in mydata) {
            entries.add(Entry(point.first, point.second))
        }
        var datset = LineDataSet(entries, "Lable")
        datset.setColor(Color.BLACK)
        var linedata = LineData(datset)
        chart.data = linedata
        chart.invalidate()
    }

}
