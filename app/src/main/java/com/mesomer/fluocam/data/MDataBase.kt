package com.mesomer.fluocam.data

import com.mesomer.fluocam.extention.MapToPair
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select

class MDataBase{
    companion object{
        val databaseHelper = MyDataBaseHelper()
        val instance = MDataBase
    }
    fun savePhoto(photo: Photo){
        databaseHelper.use {
            //sqlitedatabase扩展方法,*表示转变为可变参数
            insert(PhotoTabel.NAME,*photo.map.MapToPair())
        }
    }

    //查询所有照片，解析一行数据返回实体类
    fun getAllPhoto():List<Photo>{
        return databaseHelper.use {
            select(PhotoTabel.NAME).parseList(object : MapRowParser<Photo>{
                override fun parseRow(columns: Map<String, Any?>): Photo {
                    return Photo(columns.toMutableMap() )
                }

            })
        }
    }

}