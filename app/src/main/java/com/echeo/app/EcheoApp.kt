package com.echeo.app

import android.app.Application
import com.echeo.app.data.EcheoDatabase
import com.echeo.app.data.Settings

/** Point d'entrée : crée la base locale et les réglages une seule fois. */
class EcheoApp : Application() {
    val database: EcheoDatabase by lazy { EcheoDatabase.build(this) }
    val settings: Settings by lazy { Settings(this) }
}
