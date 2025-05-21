package com.example.petfeeder.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext

// Singleton object to maintain a single instance of network connectivity state
object NetworkConnectivity {
    val isConnected = mutableStateOf(false)
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun initialize(context: Context) {
        if (connectivityManager == null) {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Check current connection status
            updateConnectionStatus()

            // Create network callback
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isConnected.value = true
                }

                override fun onLost(network: Network) {
                    updateConnectionStatus()
                }
            }

            // Register the callback for all network types
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
        }
    }

    fun updateConnectionStatus() {
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        isConnected.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
        }
        networkCallback = null
        connectivityManager = null
    }
}

@Composable
fun NetworkStatusObserver() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        NetworkConnectivity.initialize(context)

        onDispose {
            NetworkConnectivity.cleanup()
        }
    }
}