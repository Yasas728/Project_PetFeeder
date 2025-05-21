package com.example.petfeeder.data

data class Schedule(
    val id: Int,
    val isEnable: Boolean,
    val timeHour: Int,
    val timeMinute: Int,
    val Mon: Boolean,
    val Tue: Boolean,
    val Wed: Boolean,
    val Thu: Boolean,
    val Fri: Boolean,
    val Sat: Boolean,
    val Sun: Boolean
)
