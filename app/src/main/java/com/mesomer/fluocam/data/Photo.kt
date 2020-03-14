package com.mesomer.fluocam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = false)
    val path:String,
    val name: String,
    val concentration: String,
    val groupID:String,
    val IsStander:Boolean

)