package com.mesomer.fluocam.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView

class AlbumAdapter constructor(
    _activity: Activity,
    _dataSet: MutableSet<String>,
    _pathAndDataMap: MutableMap<String, String>
) : BaseAdapter() {

    private var activity: Activity
    private var dataSet: MutableSet<String>
    private var pathAndDataMap: MutableMap<String, String>

    init {
        activity = _activity
        dataSet = _dataSet
        pathAndDataMap = _pathAndDataMap
    }

    // A linearLayout Container
    private val linearLayout: LinearLayout
        get() {
            val linearLayout = LinearLayout(activity)
            linearLayout.orientation = LinearLayout.VERTICAL

            return linearLayout
        }

    //To Show the data when the photos were taken
    private val dataTextView: TextView
        get() {
            val textView = TextView(activity)
            textView.textSize = 20F
            return textView
        }
    //To show photos
    //its adaper is defined in annother class
    private val gridView: GridView
        get() {
            val gridView = GridView(activity)
            gridView.numColumns = 4
            gridView.horizontalSpacing = 2
            gridView.verticalSpacing = 2
            return gridView
        }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val dataString=dataSet.elementAt(position)
        dataTextView.text=dataString
        gridView.adapter=GridViewAdapter(activity,pathAndDataMap.filter { (_,value)->value==dataString })
        linearLayout.addView(dataTextView)
        linearLayout.addView(gridView)
        return linearLayout
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSet.size
    }
}