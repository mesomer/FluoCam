package com.mesomer.fluocam

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.adapter.AlbumAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
private const val REQUEST_CODE_PERMISSIONS = 15
private const val APPGALLERY = 0x111
private const val PHONEGALLERY = 0x222

class ShowAlbum : AppCompatActivity() {
    private var db: AppDatabase? = null
    private var myDao: MyDAO? = null
    private var showMode = 1
    private lateinit var mediaList: MutableList<File>
    private lateinit var photoList:ListView
    private val formatter= SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val pathAndDataMap = mutableMapOf<String,String>()
    private val dataSet = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_album)
        db = AppDatabase.getAppDataBase(context = this)
        myDao = db?.myDao()
        photoList = findViewById<ListView>(R.id.albumList)
        if (allPermissionsGranted()) {
            photoList.post { startShowAlbum() }
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                photoList.post { startShowAlbum() }
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
        menu?.add(0, APPGALLERY, 2, "外部")
        menu?.add(0, PHONEGALLERY, 1, "内部")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            APPGALLERY -> {
                showMode = 1
                startShowAlbum()
            }
            PHONEGALLERY -> {
                showMode = 2
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



    private fun startShowAlbum() {
        getAllPhoto()
        photoList.adapter=AlbumAdapter(this@ShowAlbum,dataSet.toSortedSet(),pathAndDataMap)
    }

    private fun getAllPhoto() {
        dataSet.clear()
        pathAndDataMap.clear()
        if (showMode == 1) {
            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (cursor!!.moveToNext()) {
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                if (formatJudge(path, "jpg") || formatJudge(path, "jpeg")) {
                    val time=File(path).lastModified()
                    val result=formatter.format(time)
                    dataSet.add(result)
                    pathAndDataMap[path] = result
                }
            }
            cursor.close()
        } else {
            val rootDirectory = File(externalMediaDirs.first().path)
            mediaList = rootDirectory.listFiles().toMutableList()
            for (file in mediaList) {
                if (formatJudge(file, "jpg") || formatJudge(file, "jpeg")) {
                    val time = file.lastModified()
                    val result= formatter.format(time)
                    dataSet.add(result)
                    pathAndDataMap[file.path] = result
                }
            }
        }
    }

    private fun formatJudge(file: File, format: String): Boolean {
        val end = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length)
            .toLowerCase(Locale.getDefault())
        return end == format
    }

    private fun formatJudge(path: String, format: String): Boolean {
        val end =
            path.substring(path.lastIndexOf(".") + 1, path.length).toLowerCase(Locale.getDefault())
        return end == format
    }
}
