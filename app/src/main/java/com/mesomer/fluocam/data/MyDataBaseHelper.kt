package com.mesomer.fluocam.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import com.mesomer.fluocam.app.FluoCamApplication
import org.jetbrains.anko.db.*

class MyDataBaseHelper(ctx: Context=FluoCamApplication.instance) : ManagedSQLiteOpenHelper(ctx,NAME, null, VERSION){
    companion object{
        val NAME = "photo.db"
        var VERSION=1
    }
    override fun onCreate(db: SQLiteDatabase?) {
        db?.createTable(PhotoTabel.NAME,true,PhotoTabel.ID to INTEGER+ PRIMARY_KEY+ AUTOINCREMENT,PhotoTabel.concentrate to TEXT,PhotoTabel.photo to BLOB )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.dropTable(PhotoTabel.NAME,true)
        onCreate(db)
    }
}