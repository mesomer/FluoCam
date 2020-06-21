package com.mesomer.fluocam

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mesomer.fluocam.myview.MyImageView
import java.io.File

class ImageReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_reader)
        val imageView=findViewById<MyImageView>(R.id.image)
        var path=""
        try {
            path = intent.getStringExtra("path")
            Log.i("ImageReaderPath",path)
            Glide.with(this@ImageReaderActivity).load(File(path)).diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerInside().error(R.mipmap.ic_launcher).into(imageView)
        }catch (exception:Exception){
            exception.printStackTrace()
        }
    }
}
