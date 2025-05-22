import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.net.Uri
import kotlinx.coroutines.tasks.await

class FirebaseStorageManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadAudioFile(audioUri: Uri, fileName: String): Result<String> {
        return try {
            val audioRef = storageRef.child("audio/$fileName")
            val uploadTask = audioRef.putFile(audioUri).await()
            val downloadUrl = audioRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAudioWithProgress(
        audioUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<String> {
        return try {
            val audioRef = storageRef.child("audio/$fileName")
            val uploadTask = audioRef.putFile(audioUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()
            val downloadUrl = audioRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}