package com.mesomer.fluocam.adapter

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseExpandableListAdapter
import android.widget.LinearLayout
import android.widget.TextView
//结果活动ExpandList的适配器

class MyExpandableAdapter (activity: Activity,groupList:ArrayList<String>,sampleList:ArrayList<ArrayList<String>>):BaseExpandableListAdapter(){

    private var activity:Activity
    private var groupList:ArrayList<String>
    private var sampleList:ArrayList<ArrayList<String>>

    init {
        this.activity=activity
        this.groupList=groupList
        this.sampleList=sampleList
    }
    private val textView:TextView
        get() {
        val textView =TextView(activity)
        val lp = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.layoutParams=lp
        textView.gravity =Gravity.CENTER_VERTICAL or Gravity.START
        textView.setPadding(30,10,0,10)
        textView.textSize=20f
        return textView
    }

    override fun getGroup(groupPosition: Int): Any {
        return groupList[groupPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }
    override fun getGroupCount(): Int {
        return groupList.size
    }
    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val ll:LinearLayout
        if(convertView==null){
            ll= LinearLayout(activity)
            ll.orientation=LinearLayout.HORIZONTAL
            val textView = this.textView
            textView.text=getGroup(groupPosition).toString()
            ll.addView(textView)
        }
        else{
            ll=convertView as LinearLayout
        }
        return ll
    }

    override fun getChildrenCount(groupPosition: Int): Int {
       return sampleList[groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return sampleList[groupPosition][childPosition]
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val textView:TextView
        if (convertView==null){
            textView=this.textView
            textView.text=getChild(groupPosition,childPosition).toString()
        }
        else{
            textView = convertView as TextView
        }
        return textView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
         return childPosition.toLong()
    }


    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}