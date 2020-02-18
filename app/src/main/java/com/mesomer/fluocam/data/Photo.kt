package com.mesomer.fluocam.data

data class Photo(val map:MutableMap<String,Any?>) {

    val _id by map
    val name by map

}