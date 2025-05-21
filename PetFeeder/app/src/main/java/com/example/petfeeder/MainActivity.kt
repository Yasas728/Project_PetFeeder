package com.example.petfeeder

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.example.petfeeder.data.ScheduleViewModel
import com.example.petfeeder.ui.components.NetworkAwareContent
import com.example.petfeeder.ui.theme.PetFeederTheme
import com.example.petfeeder.util.NetworkStatusObserver

class MainActivity : ComponentActivity() {

    val viewModel by viewModels<ScheduleViewModel>()

    companion object{
        val isSettingsPage = mutableStateOf(false)
        val selectedTabIndex = mutableStateOf(0)
        // Track if back button was recently pressed
        var backPressedTime = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Custom back button behavior handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    // If in settings page, return to previous screen
                    isSettingsPage.value -> {
                        isSettingsPage.value = false
                        selectedTabIndex.value = 2 // Go back to Schedule tab
                    }

                    // If not on home screen, go to home screen
                    selectedTabIndex.value != 0 -> {
                        selectedTabIndex.value = 0
                    }

                    // If on home screen, implement double-press to exit
                    else -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime < 2000) {
                            // If double-pressed within 2 seconds, actually exit the app
                            this.remove() // Remove callback to allow default back behavior
                            onBackPressed() // Call the system back press
                        } else {
                            backPressedTime = currentTime
                            Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })

        setContent {
            PetFeederTheme {
                // Add the NetworkStatusObserver to start monitoring connectivity
                NetworkStatusObserver()

                // Wrap the entire UI in the NetworkAwareContent
                NetworkAwareContent {
                    // Your original navigation - exactly as it was before
                    T_B_Navigation()
                }
            }
        }
    }
}