package com.mesomer.databasetest.data

import androidx.room.TypeConverter
import com.mesomer.fluocam.data.Photo

class DateTypeConverter {
    @TypeConverter
    fun havePhoto(photo: Photo?):Boolean{
        return photo!=null
    }
}