package com.mesomer.fluocam.science

class Conversion{
    fun nsToms(nanosecond: Long): Long {
        return nanosecond / 1000000
    }
    fun msTons(msecond: Long): Long {
        return msecond * 1000000
    }
}