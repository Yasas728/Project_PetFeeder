package com.example.petfeeder.data

import android.net.Uri
import java.io.File

data class AudioFileItem(
    val name: String,
    val timestamp: String,
    val file: File? = null,
    val uri: Uri? = null
)