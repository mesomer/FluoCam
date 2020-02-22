package com.mesomer.fluocam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mesomer.fluocam.myview.MyGridVIew
import kotlinx.android.synthetic.main.activity_show_album.*
import java.io.File

private val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
private const val REQUEST_CODE_PERMISSIONS = 15

class ShowAlbum : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_album)
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
    private fun StartShowAlbum() {
        val adapter =object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                //val image = ImageView(this@ShowAlbum)
                val image = MyGridVIew(this@ShowAlbum )
                val path=paths.get(paths.size-position-1)
                Glide.with(this@ShowAlbum).load(File(path)).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().into(image)
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
        photogrid.adapter=adapter
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
            names.add(name)
            paths.add(path)
        }
        cursor.close()
    }

}
