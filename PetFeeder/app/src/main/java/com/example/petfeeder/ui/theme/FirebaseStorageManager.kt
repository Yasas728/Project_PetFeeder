import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.util.*

class FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // Upload audio file with progress
    suspend fun uploadAudioWithProgress(
        fileUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<String> {
        return try {
            val audioRef = storageRef.child("audio/$fileName")
            val uploadTask = audioRef.putFile(fileUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            // Wait for upload to complete
            val taskSnapshot = uploadTask.await()

            // Get download URL
            val downloadUrl = audioRef.downloadUrl.await().toString()
            onProgress(100)

            Result.success(downloadUrl)
        } catch (e: Exception) {
            onProgress(0)
            Result.failure(e)
        }
    }

    // Upload image file with progress
    suspend fun uploadImageWithProgress(
        fileUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<String> {
        return try {
            val imageRef = storageRef.child("images/$fileName")
            val uploadTask = imageRef.putFile(fileUri)

            // Monitor upload progress
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            // Wait for upload to complete
            val taskSnapshot = uploadTask.await()

            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            onProgress(100)

            Result.success(downloadUrl)
        } catch (e: Exception) {
            onProgress(0)
            Result.failure(e)
        }
    }

    // Get all audio files from Firebase Storage
    suspend fun getAudioFiles(): Result<List<StorageFileItem>> {
        return try {
            val audioRef = storageRef.child("audio")
            val listResult = audioRef.listAll().await()

            val files = listResult.items.map { item ->
                val downloadUrl = item.downloadUrl.await().toString()
                val metadata = item.metadata.await()
                StorageFileItem(
                    name = item.name,
                    downloadUrl = downloadUrl,
                    createdTime = metadata.creationTimeMillis,
                    size = metadata.sizeBytes
                )
            }

            Result.success(files.sortedByDescending { it.createdTime })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all images from Firebase Storage
    suspend fun getImageFiles(): Result<List<StorageFileItem>> {
        return try {
            val imagesRef = storageRef.child("images")
            val listResult = imagesRef.listAll().await()

            val files = listResult.items.map { item ->
                val downloadUrl = item.downloadUrl.await().toString()
                val metadata = item.metadata.await()
                StorageFileItem(
                    name = item.name,
                    downloadUrl = downloadUrl,
                    createdTime = metadata.creationTimeMillis,
                    size = metadata.sizeBytes
                )
            }

            Result.success(files.sortedByDescending { it.createdTime })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete file from Firebase Storage
    suspend fun deleteFile(fileName: String, isAudio: Boolean): Result<Unit> {
        return try {
            val fileRef = if (isAudio) {
                storageRef.child("audio/$fileName")
            } else {
                storageRef.child("images/$fileName")
            }

            fileRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create unique filename
    fun createUniqueFileName(originalName: String, extension: String): String {
        val timestamp = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString().take(8)
        return "${originalName}_${timestamp}_${uuid}.$extension"
    }
}

// Data class for Firebase storage files
data class StorageFileItem(
    val name: String,
    val downloadUrl: String,
    val createdTime: Long,
    val size: Long
)