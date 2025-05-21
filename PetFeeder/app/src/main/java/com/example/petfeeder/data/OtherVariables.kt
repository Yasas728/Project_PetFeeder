package com.example.petfeeder.data

data class OtherVariables(
    val FeedNow: Boolean = false,
    val MainFoodLevel: Double = 0.25,
    val PotionSize: Int = 1,
    val IntruderAlert: Boolean = false,
    val NextFeeding: String = "Today, 6:00 PM"
)
