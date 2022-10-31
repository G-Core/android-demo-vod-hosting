package com.example.gcore_vod_demo.extensions

fun Long.toTimeFormat(): String {

    val numberOfSeconds = this / 1000 % 60
    val numberOfMinutes = this / 1000 / 60 % 60
    val numberOfHours = this / 1000 / 3600

    return String.format("%02d:%02d:%02d", numberOfHours, numberOfMinutes, numberOfSeconds)
}