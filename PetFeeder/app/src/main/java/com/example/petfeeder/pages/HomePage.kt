package com.example.petfeeder.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowCircleUp
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petfeeder.MainActivity
import com.example.petfeeder.data.OtherVariablesViewModel
import com.example.petfeeder.data.ScheduleViewModel
import com.example.petfeeder.ui.theme.Purple40
import com.example.petfeeder.ui.theme.Purple80

@Composable
fun HomePage(
    viewModel: OtherVariablesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe variables from ViewModel
    val foodLevel by viewModel.mainFoodLevel
    val portionSize by viewModel.portionSize
    val intruderAlert by viewModel.intruderAlert
    val feedNow by viewModel.feedNow
    val nextFeeding by viewModel.nextFeeding

    // Define a purple accent color since it was commented out in the original
    val purpleAccent = Purple40 // Using Purple40 as a replacement for PurpleAccent

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5FA))
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Status Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Next Feeding Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Clock Icon
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = "Clock",
                                tint = Purple40,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Next Feeding",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = nextFeeding,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Food Level Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Gauge Icon
                            Icon(
                                imageVector = Icons.Rounded.Pets, // Replace with a gauge icon
                                contentDescription = "Food Level",
                                tint = purpleAccent,
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Food Level",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Food level progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(foodLevel.toFloat())
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(purpleAccent)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${(foodLevel * 100).toInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Feed Now Button
                Button(
                    onClick = {
                        // Add feed now logic here
                        viewModel.triggerFeedNow(viewModel.portionSize.value)
                        Toast.makeText(context, "Feeding now!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purpleAccent)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (feedNow) Icons.Rounded.EmojiFoodBeverage else Icons.Rounded.KeyboardDoubleArrowUp,
                            contentDescription = "Feed Now",
                            tint = if (feedNow) Color.LightGray else Color.White,
                            modifier = Modifier.size(29.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (feedNow) "Feeding..." else "Feed Now",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (feedNow) Color.LightGray else Color.White
                        )
                    }
                }
            }

            // Add Feeder Settings Section
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Title and Subtitle
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Feeder Settings",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                        }

                        Divider(color = Color.LightGray.copy(alpha = 2.5f))

                        // Notifications Setting
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Intruder Alert",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )

                                Text(
                                    text = "Receive notifications when an intruder is detected",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // Custom Switch - Updated to use the ViewModel's intruderAlert state and update Firebase
                            Switch(
                                checked = intruderAlert,
                                onCheckedChange = { isChecked ->
                                    // Update Firebase with the new value
                                    viewModel.updateIntruderAlertInDatabase(isChecked)
                                    // Show toast for feedback (optional)
                                    Toast.makeText(
                                        context,
                                        if (isChecked) "Intruder alerts enabled" else "Intruder alerts disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Purple40,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Purple80
                                )
                            )
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        // Portion Size Setting
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Portion Size",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            // This is a hidden slider for interaction
                            Slider(
                                value = when(portionSize) {
                                    1 -> 0f
                                    2 -> 0.5f
                                    3 -> 1f
                                    else -> 0.5f
                                },
                                onValueChange = { value ->
                                    // Update the portion size based on slider position
                                    val newPortionSize = when {
                                        value < 0.33f -> 1 // Small
                                        value < 0.66f -> 2 // Medium
                                        else -> 3          // Large
                                    }
                                    updatePortionSize(viewModel, newPortionSize)
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = Purple40,
                                    activeTrackColor = Purple40,
                                    inactiveTrackColor = Color(0xFFE6E6F2)
                                ),
                                modifier = Modifier.fillMaxWidth().height(25.dp).clip(CircleShape)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Small",
                                    fontSize = 14.sp,
                                    color = if (portionSize == 1) Purple40 else Color.Gray
                                )

                                Text(
                                    text = "Medium",
                                    fontSize = 14.sp,
                                    color = if (portionSize == 2) Purple40 else Color.Gray
                                )

                                Text(
                                    text = "Large",
                                    fontSize = 14.sp,
                                    color = if (portionSize == 3) Purple40 else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to update portion size in ViewModel
private fun updatePortionSize(viewModel: OtherVariablesViewModel, size: Int) {
    try {
        if (size in 1..3) {
            viewModel.portionSize.value = size

            // Update Firebase if database reference exists
            viewModel.updatePortionSizeInDatabase(size)
        }
    } catch (e: Exception) {
        android.util.Log.e("HomePage", "Error updating portion size", e)
    }
}