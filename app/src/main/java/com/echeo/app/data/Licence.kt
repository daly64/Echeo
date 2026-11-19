package com.echeo.app.data

import android.content.Context
import com.echeo.app.domain.formatDay
import org.json.JSONObject
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.LocalDate
import java.util.Base64
import android.provider.Settings as AndroidSettings

object LicenceConfig {
    /** true : la licence est vérifiée à chaque ouverture. Passer à true une fois PUBLIC_KEY renseignée. */
    const val ENFORCED = false

    /** Clé publique de Forge Licence pour Echeo (base64, format SPKI). */
    const val PUBLIC_KEY = ""

    /** Nom de l'application dans les clés émises par Forge Licence. */
    const val APP_CODE = "echeo"

    /** Préfixe des clés émises par Forge Licence. */
    const val PREFIX = "LIC1"
}

/** La clé est refusée : le message explique pourquoi, il est affiché à l'utilisateur. */
class LicenceRejected(message: String) : Exception(message)

/**
 * Vérifie une clé émise par Forge Licence, sans serveur ni Internet.
 * La clé contient l'app, l'ID du téléphone, la date d'émission et la date d'expiration,
 * signés avec la clé privée de Forge Licence. L'app ne connaît que la clé publique.
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

        val payload = Base64.getUrlDecoder().decode(parts[1])
        val signature = Base64.getUrlDecoder().decode(parts[2])
        val publicKey = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(Base64.getDecoder().decode(LicenceConfig.PUBLIC_KEY)),
        )
        val valid = Signature.getInstance("SHA256withECDSA").run {
            initVerify(publicKey)
            update(payload)
            verify(signature)
        }
        if (!valid) throw LicenceRejected("Clé de licence invalide.")

        val json = JSONObject(String(payload, Charsets.UTF_8))
        if (json.getString("a") != LicenceConfig.APP_CODE) throw LicenceRejected("Cette clé ne concerne pas Echeo.")
        if (json.getString("d") != deviceId()) {
            throw LicenceRejected("Cette licence est liée à un autre téléphone.")
        }
        val issued = LocalDate.ofEpochDay(json.getLong("i"))
        val expiry = LocalDate.ofEpochDay(json.getLong("e"))
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
