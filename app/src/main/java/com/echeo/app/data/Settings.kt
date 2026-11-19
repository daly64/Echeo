package com.echeo.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Réglages simples de l'app, gardés sur le téléphone. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("echeo_settings", Context.MODE_PRIVATE)

    private val _shopName = MutableStateFlow(prefs.getString(KEY_SHOP, null) ?: DEFAULT_SHOP)
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _warnDays = MutableStateFlow(prefs.getInt(KEY_WARN_DAYS, DEFAULT_WARN_DAYS))

    /** Nombre de jours avant l'expiration où un abonnement passe en « Bientôt expiré ». */
    val warnDays: StateFlow<Int> = _warnDays.asStateFlow()

    fun setShopName(name: String) {
        prefs.edit().putString(KEY_SHOP, name).apply()
        _shopName.value = name
    }

    fun setWarnDays(days: Int) {
        prefs.edit().putInt(KEY_WARN_DAYS, days).apply()
        _warnDays.value = days
    }

    companion object {
        const val DEFAULT_SHOP = "Ma boutique"
        const val DEFAULT_WARN_DAYS = 7
        private const val KEY_SHOP = "shop_name"
        private const val KEY_WARN_DAYS = "warn_days"
    }
}
