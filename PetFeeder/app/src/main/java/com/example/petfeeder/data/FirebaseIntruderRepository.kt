package com.example.petfeeder.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.petfeeder.NotificationService
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class FirebaseIntruderRepository(application: Application) : AndroidViewModel(application) {
    private val TAG = "FirebaseIntruderRepository"
    private val notificationService = NotificationService(application.applicationContext)

    // Use try-catch to handle any Firebase initialization issues
    val database = try {
        Firebase.database("https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing Firebase database", e)
        null
    }

    val isHere = mutableStateOf(true)
    val time = mutableStateOf("")
    val intruderAlert = mutableStateOf(false)

    // Keep track of previous state to detect changes
    private var previousIntruderAlert = false
    private var previousIsHere = false

    init {
        try {
            setupIntruderListener()
        } catch (e: Exception) {
            Log.e(TAG, "Error in init block", e)
        }
    }

    private fun setupIntruderListener() {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot set up listeners: database is null")
                return
            }

            val intruderRef = database.getReference("Intruder")

            // Listen for Intruder data changes
            intruderRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d(TAG, "Got Firebase Intruder data")

                        val isHereValue = snapshot.child("IsHere").getValue(Boolean::class.java) ?: false
                        val timeValue = snapshot.child("Time").getValue(String::class.java) ?: ""

                        // Update UI state
                        isHere.value = isHereValue
                        time.value = timeValue

                        // Check if we need to trigger notification
                        checkAndNotifyIntruder()

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing Firebase Intruder data", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase Intruder operation cancelled", error.toException())
                }
            })

            val variablesRef = database.getReference("Variables")

            // Listen for Variables data changes
            variablesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d(TAG, "Got Firebase Variables data")

                        val intruderAlertValue = snapshot.child("IntruderAlert").getValue(Boolean::class.java) ?: false

                        // Update UI state
                        intruderAlert.value = intruderAlertValue

                        // Check if we need to trigger notification
                        checkAndNotifyIntruder()

                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing Firebase Variables data", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase Variables operation cancelled", error.toException())
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Error in setupIntruderListener method", e)
        }
    }

    private fun checkAndNotifyIntruder() {
        val currentIntruderAlert = intruderAlert.value
        val currentIsHere = isHere.value

        // Only notify if IntruderAlert just became true AND IsHere is true
        // This prevents multiple notifications for the same event
        if (currentIntruderAlert && currentIsHere &&
            (!previousIntruderAlert || !previousIsHere)) {

            Log.d(TAG, "Triggering intruder notification")
            notificationService.showIntruderAlert(time.value)

            // Removed the problematic line that was trying to call UI from ViewModel
            // The UI dialog will be handled by the NotificationPage composable
        }

        // Update previous state
        previousIntruderAlert = currentIntruderAlert
        previousIsHere = currentIsHere
    }

    // Public method to manually check and notify (if needed)
    fun intruderCheck() {
        checkAndNotifyIntruder()
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources if needed
        Log.d(TAG, "FirebaseIntruderRepository cleared")
    }

    fun falseIsHereInDatabase() {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot update intruder alert: database is null")
                return
            }

            // Update Firebase
            database.getReference("Intruder").child("IsHere").setValue(false)
                .addOnSuccessListener {
                    isHere.value = false
                    Log.d(TAG, "Successfully updated IsHere to false")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update IsHere", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating IsHere in database", e)
        }
    }
}