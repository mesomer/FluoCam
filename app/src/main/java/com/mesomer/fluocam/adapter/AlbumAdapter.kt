package com.mesomer.fluocam.adapter

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import com.mesomer.fluocam.myview.MyGridView

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val dataString=dataSet.elementAt(position)
        Log.d("DATA",dataString)
        val linearLayout=getLinearLayout()
        val dataTextView=getTextView(dataString)
        val gridView=MyGridView(activity,pathAndDataMap.filter { (_,value)->value==dataString }).gridView
        linearLayout.addView(dataTextView)
        linearLayout.addView(gridView)
        return linearLayout
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return dataSet.size
    }
    private fun getLinearLayout():LinearLayout{
        val linearLayout=LinearLayout(activity)
        linearLayout.orientation=LinearLayout.VERTICAL
        return linearLayout
    }
    private fun getTextView(dataString:String):TextView{
        val dataTextView=TextView(activity)
        dataTextView.textSize=15F
        dataTextView.text=dataString
        return dataTextView
    }

}