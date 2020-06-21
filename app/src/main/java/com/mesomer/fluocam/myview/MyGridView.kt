package com.mesomer.fluocam.myview

import android.app.Activity
import android.util.Log
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.R
import com.mesomer.fluocam.adapter.GridViewAdapter
import com.mesomer.fluocam.data.Photo
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.mark_window.view.*

class MyGridView (_activity: Activity,_paths: Map<String, String>){

    private var activity:Activity
    private var paths:Map<String, String>
    private var db: AppDatabase?
    private var myDao: MyDAO?

    init {
        activity=_activity
        paths=_paths
        db=AppDatabase.getAppDataBase(activity)
        myDao=db?.myDao()
    }

    val gridView:GridView
    get() {
        val gridView=SelfAdaptGridViw(activity)
        gridView.numColumns = 4
        gridView.horizontalSpacing = 2
        gridView.verticalSpacing = 2
        gridView.adapter=GridViewAdapter(activity,paths)
        gridView.onItemClickListener=AdapterView.OnItemClickListener { _, _, position, _ ->
            val thepath=paths.keys.elementAt(position)
            val thisphoto = myDao!!.getPhotoByurl(thepath)
            val havePhoto = (thisphoto.isNotEmpty())
            var thephoto = Photo(thepath, "0", "0", "0", true,null,null,null)
            Log.i("path", "path:$thephoto")
            for (photo in thisphoto) {
                if (photo.path == thepath) {
                    thephoto = photo
                }
            }
            markWindow(havePhoto, thephoto)
        }
        return gridView
    }
    private fun markWindow(havephoto: Boolean, photo: Photo) {
        val markForm = activity.layoutInflater.inflate(R.layout.mark_window, null)
        val concentrationEdit = markForm.concentration
        val groupEdit = markForm.group_num
        val isStandarRatio = markForm.sampletag

        if (havephoto) {
            concentrationEdit.setText(photo.concentration)
            groupEdit.setText(photo.groupID)
            if (photo.IsStander)
                isStandarRatio.check(R.id.tested)
            else
                isStandarRatio.check(R.id.tobetested)
        }

        AlertDialog.Builder(activity).setView(markForm).setPositiveButton("确认") { _, _ ->
            if (havephoto) {
                myDao?.updatePhoto(
                    Photo(
                        concentration = concentrationEdit.text.toString(),
                        path = photo.path,
                        name = "",
                        groupID = groupEdit.text.toString(),
                        IsStander = isStandarRatio.checkedRadioButtonId == R.id.tested,
                        Eigenvalues = null, ExposureTime = null, ISO = null
                    )
                )
                Toast.makeText(activity, "属性已更新", Toast.LENGTH_LONG).show()

            } else
                myDao?.insertPhoto(
                    Photo(
                        concentration = concentrationEdit.text.toString(),
                        path = photo.path,
                        name = "",
                        groupID = groupEdit.text.toString(),
                        IsStander = isStandarRatio.checkedRadioButtonId == R.id.tested,
                        Eigenvalues = null,
                        ExposureTime = null,
                        ISO = null
                    )
                )
        }
            .setNegativeButton("取消") { _, _ ->

            }.create().show()
    }
}