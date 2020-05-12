package com.mesomer.databasetest.data

import androidx.room.*
import com.mesomer.fluocam.ShowResult
import com.mesomer.fluocam.data.Photo

@Dao
interface MyDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoto(photo: Photo)

    @Update
    fun updatePhoto(photo: Photo)

    @Delete
    fun deletPhoto(photo: Photo)
    @Query("DELETE FROM PHOTO WHERE groupID==:groupID")
    fun deleteByGroup(groupID: String)

    @Query("SELECT *FROM Photo WHERE path==:path")
    fun getPhotoByurl(path: String): List<Photo>

    @Query("SELECT *FROM Photo")
    fun getAllPhoto():List<Photo>
    @Query("SELECT DISTINCT groupID FROM Photo")
    fun getAllGroupID():List<String>
    @Query("SELECT *FROM Photo where groupID==:groupID")
    fun getPhotoByGroupID(groupID:String):List<Photo>


}