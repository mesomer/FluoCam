package com.mesomer.fluocam.app

import android.app.Application
const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
public class FluoCamApplication:Application(){
    companion object{
        lateinit var instance:FluoCamApplication
    }
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}