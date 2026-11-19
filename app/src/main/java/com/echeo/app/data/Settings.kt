package com.echeo.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Réglages de l'app, gardés sur le téléphone. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("echeo_settings", Context.MODE_PRIVATE)

    private val _shopName = MutableStateFlow(prefs.getString(KEY_SHOP, null) ?: DEFAULT_SHOP)
    val shopName: StateFlow<String> = _shopName.asStateFlow()

    private val _warnDays = MutableStateFlow(prefs.getInt(KEY_WARN_DAYS, DEFAULT_WARN_DAYS))
    /** Jours avant l'expiration où un abonnement passe en « Bientôt expiré ». */
    val warnDays: StateFlow<Int> = _warnDays.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getInt(KEY_HOUR, 9))
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _notify = MutableStateFlow(prefs.getBoolean(KEY_NOTIFY, true))
    val notifyEnabled: StateFlow<Boolean> = _notify.asStateFlow()

    private val _waBefore = MutableStateFlow(prefs.getString(KEY_WA_BEFORE, null) ?: WA_BEFORE_DEFAULT)
    val waBefore: StateFlow<String> = _waBefore.asStateFlow()

    private val _waAfter = MutableStateFlow(prefs.getString(KEY_WA_AFTER, null) ?: WA_AFTER_DEFAULT)
    val waAfter: StateFlow<String> = _waAfter.asStateFlow()

    private val _licenceKey = MutableStateFlow(prefs.getString(KEY_LICENCE, null))
    val licenceKey: StateFlow<String?> = _licenceKey.asStateFlow()

    private val _lastBackup = MutableStateFlow(prefs.getLong(KEY_LAST_BACKUP, 0L))
    val lastBackupAt: StateFlow<Long> = _lastBackup.asStateFlow()

    val onboardingDone: Boolean get() = prefs.getBoolean(KEY_ONBOARDING, false)

    fun finishOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
    }

    fun setShopName(value: String) {
        prefs.edit().putString(KEY_SHOP, value).apply()
        _shopName.value = value
    }

    fun setWarnDays(value: Int) {
        prefs.edit().putInt(KEY_WARN_DAYS, value).apply()
        _warnDays.value = value
    }

    fun setReminderHour(value: Int) {
        prefs.edit().putInt(KEY_HOUR, value).apply()
        _reminderHour.value = value
    }

    fun setNotifyEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFY, value).apply()
        _notify.value = value
    }

    fun setWaBefore(value: String) {
        prefs.edit().putString(KEY_WA_BEFORE, value).apply()
        _waBefore.value = value
    }

    fun setWaAfter(value: String) {
        prefs.edit().putString(KEY_WA_AFTER, value).apply()
        _waAfter.value = value
    }

    fun setLicenceKey(value: String?) {
        prefs.edit().putString(KEY_LICENCE, value).apply()
        _licenceKey.value = value
    }

    fun setLastBackup(value: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP, value).apply()
        _lastBackup.value = value
    }

    companion object {
        const val DEFAULT_SHOP = "Ma boutique"
        const val DEFAULT_WARN_DAYS = 7
        const val WA_BEFORE_DEFAULT =
            "Bonjour {nom}, votre abonnement {abonnement} expire le {date}. Souhaitez-vous le renouveler ?"
        const val WA_AFTER_DEFAULT =
            "Bonjour {nom}, votre abonnement {abonnement} a expiré le {date}. Je peux le renouveler dès aujourd'hui."

        private const val KEY_SHOP = "shop_name"
        private const val KEY_WARN_DAYS = "warn_days"
        private const val KEY_HOUR = "reminder_hour"
        private const val KEY_NOTIFY = "notify"
        private const val KEY_WA_BEFORE = "wa_before"
        private const val KEY_WA_AFTER = "wa_after"
        private const val KEY_LICENCE = "licence_key"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val KEY_ONBOARDING = "onboarding_done"
    }
}
