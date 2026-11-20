package com.echeo.app.data

import android.content.Context
import com.echeo.app.domain.formatDay
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.provider.Settings as AndroidSettings

object LicenceConfig {
    /** true : Echeo ne fonctionne qu'avec une licence valide, vérifiée auprès du serveur. */
    const val ENFORCED = true

    /** Nom de l'application chez Forge Licence. */
    const val APP_CODE = "echeo"

    /** Serveur de licences de Forge Licence. */
    const val SERVER = "https://forge-licence-api.vercel.app"

    /** Sans Internet, la dernière vérification réussie reste valable ce nombre de jours. */
    const val OFFLINE_GRACE_DAYS = 7L
}

/** La licence est refusée : le message explique pourquoi, il est affiché à l'utilisateur. */
class LicenceRejected(message: String) : Exception(message)

/**
 * Licence vérifiée par le serveur de Forge Licence.
 * « lifetime » : licence à vie, sans date de fin (« expiry » ne sert alors à rien).
 */
data class SerialLicence(val issued: LocalDate, val expiry: LocalDate, val lifetime: Boolean = false)

/**
 * L'activation et la vérification se font en ligne. Le résultat de la dernière vérification est gardé
 * sur le téléphone : sans Internet, Echeo reste utilisable quelques jours (OFFLINE_GRACE_DAYS).
 */
class LicenceService(private val context: Context) {

    private val prefs = context.getSharedPreferences("echeo_licence", Context.MODE_PRIVATE)

    /** Code du téléphone à communiquer pour obtenir une clé, au format XXXX-XXXX. */
    fun deviceId(): String {
        val raw = AndroidSettings.Secure.getString(context.contentResolver, AndroidSettings.Secure.ANDROID_ID) ?: "inconnu"
        return SerialKey.deviceCode(raw, LicenceConfig.APP_CODE)
    }

    /** Active une clé saisie : le serveur la contrôle et démarre la durée. */
    suspend fun activate(key: String): Result<SerialLicence> = withContext(Dispatchers.IO) {
        try {
            val answer = post(
                "/api/activate",
                JSONObject().put("app", LicenceConfig.APP_CODE).put("device", deviceId()).put("key", key),
            )
            if (!answer.optBoolean("ok")) throw LicenceRejected(activationMessage(answer.optString("error")))
            Result.success(accept(answer))
        } catch (e: LicenceRejected) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(LicenceRejected("Connexion impossible. Activez Internet puis réessayez."))
        } catch (e: Exception) {
            Result.failure(LicenceRejected("Activation impossible. Réessayez."))
        }
    }

    /** Vérifie la licence à chaque ouverture de l'app. La clé n'est plus nécessaire : le serveur reconnaît le téléphone. */
    @Suppress("UNUSED_PARAMETER")
    suspend fun verify(key: String): Result<SerialLicence> = withContext(Dispatchers.IO) {
        try {
            val answer = post("/api/check", JSONObject().put("app", LicenceConfig.APP_CODE).put("device", deviceId()))
            if (!answer.optBoolean("ok")) throw LicenceRejected("Vérification impossible. Réessayez.")
            if (answer.optBoolean("valid")) {
                Result.success(accept(answer))
            } else {
                forget()
                throw LicenceRejected(checkMessage(answer.optString("state"), answer))
            }
        } catch (e: LicenceRejected) {
            Result.failure(e)
        } catch (e: Exception) {
            offline()
        }
    }

    /** Dates de la licence connues, sans contrôle (pour l'affichage et le rappel). */
    @Suppress("UNUSED_PARAMETER")
    fun info(key: String?): SerialLicence? {
        if (key == null || !prefs.getBoolean(P_VALID, false)) return null
        val lifetime = prefs.getBoolean(P_LIFETIME, false)
        val expiry = if (lifetime) LocalDate.of(2999, 12, 31) else LocalDate.ofEpochDay(prefs.getLong(P_EXPIRY, 0L))
        return SerialLicence(LocalDate.ofEpochDay(prefs.getLong(P_ISSUED, 0L)), expiry, lifetime)
    }

    /** Sans Internet : la dernière vérification réussie reste valable quelques jours. */
    private fun offline(): Result<SerialLicence> {
        val last = prefs.getLong(P_LAST_OK, 0L)
        val now = System.currentTimeMillis()
        val licence = info(if (prefs.getBoolean(P_VALID, false)) "ok" else null)
        val fresh = last in 1..now && now - last <= LicenceConfig.OFFLINE_GRACE_DAYS * DAY_MS
        val running = licence != null && (licence.lifetime || !LocalDate.now().isAfter(licence.expiry))
        return if (licence != null && fresh && running) {
            Result.success(licence)
        } else {
            Result.failure(
                LicenceRejected("Connexion requise pour vérifier la licence. Activez Internet puis réessayez."),
            )
        }
    }

    private fun accept(answer: JSONObject): SerialLicence {
        val zone = ZoneId.systemDefault()
        val lifetime = answer.optBoolean("lifetime")
        val today = LocalDate.now()
        val expiry = if (lifetime) LocalDate.of(2999, 12, 31) else Instant.parse(answer.getString("expiresAt")).atZone(zone).toLocalDate()
        prefs.edit()
            .putBoolean(P_VALID, true)
            .putBoolean(P_LIFETIME, lifetime)
            .putLong(P_EXPIRY, expiry.toEpochDay())
            .putLong(P_ISSUED, if (prefs.contains(P_ISSUED)) prefs.getLong(P_ISSUED, today.toEpochDay()) else today.toEpochDay())
            .putLong(P_LAST_OK, System.currentTimeMillis())
            .apply()
        return SerialLicence(LocalDate.ofEpochDay(prefs.getLong(P_ISSUED, today.toEpochDay())), expiry, lifetime)
    }

    private fun forget() {
        prefs.edit().putBoolean(P_VALID, false).apply()
    }

    private fun post(path: String, body: JSONObject): JSONObject {
        val connection = URL(LicenceConfig.SERVER + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val stream = if (connection.responseCode < 400) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (text.isBlank()) throw IOException("Réponse vide")
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun activationMessage(error: String): String = when (error) {
        "format" -> "Clé mal saisie. Vérifiez les 20 caractères."
        "invalid" -> "Clé invalide, ou faite pour un autre téléphone. Vérifiez la saisie."
        "revoked" -> "Cette licence a été bloquée. Contactez votre fournisseur."
        "replaced" -> "Cette clé a été remplacée par une plus récente."
        "too_late" -> "Cette clé n'a pas été activée à temps. Demandez-en une nouvelle."
        else -> "Activation impossible. Réessayez."
    }

    private fun checkMessage(state: String, answer: JSONObject): String = when (state) {
        "blocked" -> "Cette licence a été bloquée. Contactez votre fournisseur."
        "expired" -> {
            val end = runCatching { Instant.parse(answer.getString("expiresAt")).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            if (end != null) "Licence expirée le ${formatDay(end.toEpochDay())}. Demandez une nouvelle clé." else "Licence expirée. Demandez une nouvelle clé."
        }
        else -> "Aucune licence active sur ce téléphone. Activez une clé."
    }

    private companion object {
        const val DAY_MS = 86_400_000L
        const val P_VALID = "valid"
        const val P_LIFETIME = "lifetime"
        const val P_EXPIRY = "expiry_day"
        const val P_ISSUED = "issued_day"
        const val P_LAST_OK = "last_ok_at"
    }
}
