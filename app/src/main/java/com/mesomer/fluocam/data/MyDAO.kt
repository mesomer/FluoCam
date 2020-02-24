package com.mesomer.databasetest.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mesomer.fluocam.data.Photo

@Dao
interface MyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoto(photo: Photo)
    @Update
    fun updatePhoto(photo: Photo)
    @Delete
    fun deletPhoto(photo: Photo)
    @Query("SELECT *FROM Photo WHERE name==:name")
    fun getPhotoByName(name:String):List<Photo>


}