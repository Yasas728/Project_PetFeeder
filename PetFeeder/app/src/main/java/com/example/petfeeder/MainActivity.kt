package com.example.petfeeder

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.petfeeder.data.ScheduleViewModel
import com.example.petfeeder.ui.components.NetworkAwareContent
import com.example.petfeeder.ui.theme.PetFeederTheme
import com.example.petfeeder.util.NetworkStatusObserver
import com.example.petfeeder.data.FirebaseIntruderRepository

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ScheduleViewModel>()

    // Properly initialize the FirebaseIntruderRepository as an AndroidViewModel
    private val firebaseIntruderRepository by viewModels<FirebaseIntruderRepository>()

    companion object {
        val isSettingsPage = mutableStateOf(false)
        val selectedTabIndex = mutableStateOf(0)
        var backPressedTime = 0L
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied. You won't receive notifications.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Firebase monitoring is automatically initialized in the repository's init block
        // No need to manually initialize it here

        // Custom back button behavior handler
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    isSettingsPage.value -> {
                        isSettingsPage.value = false
                        selectedTabIndex.value = 2
                    }
                    selectedTabIndex.value != 0 -> {
                        selectedTabIndex.value = 0
                    }
                    else -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime < 2000) {
                            finish()
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
                NetworkStatusObserver()
                NetworkAwareContent {
                    T_B_Navigation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // The repository is automatically listening for changes
        // No need to manually start listening
    }

    override fun onStop() {
        super.onStop()
        // The repository will handle cleanup automatically when the ViewModel is cleared
        // No need to manually stop listening
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    Toast.makeText(
                        this,
                        "Notification permission is needed to alert you about feeding times and security alerts.",
                        Toast.LENGTH_LONG
                    ).show()
                    requestPermissionLauncher.launch(permission)
                }
                else -> {
                    requestPermissionLauncher.launch(permission)
                }
            }
        }
    }
}