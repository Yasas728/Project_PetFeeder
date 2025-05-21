package com.example.petfeeder.data

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ScheduleViewModel : ViewModel() {
    private val TAG = "ScheduleViewModel"

    // Use try-catch to handle any Firebase initialization issues
    private val database = try {
        Firebase.database("https://petfeederdatabase-bd940-default-rtdb.asia-southeast1.firebasedatabase.app")
    } catch (e: Exception) {
        Log.e(TAG, "Error initializing Firebase database", e)
        null
    }

    val schedules = mutableStateListOf<Schedule>()

    init {
        try {
            getSchedules()
        } catch (e: Exception) {
            Log.e(TAG, "Error in init block", e)
        }
    }

    fun getSchedules() {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot get schedules: database is null")
                return
            }

            val schedulesRef = database.getReference("Schedules")

            // Use ValueEventListener to listen for real-time updates
            schedulesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d(TAG, "Got Firebase data: ${snapshot.childrenCount} items")
                        schedules.clear()

                        for (scheduleSnapshot in snapshot.children) {
                            try {
                                val id = scheduleSnapshot.child("id").getValue(Int::class.java) ?: 0
                                val isEnable = scheduleSnapshot.child("enable").getValue(Boolean::class.java) ?: false
                                val timeHour = scheduleSnapshot.child("timeHour").getValue(Int::class.java) ?: 0
                                val timeMinute = scheduleSnapshot.child("timeMinute").getValue(Int::class.java) ?: 0
                                val Mon = scheduleSnapshot.child("mon").getValue(Boolean::class.java) ?: false
                                val Tue = scheduleSnapshot.child("tue").getValue(Boolean::class.java) ?: false
                                val Wed = scheduleSnapshot.child("wed").getValue(Boolean::class.java) ?: false
                                val Thu = scheduleSnapshot.child("thu").getValue(Boolean::class.java) ?: false
                                val Fri = scheduleSnapshot.child("fri").getValue(Boolean::class.java) ?: false

                                val Sat = scheduleSnapshot.child("sat").getValue(Boolean::class.java) ?: false
                                val Sun = scheduleSnapshot.child("sun").getValue(Boolean::class.java) ?: false

                                val schedule = Schedule(
                                    id = id,
                                    isEnable = isEnable,
                                    timeHour = timeHour,
                                    timeMinute = timeMinute,
                                    Mon = Mon,
                                    Tue = Tue,
                                    Wed = Wed,
                                    Thu = Thu,
                                    Fri = Fri,
                                    Sat = Sat,
                                    Sun = Sun
                                )

                                schedules.add(schedule)
                                Log.d(TAG, "Added schedule: $schedule")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing schedule item", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing Firebase results", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Firebase operation cancelled", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in getSchedules method", e)
        }
    }

    // Add methods to update schedules in Firebase
    fun updateSchedule(schedule: Schedule) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot update schedule: database is null")
                return
            }

            database.getReference("Schedules").child(schedule.id.toString())
                .setValue(schedule)
                .addOnSuccessListener {
                    Log.d(TAG, "Schedule updated successfully: $schedule")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update schedule", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateSchedule method", e)
        }
    }

    // Method to add a new schedule
    fun addSchedule(schedule: Schedule) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot add schedule: database is null")
                return
            }

            // Generate a new ID if needed
            val newId = if (schedules.isEmpty()) 0 else schedules.maxOf { it.id } + 1
            val newSchedule = schedule.copy(id = newId)

            database.getReference("Schedules").child(newId.toString())
                .setValue(newSchedule)
                .addOnSuccessListener {
                    Log.d(TAG, "Schedule added successfully: $newSchedule")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add schedule", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in addSchedule method", e)
        }
    }

    // Method to delete a schedule
    fun deleteSchedule(scheduleId: Int) {
        try {
            if (database == null) {
                Log.e(TAG, "Cannot delete schedule: database is null")
                return
            }

            database.getReference("Schedules").child(scheduleId.toString())
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Schedule deleted successfully: $scheduleId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to delete schedule", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in deleteSchedule method", e)
        }
    }
}