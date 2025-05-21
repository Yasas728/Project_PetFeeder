package com.example.petfeeder.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class OtherVariablesViewModel : ViewModel() {
    private val TAG = "OtherVariablesViewModel"

    // Use try-catch to handle any Firebase initialization issues
    val database = try {
        Firebase.database("https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing Firebase database", e)
        null
    }

    // Mutable state values for the UI to observe
    val feedNow = mutableStateOf(false)
    val mainFoodLevel = mutableStateOf(0.00) // Default to 75% as shown in your design
    val portionSize = mutableStateOf(1)
    val intruderAlert = mutableStateOf(true)
    val nextFeeding = mutableStateOf("")

    init {
        try {
            getVariables()
        } catch (e: Exception) {
            Log.e(TAG, "Error in init block", e)
        }
    }

    private fun getVariables() {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot get variables: database is null")
                return
            }

            val variablesRef = database.getReference("Variables")

            // Use ValueEventListener to listen for real-time updates
            variablesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d(TAG, "Got Firebase variables data")

                        val feedNowValue = snapshot.child("FeedNow").getValue(Boolean::class.java) ?: false
                        val mainFoodLevelValue = snapshot.child("MainFoodLevel").getValue(Double::class.java) ?: 0.78
                        val portionSizeValue = snapshot.child("PotionSize").getValue(Int::class.java) ?: 1
                        val intruderAlertValue = snapshot.child("IntruderAlert").getValue(Boolean::class.java) ?: false
                        val nextFeedingValue = snapshot.child("NextFeeding").getValue(String::class.java) ?: ""

                        // Update UI state
                        feedNow.value = feedNowValue
                        mainFoodLevel.value = mainFoodLevelValue
                        portionSize.value = portionSizeValue
                        intruderAlert.value = intruderAlertValue
                        nextFeeding.value = nextFeedingValue

                        Log.d(TAG, "Updated variables: FeedNow=$feedNowValue, FoodLevel=$mainFoodLevelValue, PortionSize=$portionSizeValue,IntruderAlert=$intruderAlertValue")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing Firebase results", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase operation cancelled", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in getVariables method", e)
        }
    }

    fun triggerFeedNow(portionSize: Int) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot trigger feed now: database is null")
                return
            }

            // Update Firebase
            database.getReference("Variables").child("FeedNow").setValue(true)
                .addOnSuccessListener {
                    Log.d(TAG, "Feed now triggered successfully")

                    // Update portion size
                    database.getReference("Variables").child("PotionSize").setValue(portionSize)
                        .addOnSuccessListener {
                            Log.d(TAG, "Portion size updated to $portionSize")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to update portion size", e)
                        }

                    // Reset the feed now flag after a short delay (simulating hardware response)
                    android.os.Handler().postDelayed({
                        database.getReference("Variables").child("FeedNow").setValue(false)
                            .addOnSuccessListener {
                                Log.d(TAG, "Feed now reset successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to reset feed now", e)
                            }
                    }, 3000*15*20) // 3 seconds delay
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to trigger feed now", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in triggerFeedNow method", e)
        }
    }

    // New method to update portion size in database
    fun updatePortionSizeInDatabase(size: Int) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot update portion size: database is null")
                return
            }

            // Update Firebase
            database.getReference("Variables").child("PotionSize").setValue(size)
                .addOnSuccessListener {
                    Log.d(TAG, "Portion size updated to $size")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update portion size", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating portion size in database", e)
        }
    }

    // New method to update intruder alert setting in database
    fun updateIntruderAlertInDatabase(enabled: Boolean) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot update intruder alert: database is null")
                return
            }

            // Update Firebase
            database.getReference("Variables").child("IntruderAlert").setValue(enabled)
                .addOnSuccessListener {
                    Log.d(TAG, "Intruder alert updated to $enabled")
                    intruderAlert.value = enabled
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update intruder alert", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating intruder alert in database", e)
        }
    }
}