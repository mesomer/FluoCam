package com.mesomer.fluocam

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mesomer.fluocam.camera.PhotoCapture

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //获取按钮
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        //按钮的点击事件

        button1.setOnClickListener {
            val intent = Intent(this@MainActivity,PhotoCapture::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this@MainActivity,ShowResult::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener {
            Toast.makeText(this@MainActivity,"点击按钮三",Toast.LENGTH_LONG).show()
        }

        button4.setOnClickListener {
            Toast.makeText(this@MainActivity,"点击按钮四",Toast.LENGTH_LONG).show()
        }
    }
}
