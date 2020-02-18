package com.mesomer.fluocam.app

import android.app.Application

public class FluoCamApplication:Application(){
    companion object{
        lateinit var instance:FluoCamApplication
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}