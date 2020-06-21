package com.mesomer.fluocam

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ParamInputActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    private val CENTERX = "centerX"
    private val CENTERY = "centerY"
    private val RECWIDTH = "recwidth"
    private val RECHEIGHT = "recheight"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_param_input)
        val centerxText = findViewById<EditText>(R.id.centerX)
        val centeryText = findViewById<EditText>(R.id.centerY)
        val recwidthText = findViewById<EditText>(R.id.recwidth)
        val recheightText = findViewById<EditText>(R.id.recheight)
        val updateButton = findViewById<Button>(R.id.update)

        preferences = getSharedPreferences(CENTERX, Context.MODE_PRIVATE)
        var centerX = preferences.getInt(CENTERX, 0)
        preferences = getSharedPreferences(CENTERY, Context.MODE_PRIVATE)
        var centery = preferences.getInt(CENTERY, 0)
        preferences = getSharedPreferences(RECWIDTH, Context.MODE_PRIVATE)
        var recwidth = preferences.getInt(RECWIDTH, 0)
        preferences = getSharedPreferences(RECHEIGHT, Context.MODE_PRIVATE)
        var recheight = preferences.getInt(RECHEIGHT, 0)

        centerxText.hint = centerX.toString()
        centeryText.hint = centery.toString()
        recwidthText.hint = recwidth.toString()
        recheightText.hint = recheight.toString()


        updateButton.setOnClickListener {
            if (centerxText.text.isEmpty() || centeryText.text.isEmpty() || recwidthText.text.isEmpty() || recheightText.text.isEmpty()) {
                Toast.makeText(this@ParamInputActivity, "编辑框不能为空", Toast.LENGTH_SHORT).show()
            } else {
                centerX = centerxText.text.toString().toInt()
                centery = centeryText.text.toString().toInt()
                recwidth = recwidthText.text.toString().toInt()
                recheight = recheightText.text.toString().toInt()

                var editor = getSharedPreferences(CENTERX, Context.MODE_PRIVATE).edit()
                editor.putInt(CENTERX, centerX)
                editor.apply()
                editor = getSharedPreferences(CENTERY, Context.MODE_PRIVATE).edit()
                editor.putInt(CENTERY, centery)
                editor.apply()
                editor = getSharedPreferences(RECWIDTH, Context.MODE_PRIVATE).edit()
                editor.putInt(RECWIDTH, recwidth)
                editor.apply()
                editor = getSharedPreferences(RECHEIGHT, Context.MODE_PRIVATE).edit()
                editor.putInt(RECHEIGHT, recheight)
                editor.apply()


                Toast.makeText(this@ParamInputActivity, "已更新", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
