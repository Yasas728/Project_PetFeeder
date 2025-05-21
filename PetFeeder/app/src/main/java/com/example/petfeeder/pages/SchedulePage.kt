package com.example.petfeeder.pages

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petfeeder.MainActivity
import com.example.petfeeder.data.Schedule
import com.example.petfeeder.data.ScheduleViewModel
import com.example.petfeeder.ui.theme.Purple40
import com.example.petfeeder.ui.theme.Purple80
import com.example.petfeeder.ui.theme.PurpleGrey40

@Composable
fun SchedulePage(
    viewModel: ScheduleViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEEEF5))
            .padding(top = 80.dp)
            .padding(bottom = 86.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            MainActivity.isSettingsPage.value = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple40)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Feeding Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                items(viewModel.schedules) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggle = {
                            val updatedSchedule = schedule.copy(isEnable = !schedule.isEnable)
                            viewModel.updateSchedule(updatedSchedule)
                        },
                        onEdit = {
                            // Could implement edit functionality here
                            // For now, just navigate to settings page
                            MainActivity.isSettingsPage.value = true
                        },
                        onDelete = {
                            viewModel.deleteSchedule(schedule.id)
                        }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ScheduleCard(
    schedule: Schedule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time display with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "Time",
                        tint = Color(0xFF9D84F8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%02d:%02d", schedule.timeHour, schedule.timeMinute),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Toggle switch
                Switch(
                    checked = schedule.isEnable,
                    onCheckedChange = { onToggle() },
                    modifier = Modifier.scale(0.7f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF9D84F8),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Day circles - need to use the actual boolean values from schedule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DayCircle("M", schedule.Mon)
                DayCircle("T", schedule.Tue)
                DayCircle("W", schedule.Wed)
                DayCircle("T", schedule.Thu)
                DayCircle("F", schedule.Fri)
                DayCircle("S", schedule.Sat)
                DayCircle("S", schedule.Sun)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Edit and Delete buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            Toast.makeText(context, "Schedule deleted!", Toast.LENGTH_SHORT).show()
                            onDelete() }
                )
            }
        }
    }
}

@Composable
fun DayCircle(day: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF9D84F8) else Color.LightGray.copy(alpha = 0.3f))
            .border(1.dp, if (isSelected) Color(0xFF9D84F8) else Color.LightGray, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}