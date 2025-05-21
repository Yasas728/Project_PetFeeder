package com.example.petfeeder.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petfeeder.data.Schedule
import com.example.petfeeder.data.ScheduleViewModel

@Composable
fun SettingsSchedulePage(
    viewModel: ScheduleViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val dayMapping = mapOf(
        "Mon" to "M",
        "Tue" to "T",
        "Wed" to "W",
        "Thu" to "T",
        "Fri" to "F",
        "Sat" to "S",
        "Sun" to "S"
    )
    val selectedDays = remember { mutableStateListOf<String>() }

    // For time picker implementation
    var timeHour by remember { mutableStateOf(12) }
    var timeMinute by remember { mutableStateOf(0) }
    var timeText by remember { mutableStateOf("12:00") }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEF5))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Title with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Time",
                        tint = Color(0xFF9D84F8),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = " Add Feeding Schedule",
                        fontSize = 20.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time section
                Text(
                    text = "Time",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable { showTimePicker = true }
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeText,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Select time",
                        )
                    }
                }

                // Days section
                Text(
                    text = "Days",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for ((key,value) in dayMapping) {
                        val isSelected = selectedDays.contains(key)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF9D84F8) else Color.Transparent)
                                .border(1.dp, if (isSelected) Color(0xFF9D84F8) else Color.Gray, CircleShape)
                                .clickable {
                                    if (isSelected) {
                                        selectedDays.remove(key)
                                    } else {
                                        selectedDays.add(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = value,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Color.Black
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onNavigateBack() },
                        modifier = Modifier
                            .height(45.dp)
                            .width(100.dp),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(1.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        modifier = Modifier
                            .height(45.dp)
                            .width(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            // Create Schedule object from UI data
                            val newSchedule = Schedule(
                                id = 0, // This will be set by the ViewModel
                                isEnable = true,
                                timeHour = timeHour,
                                timeMinute = timeMinute,
                                Mon = selectedDays.contains("Mon"),
                                Tue = selectedDays.contains("Tue"),
                                Wed = selectedDays.contains("Wed"),
                                Thu = selectedDays.contains("Thu"),
                                Fri = selectedDays.contains("Fri"),
                                Sat = selectedDays.contains("Sat"),
                                Sun = selectedDays.contains("Sun")
                            )

                            // Add schedule to Firebase via ViewModel
                            viewModel.addSchedule(newSchedule)

                            // Navigate back after saving
                            onNavigateBack()

                            Toast.makeText(context, "New Schedule added", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D84F8)),
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(1.dp)
                        )
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = { hour, minute ->
                timeHour = hour
                timeMinute = minute
                timeText = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
        text = {
            Column {
                // Simple time picker UI
                // In a real app, you would use Material3's TimePicker here
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hours picker
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 0..23
                    )

                    Text(
                        text = ":",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minutes picker
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedHour, selectedMinute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Text("▲", fontSize = 20.sp)
        }

        Text(
            text = String.format("%02d", value),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = {
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text("▼", fontSize = 20.sp)
        }
    }
}