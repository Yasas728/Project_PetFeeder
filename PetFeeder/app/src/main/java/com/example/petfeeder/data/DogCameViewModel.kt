package com.example.petfeeder.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class DogCameViewModel : ViewModel(){

 private val TAG = "DogCameViewModel"

    val database = try {
        Firebase.database("https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing Firebase database", e)
        null
    }

    // Mutable state values for the UI to observe
    val Eaten = mutableStateOf(false)
    val Number = mutableStateOf(5)
    val Time = mutableStateOf("")

    init {
        try {
            DogCame()
        } catch (e: Exception) {
            Log.e(TAG, "Error in init block", e)
        }
    }

    private fun DogCame() {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot get variables: database is null")
                return
            }

            val variablesRef = database.getReference("DogCame")


            // Use ValueEventListener to listen for real-time updates
            variablesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d(TAG, "Got Firebase variables data")

                        val EatenValue = snapshot.child("Eaten").getValue(Boolean::class.java) ?: false
                        val NumberValue = snapshot.child("Number").getValue(Int::class.java) ?: 0
                        val TimeValue = snapshot.child("Time").getValue(String::class.java) ?: ""

                        // Update UI state
                        Eaten.value = EatenValue
                        Number.value = NumberValue
                        Time.value = TimeValue

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
}