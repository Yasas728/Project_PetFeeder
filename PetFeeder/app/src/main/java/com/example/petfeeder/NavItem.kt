package com.example.petfeeder

import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label : String,
    val icon: ImageVector,
    var badgeCount : Int = 0,
    val topLabel : String
)
