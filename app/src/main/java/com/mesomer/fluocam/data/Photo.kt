package com.mesomer.fluocam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id:Int?=null,
    val name:String,
    val concentration:String
)