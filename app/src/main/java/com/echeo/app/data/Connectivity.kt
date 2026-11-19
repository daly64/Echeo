package com.echeo.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Vrai si le téléphone a une connexion Internet active. */
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
