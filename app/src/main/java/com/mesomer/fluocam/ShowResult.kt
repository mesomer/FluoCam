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
import com.mesomer.fluocam.camera.RGBmap
import com.mesomer.fluocam.data.Photo
import com.mesomer.fluocam.myview.MyRec
import com.mesomer.fluocam.science.LinearRegression
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParsePosition
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

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
                        val Rec_area=MyRec(1560,2036,30,30)
                        val RGBvalue = ArrayList<Double>()
                        val concentrationValue=ArrayList<Double>()
                        for(photo in photoReturn){
                            if(photo.IsStander) {
                                val photoBitmap =
                                    Glide.with(this@ShowResult).asBitmap().load(File(photo.path))
                                        .submit().get()
                                val greenArray = RGBmap(photoBitmap, Rec_area).getGArray()
                                val meanGreen = (greenArray.sum() / greenArray.size).toDouble()
                                Log.i(
                                    "RGBmap",
                                    "meangreen=${meanGreen},greenarray.size=${greenArray.size}"
                                )
                                RGBvalue.add(meanGreen)
                                concentrationValue.add(photo.concentration.toDouble())
                                Log.i("RGBmap", "concentration=${photo.concentration.toDouble()}")
                            }
                        }
                        val RGBarray=RGBvalue.toTypedArray()
                        val concentrationArray=concentrationValue.toTypedArray()
                        val linearRegression = LinearRegression(concentrationArray,RGBarray)
                        //分类
                        runOnUiThread {
                            drawLine(linearRegression.a,linearRegression.b,concentrationArray.min()!!,concentrationArray.max()!!,linearRegression.a,linearRegression.b,linearRegression.R_2)
                            Log.i("linear","a="+linearRegression.a.toString()+" b="+linearRegression.b.toString()+" R^2="+linearRegression.R_2.toString() )
                        }
                    }
                    //初始化列表
                     if (msg?.what == 0x111) {
                        groupList = myDao.getAllGroupID()
                         groupArrayList = ArrayList<String>()
                         sampleArrayList = ArrayList<ArrayList<String>>()
                        //从数据库读信息
                        for (groupid in groupList) {
                            groupArrayList.add(groupid)
                            val photoReturn = myDao.getPhotoByGroupID(groupid)
                            val cocentrationArray = ArrayList<String>()
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

    private fun drawLine(Slope:Double,Intercept:Double,minX:Double,maxX:Double,a:Double,b:Double,Rsquare:Double){
        var mydata = ArrayList<Pair<Double, Double>>()
        val minx=minX
        val maxx=maxX
        val slope=Slope
        val intercept=Intercept
        var startX=0.0
        var endX=0.0
        if ((minx-0.2*minx)>0){
            startX=(minx-0.2*minx)
        }
        endX=(maxx+0.2* maxx)
        mydata.add(Pair(startX,startX*slope+intercept))
        mydata.add(Pair(endX,endX*slope+intercept))
        var entries = ArrayList<Entry>()
        for (point in mydata) {
            entries.add(Entry(point.first.toFloat(), point.second.toFloat()))
        }
        val df = DecimalFormat("#.###")
        df.roundingMode = RoundingMode.CEILING
        var datset = LineDataSet(entries, "a=${df.format(a)},b=${df.format(b)},R^2=${df.format(Rsquare)}")

        datset.setColor(Color.GREEN)
        datset.setDrawCircles(false)

        var linedata = LineData(datset)
        chart.data = linedata
        chart.invalidate()
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
}
