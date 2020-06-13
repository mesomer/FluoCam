package com.mesomer.fluocam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import java.util.*
import kotlin.collections.ArrayList

private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
private const val REQUEST_CODE_PERMISSIONS = 15
private const val APPGALLERY=0x111
private const val  PHONEGALLERY=0x222

class ShowAlbum : AppCompatActivity() {
    private var db: AppDatabase?=null
    private var myDao: MyDAO?=null
    private var showMode=1
    private lateinit var mediaList: MutableList<File>
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_album)
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        val photoGrid = findViewById<GridView>(R.id.photogrid)
        if (allPermissionsGranted()) {
            photoGrid.post { startShowAlbum() }
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
                photoGrid.post { startShowAlbum() }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, APPGALLERY,2,"内部")
        menu?.add(0, PHONEGALLERY,1,"外部")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            APPGALLERY->{
                showMode=1
                startShowAlbum()
            }
            PHONEGALLERY->{
                showMode=2
                startShowAlbum()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    var paths = ArrayList<String>()
    private fun startShowAlbum() {
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
        getAllPhoto()
        photogrid.adapter = adapter
        photogrid.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val thisphoto=myDao!!.getPhotoByurl(paths[paths.size-position-1])
                val havePhoto=(thisphoto.size!=0)
                var thephoto=Photo(paths[paths.size-position-1],"0","0","0",true)
                Log.i("path","path:"+paths[paths.size-position-1])
                for (photo in thisphoto){
                    if (photo.path==paths[paths.size-position-1]){
                        thephoto=photo
                    }
                }
                markWindow(havePhoto,thephoto)
            }
        photogrid.onItemLongClickListener=
            AdapterView.OnItemLongClickListener{_, _, position, _ ->
                val intent = Intent(this@ShowAlbum,ImageReaderActivity::class.java)
                intent.putExtra("path",paths[paths.size-position-1])
                startActivity(intent)
                true
            }
    }

    private fun getAllPhoto() {
        paths.clear()
        if(showMode==1){
            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (cursor!!.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                paths.add(path)
            }
            cursor.close()
        }
        else{
            val rootDirectory = File(externalMediaDirs.first().path)
            mediaList = rootDirectory.listFiles().toMutableList()
            for (file in mediaList){
                val end = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length).toLowerCase(Locale.getDefault())
                if (end == "jpg"){
                    paths.add(file.path)
                }
            }
        }
    }

    private fun markWindow(havephoto: Boolean, photo: Photo) {
        val markForm = layoutInflater.inflate(R.layout.mark_window, null)
        val concentrationEdit=markForm.concentration
        val groupEdit=markForm.group_num
        val isStandarRatio=markForm.sampletag

        if (havephoto){
            concentrationEdit.setText(photo.concentration)
            groupEdit.setText(photo.groupID)
            if (photo.IsStander)
                isStandarRatio.check(R.id.tested)
            else
                isStandarRatio.check(R.id.tobetested)
        }

        AlertDialog.Builder(this).setView(markForm).setPositiveButton("确认") { dialog, which ->
            if (havephoto){
                myDao?.updatePhoto(Photo(concentration = concentrationEdit.text.toString(),path=photo.path,name = "",groupID = groupEdit.text.toString(),IsStander = isStandarRatio.checkedRadioButtonId==R.id.tested))
                Toast.makeText(this@ShowAlbum,"属性已更新",Toast.LENGTH_LONG).show()

            }
            else
                myDao?.insertPhoto(Photo(concentration = concentrationEdit.text.toString(),path=photo.path,name = "",groupID = groupEdit.text.toString(),IsStander = isStandarRatio.checkedRadioButtonId==R.id.tested))
        }
            .setNegativeButton("取消") { dialog, which ->

            }.create().show()
    }
}
