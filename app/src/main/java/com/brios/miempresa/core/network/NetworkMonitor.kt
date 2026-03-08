package com.brios.miempresa.core.network

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Singleton
class NetworkMonitor
    @Inject
    constructor(
        private val connectivityManager: ConnectivityManager,
    ) {
        fun observeOnlineStatus(): Flow<Boolean> =
            callbackFlow {
                val callback =
                    object : ConnectivityManager.NetworkCallback() {
                        private val onlineNetworks = mutableSetOf<Network>()

                        override fun onAvailable(network: Network) {
                            val capabilities = connectivityManager.getNetworkCapabilities(network)
                            if (hasInternet(capabilities)) {
                                onlineNetworks += network
                                trySend(true)
                            }
                        }

                        override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities,
                        ) {
                            if (hasInternet(networkCapabilities)) {
                                onlineNetworks += network
                            } else {
                                onlineNetworks -= network
                            }
                            trySend(onlineNetworks.isNotEmpty())
                        }

                        override fun onLost(network: Network) {
                            onlineNetworks -= network
                            trySend(onlineNetworks.isNotEmpty())
                        }

                        override fun onUnavailable() {
                            trySend(false)
                        }
                    }

                val request =
                    NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

                connectivityManager.registerNetworkCallback(request, callback)
                trySend(isOnlineNow())

                awaitClose {
                    try {
                        connectivityManager.unregisterNetworkCallback(callback)
                    } catch (error: IllegalArgumentException) {
                        Log.w("NetworkMonitor", "Connectivity callback already unregistered", error)
                    }
                }
            }.distinctUntilChanged()

        fun isOnlineNow(): Boolean {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return hasInternet(capabilities)
        }

        private fun hasInternet(capabilities: NetworkCapabilities?): Boolean {
            if (capabilities == null) return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }
