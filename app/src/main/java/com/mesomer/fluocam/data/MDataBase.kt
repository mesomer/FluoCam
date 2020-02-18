package com.mesomer.fluocam.data

import org.jetbrains.anko.db.insert

class MDataBase{
    companion object{
        val databaseHelper = MyDataBaseHelper()
        val instance = MDataBase
    }
    fun savePhoto(photo: Photo){
        databaseHelper.use {
            //sqlitedatabase扩展方法
            insert(PhotoTabel.NAME,)
        }
    }
}