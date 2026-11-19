package com.echeo.app.data

import android.content.Context
import com.echeo.app.domain.formatDay
import org.json.JSONObject
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDate
import android.provider.Settings as AndroidSettings

object LicenceConfig {
    /** true : la licence est vérifiée à chaque ouverture. Passer à true une fois PUBLIC_KEY renseignée. */
    const val ENFORCED = false

    /** Clé publique du générateur (base64, format SPKI). Donnée par `echeo_licence.py keygen`. */
    const val PUBLIC_KEY = ""

    /** Durée de validité d'une clé, en mois après sa génération. */
    const val VALID_MONTHS = 1L

    const val PREFIX = "ECH1"
}

/** La clé est refusée : le message explique pourquoi, il est affiché à l'utilisateur. */
class LicenceRejected(message: String) : Exception(message)

/**
 * Vérifie une clé de licence sans serveur. Une clé contient l'ID du téléphone et la date
 * de génération, signées avec la clé privée du générateur. L'app ne connaît que la clé publique.
 */
class LicenceService(private val context: Context) {

    private val prefs = context.getSharedPreferences("echeo_licence", Context.MODE_PRIVATE)

    /** Identifiant stable du téléphone, au format ECH-XXXX-XXXX. */
    fun deviceId(): String {
        val raw = AndroidSettings.Secure.getString(context.contentResolver, AndroidSettings.Secure.ANDROID_ID) ?: "inconnu"
        val hex = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02X".format(it) }
        return "ECH-${hex.substring(0, 4)}-${hex.substring(4, 8)}"
    }

    /** Vérifie une clé saisie à l'activation. */
    fun activate(key: String): Result<Unit> = verify(key)

    /** Vérifie une clé déjà enregistrée, à chaque ouverture de l'app. */
    fun verify(key: String): Result<Unit> = try {
        check(key.trim())
        Result.success(Unit)
    } catch (e: LicenceRejected) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(LicenceRejected("Clé de licence invalide."))
    }

    private fun check(key: String) {
        if (LicenceConfig.PUBLIC_KEY.isBlank()) throw LicenceRejected("Clé publique non configurée.")
        val parts = key.split(".")
        if (parts.size != 3 || parts[0] != LicenceConfig.PREFIX) throw LicenceRejected("Clé de licence invalide.")

        val payload = java.util.Base64.getUrlDecoder().decode(parts[1])
        val signature = java.util.Base64.getUrlDecoder().decode(parts[2])
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(java.util.Base64.getDecoder().decode(LicenceConfig.PUBLIC_KEY)),
        )
        val valid = Signature.getInstance("SHA256withECDSA").run {
            initVerify(publicKey)
            update(payload)
            verify(signature)
        }
        if (!valid) throw LicenceRejected("Clé de licence invalide.")

        val json = JSONObject(String(payload, Charsets.UTF_8))
        if (json.getString("d") != deviceId()) {
            throw LicenceRejected("Cette licence est liée à un autre téléphone.")
        }
        val issued = LocalDate.ofEpochDay(json.getLong("i"))
        val expiry = issued.plusMonths(LicenceConfig.VALID_MONTHS)
        val today = LocalDate.now()

        // Empêche de prolonger la licence en reculant la date du téléphone.
        val lastSeen = prefs.getLong(KEY_LAST_SEEN, 0L)
        if (today.toEpochDay() < lastSeen || today.isBefore(issued)) {
            throw LicenceRejected("La date du téléphone semble incorrecte.")
        }
        if (today.isAfter(expiry)) {
            throw LicenceRejected("Licence expirée le ${formatDay(expiry.toEpochDay())}. Demandez une nouvelle clé.")
        }
        prefs.edit().putLong(KEY_LAST_SEEN, today.toEpochDay()).apply()
    }

    private companion object {
        const val KEY_LAST_SEEN = "last_seen_day"
    }
}
