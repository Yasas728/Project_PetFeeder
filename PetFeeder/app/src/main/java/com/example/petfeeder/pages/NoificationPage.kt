package com.example.petfeeder.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petfeeder.NotificationService
import com.example.petfeeder.data.FirebaseIntruderRepository
import com.example.petfeeder.data.DogCameViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun NotificationPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val notificationService = NotificationService(context)

    // Get the shared ViewModel instances
    val firebaseRepository: FirebaseIntruderRepository = viewModel()
    val dogCameViewModel: DogCameViewModel = viewModel()

    // Observe the intruder repository states
    val isHere by firebaseRepository.isHere
    val intruderTime by firebaseRepository.time
    val intruderAlert by firebaseRepository.intruderAlert

    // Observe the dog came states
    val hasEaten by dogCameViewModel.Eaten
    val visitCount by dogCameViewModel.Number
    val lastFeedTime by dogCameViewModel.Time

    // Check if monitoring is active
    val isMonitoring = firebaseRepository.database != null && dogCameViewModel.database != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5FA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // PRIORITY ALERT: Intruder Detection
        if (intruderAlert && isHere) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚨 INTRUDER ALERT!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    if (intruderTime.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detected at: $intruderTime",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Unknown presence detected near feeder",
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }

        // System Status Card
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isMonitoring) Color(0xFFE8F5E8) else Color(0xFFFFF2F2)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "System Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isMonitoring) "✅ Connected & Monitoring" else "❌ Not Connected",
                    fontSize = 16.sp,
                    color = if (isMonitoring) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }
        }

        // Pet Feeding Status - Most Important Info
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasEaten) Color(0xFFE8F5E8) else Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (hasEaten) "🍽️" else "😿",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (hasEaten) "Pet Fed Today" else "Pet Not Fed",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasEaten) Color(0xFF2E7D32) else Color(0xFFFF8F00)
                        )
                        Text(
                            text = if (hasEaten) "✅ All good!" else "⚠️ Needs attention",
                            fontSize = 14.sp,
                            color = if (hasEaten) Color(0xFF2E7D32) else Color(0xFFFF8F00)
                        )
                    }
                }

                if (lastFeedTime.isNotEmpty() && hasEaten) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Last fed: $lastFeedTime",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        // Activity Summary Card
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📊 Today's Activity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Visit Count
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👀",
                            fontSize = 24.sp
                        )
                        Text(
                            text = "$visitCount",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Visits",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }

                    // Activity Level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val activityEmoji = when {
                            visitCount == 0 -> "😴"
                            visitCount < 3 -> "🐢"
                            visitCount < 7 -> "🐕"
                            else -> "🏃"
                        }
                        Text(
                            text = activityEmoji,
                            fontSize = 24.sp
                        )
                        Text(
                            text = when {
                                visitCount == 0 -> "None"
                                visitCount < 3 -> "Low"
                                visitCount < 7 -> "Normal"
                                else -> "High"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        Text(
                            text = "Activity",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }

        // Smart Notifications Card
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🔔 Smart Alerts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Generate smart notifications based on data
                val notifications = mutableListOf<String>()

                if (!hasEaten && visitCount > 2) {
                    notifications.add("🤔 Pet visited $visitCount times but hasn't eaten")
                }
                if (hasEaten && visitCount == 0) {
                    notifications.add("🎉 Pet ate without multiple visits - efficient!")
                }
                if (!hasEaten && visitCount == 0) {
                    notifications.add("😴 No activity detected today")
                }
                if (intruderAlert) {
                    notifications.add("🚨 Security alert - check your pet's safety")
                }
                if (visitCount > 10) {
                    notifications.add("🏃 Very active day - pet visited ${visitCount} times")
                }

                if (notifications.isEmpty()) {
                    notifications.add("✅ All systems normal")
                }

                notifications.forEach { notification ->
                    Text(
                        text = "• $notification",
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun NotificationPagePreview() {
    NotificationPage()
}