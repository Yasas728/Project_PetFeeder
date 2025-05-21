package com.example.petfeeder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import com.example.petfeeder.pages.CameraPage
import com.example.petfeeder.pages.HomePage
import com.example.petfeeder.pages.NotificationPage
import com.example.petfeeder.pages.SchedulePage
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petfeeder.data.ScheduleViewModel
import com.example.petfeeder.data.OtherVariablesViewModel
import com.example.petfeeder.pages.SettingsSchedulePage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun T_B_Navigation(modifier: Modifier = Modifier) {
    // Create shared ViewModel instances
    val scheduleViewModel: ScheduleViewModel = viewModel()
    val otherVariablesViewModel: OtherVariablesViewModel = viewModel()

    // Use the shared state from MainActivity
    var selectedIndex by remember { MainActivity.selectedTabIndex }
    var isSettingsPage by remember { MainActivity.isSettingsPage }

    val navItemList = listOf(
        NavItem(
            label = "Home",
            icon = Icons.Outlined.Home,
            badgeCount = 0,
            topLabel = "Pet Feeder"
        ),
        NavItem(
            label = "Camera",
            icon = Icons.Outlined.PhotoCamera,
            badgeCount = 0,
            topLabel = "Live Camera"
        ),
        NavItem(
            label = "Schedule",
            icon = Icons.Outlined.CalendarToday,
            badgeCount = 0,
            topLabel = "Feeding Schedule"
        ),
        NavItem(
            label = "Alerts",
            icon = Icons.Outlined.Notifications,
            badgeCount = 5,
            topLabel = "Notifications"
        )
    )

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            TopAppBar(
                title = {
                    Text(if (isSettingsPage || selectedIndex == -1) "Add Feeding Schedule" else navItemList[selectedIndex].topLabel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 23.sp)
                },
                actions = {
                    IconButton(onClick = {
                        isSettingsPage = true
                        selectedIndex = -1}) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = "Add")

                    }
                },
                scrollBehavior = scrollBehavior
            )
        },

        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            isSettingsPage = false},
                        label = { Text(text = navItem.label) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (navItem.badgeCount > 0)
                                        Badge(){
                                            Text(text = navItem.badgeCount.toString())
                                        }
                                }
                            ) {
                                Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                            }
                        }
                    )

                }
            }
        }
    ) { innerPadding ->
        if (isSettingsPage || selectedIndex == -1) {
            // Pass the required parameters to SettingsSchedulePage
            SettingsSchedulePage(
                viewModel = scheduleViewModel,
                onNavigateBack = {
                    isSettingsPage = false
                    selectedIndex = 2  // Navigate back to Schedule tab
                },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            BottomContentScreen(
                modifier = Modifier.padding(innerPadding),
                selectedIndex = selectedIndex,
                scheduleViewModel = scheduleViewModel,
                otherVariablesViewModel = otherVariablesViewModel
            )
        }
    }
}

@Composable
fun BottomContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    scheduleViewModel: ScheduleViewModel,
    otherVariablesViewModel: OtherVariablesViewModel
) {
    when(selectedIndex) {
        0 -> HomePage(viewModel = otherVariablesViewModel, modifier = modifier)
        1 -> CameraPage()
        2 -> SchedulePage(viewModel = scheduleViewModel)
        3 -> NotificationPage()
    }
}