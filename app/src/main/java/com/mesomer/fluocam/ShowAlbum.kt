package com.mesomer.fluocam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.data.Photo
import com.mesomer.fluocam.myview.MyGridVIew
import kotlinx.android.synthetic.main.activity_show_album.*
import kotlinx.android.synthetic.main.mark_window.view.*
import java.io.File

private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
private const val REQUEST_CODE_PERMISSIONS = 15

class ShowAlbum : AppCompatActivity() {
    private var db: AppDatabase?=null
    private var myDao: MyDAO?=null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_album)
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        val photoGrid = findViewById<GridView>(R.id.photogrid)
        if (allPermissionsGranted()) {
            photoGrid.post { StartShowAlbum() }
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                val photoGrid = findViewById<GridView>(R.id.photogrid)
                photoGrid.post { StartShowAlbum() }
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    var names = ArrayList<String>()
    var paths = ArrayList<String>()
    var realpaths=ArrayList<String>()
    private fun StartShowAlbum() {
        val adapter = object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                //val image = ImageView(this@ShowAlbum)
                val image = MyGridVIew(this@ShowAlbum)
                val path = paths.get(paths.size - position - 1)
                Glide.with(this@ShowAlbum).load(File(path)).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(image)
                return image
            }

            override fun getItem(position: Int): Any? {
                return null
            }

            override fun getItemId(position: Int): Long {
                return 0
            }

            override fun getCount(): Int {
                return paths.size
            }
        }
        GetAllPhoto()
        photogrid.adapter = adapter
        photogrid.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val thisphoto=myDao!!.getPhotoByurl(realpaths[realpaths.size-position-1])
                val havePhoto=(thisphoto.size!=0)
                var thephoto=Photo(0,"0","0","0",true,realpaths[realpaths.size-position-1])
                Log.e("realpath",realpaths[realpaths.size-position-1])
                for (photo in thisphoto){
                    if (photo.path==realpaths[realpaths.size-position-1]){
                        thephoto=photo
                    }
                }
                MarkWindow(view, havePhoto,thephoto)
            }
    }

    private fun GetAllPhoto() {
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )
        while (cursor!!.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            val desce = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION))
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
            val realpath=File(path).absolutePath
            names.add(name)
            paths.add(path)
            realpaths.add(realpath)
        }
        cursor.close()
    }

    private fun MarkWindow(source: View, havephoto: Boolean,photo: Photo) {
        val MarkForm = layoutInflater.inflate(R.layout.mark_window, null)
        var concentrationEdit=MarkForm.concentration
        var groupEdit=MarkForm.group_num
        var isStandarRatio=MarkForm.sampletag

        if (havephoto){
            concentrationEdit.setText(photo.concentration)
            groupEdit.setText(photo.groupID)
            if (photo.IsStander)
                isStandarRatio.check(R.id.tested)
            else
                isStandarRatio.check(R.id.tobetested)
        }

        AlertDialog.Builder(this).setView(MarkForm).setPositiveButton("确认") { dialog, which ->
            if (havephoto){
                Toast.makeText(this@ShowAlbum,"已经有了",Toast.LENGTH_LONG).show()
                //待完成
            }
            else
                myDao?.insertPhoto(Photo(concentration = concentrationEdit.text.toString(),path=photo.path,name = "",groupID = groupEdit.text.toString(),IsStander = isStandarRatio.checkedRadioButtonId==R.id.tested))
        }
            .setNegativeButton("取消") { dialog, which ->
                //取消操作
            }.create().show()
    }
}
