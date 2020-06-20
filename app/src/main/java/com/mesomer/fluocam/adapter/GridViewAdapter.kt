package com.mesomer.fluocam.adapter

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mesomer.fluocam.myview.MyGridViewContent
import java.io.File

class GridViewAdapter(_activity: Activity, _paths: Map<String, String>) : BaseAdapter() {

    private var activity: Activity
    private var paths: Map<String, String>
    init {
        activity = _activity
        paths = _paths
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val image = MyGridViewContent(activity)
        val path = paths.keys.elementAt(position)
        Log.d("path",path)
        Glide.with(activity).load(File(path)).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(image)
        return image
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        Log.d("size","共有${paths.size}")
        return paths.size
    }
}