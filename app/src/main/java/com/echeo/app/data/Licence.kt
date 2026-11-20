package com.echeo.app.data

import android.content.Context
import com.echeo.app.BuildConfig
import com.echeo.app.domain.formatDay
import java.time.LocalDate
import java.util.Base64
import android.provider.Settings as AndroidSettings

object LicenceConfig {
    /** true : Echeo ne fonctionne qu'avec une clé valide, vérifiée à chaque ouverture. */
    const val ENFORCED = true

    /** Nom de l'application dans les clés émises par Forge Licence. */
    const val APP_CODE = "echeo"

    /** Secret partagé avec Forge Licence (base64), fourni à la compilation. */
    val SECRET: String get() = BuildConfig.LICENCE_SECRET
}

/** La clé est refusée : le message explique pourquoi, il est affiché à l'utilisateur. */
class LicenceRejected(message: String) : Exception(message)

/**
 * Vérifie une clé émise par Forge Licence, sans serveur ni Internet.
 * La clé (style numéro de série) contient la date d'émission et la durée, et n'est valable
 * que pour ce téléphone et pour Echeo.
 */
class LicenceService(private val context: Context) {

    private val prefs = context.getSharedPreferences("echeo_licence", Context.MODE_PRIVATE)

    /** Code du téléphone à communiquer pour obtenir une clé, au format XXXX-XXXX. */
    fun deviceId(): String {
        val raw = AndroidSettings.Secure.getString(context.contentResolver, AndroidSettings.Secure.ANDROID_ID) ?: "inconnu"
        return SerialKey.deviceCode(raw, LicenceConfig.APP_CODE)
    }

    /** Vérifie une clé saisie à l'activation. */
    fun activate(key: String): Result<SerialLicence> = verify(key)

    /** Vérifie une clé déjà enregistrée, à chaque ouverture de l'app. */
    fun verify(key: String): Result<SerialLicence> = try {
        Result.success(check(key.trim()))
    } catch (e: LicenceRejected) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(LicenceRejected("Clé de licence invalide."))
    }

    /** Dates de la clé, sans contrôle de validité (pour l'affichage et le rappel). */
    fun info(key: String?): SerialLicence? = key?.let { read(it) }

    private fun read(key: String): SerialLicence? {
        if (LicenceConfig.SECRET.isBlank()) return null
        val secret = Base64.getDecoder().decode(LicenceConfig.SECRET)
        return SerialKey.read(secret, LicenceConfig.APP_CODE, deviceId(), key)
    }

    private fun check(key: String): SerialLicence {
        if (LicenceConfig.SECRET.isBlank()) throw LicenceRejected("Licence non configurée dans cette version d'Echeo.")
        val licence = read(key)
            ?: throw LicenceRejected("Clé invalide, ou liée à un autre téléphone. Vérifiez la saisie.")
        val today = LocalDate.now()

        // Empêche de prolonger la licence en reculant la date du téléphone.
        val lastSeen = prefs.getLong(KEY_LAST_SEEN, 0L)
        if (today.toEpochDay() < lastSeen || today.isBefore(licence.issued)) {
            throw LicenceRejected("La date du téléphone semble incorrecte.")
        }
        if (today.isAfter(licence.expiry)) {
            throw LicenceRejected("Licence expirée le ${formatDay(licence.expiry.toEpochDay())}. Demandez une nouvelle clé.")
        }
        prefs.edit().putLong(KEY_LAST_SEEN, today.toEpochDay()).apply()
        return licence
    }

    private companion object {
        const val KEY_LAST_SEEN = "last_seen_day"
    }
}
