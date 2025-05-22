package com.example.petfeeder.pages

import FirebaseStorageManager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseStorageManager = remember { FirebaseStorageManager() }

    var isRecording by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile: File? by remember { mutableStateOf(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val storagePermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // No storage permission needed for Android 13+
        } else {
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        }

        if (!audioPermissionGranted) {
            statusMessage = "Audio recording permission required"
        } else if (!storagePermissionGranted) {
            statusMessage = "Storage permission required"
        } else {
            statusMessage = "Permissions granted!"
        }
    }

    // File picker launcher (alternative to recording)
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                uploadAudioFile(it, firebaseStorageManager) { progress ->
                    uploadProgress = progress
                    isUploading = progress < 100
                    statusMessage = if (progress < 100) "Uploading: $progress%" else "Upload completed!"
                }
            }
        }
    }

    // Check permissions on launch
    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFCCC0))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Audio Recorder",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Recording Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Button(
                onClick = {
                    if (!isRecording) {
                        startRecording(context) { recorder, file ->
                            mediaRecorder = recorder
                            audioFile = file
                            isRecording = true
                            statusMessage = "Recording..."
                        }
                    } else {
                        stopRecording(mediaRecorder) {
                            mediaRecorder = null
                            isRecording = false
                            statusMessage = "Recording stopped. Ready to upload!"
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.Green
                ),
                enabled = !isUploading
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }

            Button(
                onClick = {
                    audioFile?.let { file ->
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        scope.launch {
                            uploadAudioFile(uri, firebaseStorageManager) { progress ->
                                uploadProgress = progress
                                isUploading = progress < 100
                                statusMessage = if (progress < 100) "Uploading: $progress%" else "Upload completed!"
                            }
                        }
                    }
                },
                enabled = audioFile != null && !isUploading && !isRecording
            ) {
                Text("Upload Recording")
            }
        }

        // File Picker Button
        Button(
            onClick = {
                filePickerLauncher.launch("audio/*")
            },
            enabled = !isUploading && !isRecording,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text("Select Audio File")
        }

        // Upload Progress
        if (isUploading) {
            LinearProgressIndicator(
                progress = uploadProgress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Status Message
        if (statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.padding(16.dp),
                    color = Color.Black
                )
            }
        }
    }
}

// Helper Functions
private fun startRecording(
    context: Context,
    onRecordingStarted: (MediaRecorder, File) -> Unit
) {
    try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioFile = File(context.getExternalFilesDir(null), "audio_$timeStamp.3gp")

        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }

        onRecordingStarted(mediaRecorder, audioFile)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

private fun stopRecording(mediaRecorder: MediaRecorder?, onRecordingStopped: () -> Unit) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        onRecordingStopped()
    } catch (e: Exception) {
        e.printStackTrace()
        onRecordingStopped()
    }
}

private suspend fun uploadAudioFile(
    uri: Uri,
    firebaseStorageManager: FirebaseStorageManager,
    onProgress: (Int) -> Unit
) {
    val fileName = "audio_${System.currentTimeMillis()}.3gp"
    val result = firebaseStorageManager.uploadAudioWithProgress(uri, fileName, onProgress)

    result.onSuccess { downloadUrl ->
        println("Upload successful: $downloadUrl")
    }.onFailure { exception ->
        println("Upload failed: ${exception.message}")
    }
}