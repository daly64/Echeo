package com.echeo.app

import android.app.Application
import com.echeo.app.data.EcheoDatabase
import com.echeo.app.data.EcheoRepository
import com.echeo.app.data.LicenceService
import com.echeo.app.data.Settings
import com.echeo.app.notify.ExpiryNotifier
import com.echeo.app.notify.ExpiryWorker

/** Point d'entrée : la base locale, les réglages et les rappels, créés une seule fois. */
class EcheoApp : Application() {
    val database: EcheoDatabase by lazy { EcheoDatabase.build(this) }
    val settings: Settings by lazy { Settings(this) }
    val repository: EcheoRepository by lazy { EcheoRepository(database) }
    val licence: LicenceService by lazy { LicenceService(this) }

    override fun onCreate() {
        super.onCreate()
        ExpiryNotifier.createChannel(this)
        ExpiryWorker.schedule(this, settings.reminderHour.value)
    }
}
