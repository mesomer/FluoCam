package com.mesomer.fluocam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mesomer.databasetest.data.AppDatabase
import com.mesomer.databasetest.data.MyDAO
import com.mesomer.fluocam.camera.Camera2Activity

class MainActivity : AppCompatActivity() {
    //private var db: AppDatabase?=null
   // private var myDao: MyDAO?=null
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //db = AppDatabase.getAppDataBase(context = this)
        //myDao = db?.myDao()
        preferences=getSharedPreferences("count", Context.MODE_PRIVATE)
        var count = preferences.getInt("count",0)
        Log.d("usetime","应用使用"+count.toString()+"次")

        val editor = preferences.edit()
        editor.putInt("count",++count)
        editor.apply()
        //获取按钮
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)
        val button3 = findViewById<Button>(R.id.button3)
        val button4 = findViewById<Button>(R.id.button4)
        //按钮的点击事件

        button1.setOnClickListener {
            val intent = Intent(this@MainActivity,Camera2Activity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this@MainActivity,ShowResult::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener {
            val intent = Intent(this@MainActivity,ShowAlbum::class.java)
            startActivity(intent)
        }

        button4.setOnClickListener {
            val intent = Intent(this@MainActivity,ParamInputActivity::class.java)
            startActivity(intent)
        }
    }
}
