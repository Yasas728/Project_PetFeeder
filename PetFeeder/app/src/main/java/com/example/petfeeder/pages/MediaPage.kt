package com.example.petfeeder.pages

import FirebaseStorageManager
import android.Manifest
import androidx.compose.foundation.lazy.items
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.petfeeder.data.AudioFileItem
import com.example.petfeeder.data.ImageItem
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseStorageManager = remember { FirebaseStorageManager() }

    var selectedTab by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFiles by remember { mutableStateOf(listOf<AudioFileItem>()) }
    var capturedImages by remember { mutableStateOf(listOf<ImageItem>()) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val storagePermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else {
            permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        }

        if (!audioPermissionGranted) {
            statusMessage = "Audio recording permission required"
        } else if (!storagePermissionGranted) {
            statusMessage = "Storage permission required"
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val timeStamp = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date())
            val fileName = "Selected Audio"
            audioFiles = audioFiles + AudioFileItem(fileName, timeStamp, uri = it)
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val timeStamp = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date())
            capturedImages = capturedImages + ImageItem(it, timeStamp)
        }
    }

    // Check permissions and load files on launch
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

        // Load existing audio files
        loadExistingAudioFiles(context) { files ->
            audioFiles = files
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3FF))
            .padding(16.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Audio Files",
                        tint = if (selectedTab == 0) Color.Black else Color.Gray
                    )
                    Text(
                        "Audio Files",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Captured Images",
                        tint = if (selectedTab == 1) Color.Black else Color.Gray
                    )
                    Text(
                        "Captured Images",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> AudioFilesContent(
                audioFiles = audioFiles,
                isRecording = isRecording,
                isUploading = isUploading,
                uploadProgress = uploadProgress,
                onStartRecording = {
                    startRecording(context) { recorder, file ->
                        mediaRecorder = recorder
                        isRecording = true
                        statusMessage = "Recording..."
                    }
                },
                onStopRecording = {
                    stopRecording(mediaRecorder) { file ->
                        mediaRecorder = null
                        isRecording = false
                        file?.let {
                            val timeStamp = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date())
                            val fileName = "Recording ${audioFiles.size + 1}"
                            audioFiles = audioFiles + AudioFileItem(fileName, timeStamp, file = it)
                        }
                        statusMessage = ""
                    }
                },
                onSelectAudioFile = {
                    filePickerLauncher.launch("audio/*")
                },
                onUploadAudio = { audioItem ->
                    scope.launch {
                        val uri = audioItem.uri ?: audioItem.file?.let { file ->
                            FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                        }

                        uri?.let {
                            uploadAudioFile(it, firebaseStorageManager) { progress ->
                                uploadProgress = progress
                                isUploading = progress < 100
                            }
                        }
                    }
                },
                onDeleteAudio = { audioItem ->
                    audioFiles = audioFiles - audioItem
                    audioItem.file?.delete()
                }
            )
            1 -> CapturedImagesContent(
                images = capturedImages,
                onAddImage = {
                    imagePickerLauncher.launch("image/*")
                },
                onDeleteImage = { imageItem ->
                    capturedImages = capturedImages - imageItem
                }
            )
        }

        // Status message
        if (statusMessage.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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

@Composable
fun AudioFilesContent(
    audioFiles: List<AudioFileItem>,
    isRecording: Boolean,
    isUploading: Boolean,
    uploadProgress: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSelectAudioFile: () -> Unit,
    onUploadAudio: (AudioFileItem) -> Unit,
    onDeleteAudio: (AudioFileItem) -> Unit
) {
    Column {
        // Upload Audio Button
        Button(
            onClick = onSelectAudioFile,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            enabled = !isRecording && !isUploading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = "Upload Audio",
                    tint = Color.Black
                )
                Text(
                    "Upload Audio",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Recording Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Button(
                onClick = { if (!isRecording) onStartRecording() else onStopRecording() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color(0xFF4CAF50)
                ),
                enabled = !isUploading
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }
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

        // Audio Files List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                count = audioFiles.size,
                itemContent = { index ->
                    val audioItem = audioFiles[index]
                    AudioFileCard(
                        audioItem = audioItem,
                        onPlay = { /* TODO: Implement audio playback */ },
                        onUpload = { onUploadAudio(audioItem) },
                        onDelete = { onDeleteAudio(audioItem) },
                        isUploading = isUploading
                    )
                }
            )
        }
    }
}

@Composable
fun AudioFileCard(
    audioItem: AudioFileItem,
    onPlay: () -> Unit,
    onUpload: () -> Unit,
    onDelete: () -> Unit,
    isUploading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audioItem.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = audioItem.timestamp,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onPlay,
                    enabled = !isUploading
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color(0xFF4CAF50)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !isUploading
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun CapturedImagesContent(
    images: List<ImageItem>,
    onAddImage: () -> Unit,
    onDeleteImage: (ImageItem) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onAddImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Add Image",
                        tint = Color.Black
                    )
                    Text(
                        "Add Image",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        items(images) { imageItem ->
            ImageCard(
                imageItem = imageItem,
                onDelete = { onDeleteImage(imageItem) }
            )
        }
    }
}

@Composable
fun ImageCard(
    imageItem: ImageItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder for image - you can replace this with actual image loading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = "Image placeholder",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
            }

            // Timestamp overlay
            Text(
                text = imageItem.timestamp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp),
                color = Color.White,
                fontSize = 12.sp
            )

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
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

private fun stopRecording(
    mediaRecorder: MediaRecorder?,
    onRecordingStopped: (File?) -> Unit
) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        onRecordingStopped(null) // You'll need to pass the file reference properly
    } catch (e: Exception) {
        e.printStackTrace()
        onRecordingStopped(null)
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

private fun loadExistingAudioFiles(context: Context, onFilesLoaded: (List<AudioFileItem>) -> Unit) {
    val audioDir = context.getExternalFilesDir(null)
    val audioFiles = audioDir?.listFiles { file -> file.extension == "3gp" }?.map { file ->
        val timeStamp = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
            .format(Date(file.lastModified()))
        AudioFileItem(file.nameWithoutExtension, timeStamp, file = file)
    } ?: emptyList()

    onFilesLoaded(audioFiles)
}