package com.mesomer.fluocam.extention
/**
 * map变Pair
 * */
fun <K,V>MutableMap<K,V>.MapToPair():Array<Pair<K,V>>{
    return map {
        Pair(it.key,it.value)
    }.toTypedArray()
}

