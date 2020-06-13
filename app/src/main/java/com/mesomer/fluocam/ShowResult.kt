package com.mesomer.fluocam

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.data.*
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.adapter.MyExpandableAdapter
import com.mesomer.fluocam.camera.RGBmap
import com.mesomer.fluocam.myview.MyRec
import com.mesomer.fluocam.science.LinearRegression
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

const val GROUP_NUM = "groupnum"
private const val CENTER_X = "centerX"
private const val CENTER_Y = "centerY"
private const val REC_WIDTH = "recwidth"
private const val REC_HEIGHT = "recheight"

class ShowResult : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var myDao: MyDAO
    private lateinit var myThread: MyThread
    private lateinit var chart: CombinedChart
    private lateinit var expandListView: ExpandableListView

    private lateinit var groupArrayList: ArrayList<String>
    private lateinit var sampleArrayList: ArrayList<ArrayList<String>>
    val myDotData = ArrayList<Pair<Double, Double>>()
    private lateinit var groupList: List<String>

    internal inner class MyThread : Thread() {
        var relativeZero = 0.0

        lateinit var mHandler: Handler
        override fun run() {
            Looper.prepare()
            mHandler = @SuppressLint("HandlerLeak")
            object : Handler() {
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                    if (msg?.what == 0x222) {
                        //
                        val groupNum = msg.data.getInt(GROUP_NUM)
                        val photoReturn = myDao.getPhotoByGroupID(groupArrayList[groupNum])
                        var preferences: SharedPreferences =
                            getSharedPreferences(CENTER_X, Context.MODE_PRIVATE)
                        val centerX = preferences.getInt(CENTER_X, 0)
                        preferences = getSharedPreferences(CENTER_Y, Context.MODE_PRIVATE)
                        val centery = preferences.getInt(CENTER_Y, 0)
                        preferences = getSharedPreferences(REC_WIDTH, Context.MODE_PRIVATE)
                        val recwidth = preferences.getInt(REC_WIDTH, 0)
                        preferences = getSharedPreferences(REC_HEIGHT, Context.MODE_PRIVATE)
                        val recheight = preferences.getInt(REC_HEIGHT, 0)
                        val Rec_area = MyRec(centerX, centery, recwidth, recheight)
                        val RGBvalue = ArrayList<Double>()
                        val concentrationValue = ArrayList<Double>()
                        val relativeArray = ArrayList<Double>()
                        val eigenvalue = ArrayList<Double>()
                        for (photo in photoReturn) {
                            if (photo.IsStander) {
                                val photoBitmap =
                                    Glide.with(this@ShowResult).asBitmap().load(File(photo.path))
                                        .submit().get()
                                val greenArray = RGBmap(photoBitmap, Rec_area).getGArray()
                                greenArray.sort()
                                val length = greenArray.size
                                var sumGreen = 0
                                for (i in (length - 300) until length - 1) {
                                    sumGreen += greenArray[i]
                                }
                                val meanGreen = sumGreen / 300.0

                                if (photo.concentration.toInt() == 0) {
                                    relativeArray.add(meanGreen)
                                }

                                Log.i(
                                    "RGBmap",
                                    "meangreen=${meanGreen},greenarray.size=${greenArray.size}"
                                )
                                RGBvalue.add(meanGreen)
                                concentrationValue.add(photo.concentration.toDouble())
                                Log.i("RGBmap", "concentration=${photo.concentration.toDouble()}")
                            }
                        }
                        if (relativeArray.size != 0) {
                            relativeZero = relativeArray.sum() / relativeArray.size
                        }
                        for (value in RGBvalue) {
                            eigenvalue.add(value / relativeZero)
                        }
                        for (i in concentrationValue.indices) {
                            myDotData.add(Pair(concentrationValue[i], eigenvalue[i]))
                        }

                        val RGBarray = eigenvalue.toTypedArray()
                        val concentrationArray = concentrationValue.toTypedArray()
                        val linearRegression = LinearRegression(concentrationArray, RGBarray)

                        runOnUiThread {
                            Log.i(
                                "linear",
                                "a=" + linearRegression.a.toString() + " b=" + linearRegression.b.toString() + " R^2=" + linearRegression.R_2.toString()
                            )
                            drawChart(
                                linearRegression.a,
                                linearRegression.b,
                                concentrationArray.min()!!,
                                concentrationArray.max()!!,
                                linearRegression.R_2
                            )
                        }
                    }
                    if (msg?.what == 0x111) {
                        groupList = myDao.getAllGroupID()
                        groupArrayList = ArrayList<String>()
                        sampleArrayList = ArrayList<ArrayList<String>>()

                        for (groupid in groupList) {
                            groupArrayList.add(groupid)
                            val photoReturn = myDao.getPhotoByGroupID(groupid)
                            val concentrationArray = ArrayList<String>()
                            for (photo in photoReturn) {
                                concentrationArray.add(photo.concentration)
                            }
                            sampleArrayList.add(concentrationArray)
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

    private fun drawChart(
        Slope: Double,
        Intercept: Double,
        minX: Double,
        maxX: Double,
        R_square: Double
    ) {
        val myLineData = ArrayList<Pair<Double, Double>>()

        var startX = 0.0

        if ((minX - 0.2 * minX) > 0) {
            startX = (minX - 0.2 * minX)
        }
        val endX = (maxX + 0.2 * maxX)
        myLineData.add(Pair(startX, startX * Slope + Intercept))
        myLineData.add(Pair(endX, endX * Slope + Intercept))
        val lineEntries = ArrayList<Entry>()
        val dotEntries = ArrayList<Entry>()

        for (point in myLineData) {
            lineEntries.add(Entry(point.first.toFloat(), point.second.toFloat()))
        }
        bubbleSort(myDotData)
        for (dot in myDotData) {
            dotEntries.add(Entry(dot.first.toFloat(), dot.second.toFloat()))
        }
        val df = DecimalFormat("#.#######")
        df.roundingMode = RoundingMode.CEILING
        val lineDataSet = LineDataSet(
            lineEntries,
            "a=${df.format(Slope)},b=${df.format(Intercept)},R^2=${df.format(R_square)}"
        )
        val dotDataSet = ScatterDataSet(dotEntries, "")
        lineDataSet.color = Color.GREEN
        dotDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE)
        dotDataSet.scatterShapeSize = 10f
        dotDataSet.isHighlightEnabled = false
        dotDataSet.color = (Color.RED)
        lineDataSet.setDrawCircles(false)


        val combinedData = CombinedData()
        val lineData = LineData(lineDataSet)
        val scatterData = ScatterData(dotDataSet)
        combinedData.setData(lineData)
        combinedData.setData(scatterData)
        chart.data = combinedData
        chart.invalidate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_result)
        expandListView = findViewById(R.id.expand_list)
        chart = findViewById(R.id.chart)

        db = AppDatabase.getAppDataBase(context = this)!!
        myDao = db.myDao()

        myThread = MyThread()
        myThread.start()

        expandListView.setOnGroupExpandListener { groupPosition ->
            sendMessage(
                0x222,
                groupPosition
            )
        }
        expandListView.setOnItemLongClickListener { _, _, position, _ ->
            deleteWindow(position)
            true
        }
        //设
        setChartViewSize(chart)
        //
        Timer().schedule(object : TimerTask() {
            override fun run() {
                sendMessage(0x111)
            }
        }, 100)

    }

    private fun setChartViewSize(view: View) {
        val myDisplay = windowManager.defaultDisplay
        val viewParam = view.layoutParams
        val point = Point(0, 0)
        myDisplay.getSize(point)
        viewParam.height = point.y / 2
    }


    private fun deleteWindow(groupNum: Int) {
        AlertDialog.Builder(this).setTitle("删除本组").setMessage("删除组：${groupList[groupNum]}?")
            .setPositiveButton("删除") { _, _ ->
                myDao.deleteByGroup(groupList[groupNum])
                sendMessage(0x111)
            }.setNegativeButton("取消") { _, _ -> }.create().show()
    }

    private fun bubbleSort(arr: ArrayList<Pair<Double, Double>>) {
        var out = 0
        var i: Int
        while (out < arr.size) {
            i = out + 1
            while (i < arr.size) {
                if (arr[out].first > arr[i].first) {
                    val temp = arr[out]
                    arr[out] = arr[i]
                    arr[i] = temp
                }
                i++
            }
            out++
        }
    }

    private fun sendMessage(msgnum: Int) {
        val msg = Message()
        msg.what = msgnum
        myThread.mHandler.sendMessage(msg)
    }

    private fun sendMessage(msgnum: Int, groupNum: Int) {
        val msg = Message()
        msg.what = msgnum
        val bundle = Bundle()
        bundle.putInt(GROUP_NUM, groupNum)
        msg.data = bundle
        myThread.mHandler.sendMessage(msg)
    }
}
