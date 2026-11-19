package com.echeo.app.data

import android.content.Context
import android.provider.Settings as AndroidSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

object LicenceConfig {
    /** Passer à true une fois le serveur d'activation en service. */
    const val ENFORCED = false

    /** Adresse du serveur d'activation. Vide tant qu'il n'est pas déployé. */
    const val SERVER_URL = ""
}

/** Pas de connexion Internet : l'app ne peut pas vérifier la licence. */
class NetworkUnavailable : IOException("Connexion Internet requise")

/** Le serveur refuse la clé (invalide, révoquée ou liée à un autre téléphone). */
class LicenceRejected(message: String) : Exception(message)

class LicenceService(private val context: Context) {

    /** Identifiant stable du téléphone, au format ECH-XXXX-XXXX. */
    fun deviceId(): String {
        val raw = AndroidSettings.Secure.getString(context.contentResolver, AndroidSettings.Secure.ANDROID_ID) ?: "inconnu"
        val hex = MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02X".format(it) }
        return "ECH-${hex.substring(0, 4)}-${hex.substring(4, 8)}"
    }

    suspend fun activate(key: String): Result<Unit> = call("activate", key)

    suspend fun verify(key: String): Result<Unit> = call("verify", key)

    private suspend fun call(path: String, key: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (LicenceConfig.SERVER_URL.isBlank()) {
            return@withContext Result.failure(LicenceRejected("Serveur d'activation non configuré."))
        }
        try {
            val conn = (URL("${LicenceConfig.SERVER_URL}/$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 8_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().put("key", key).put("deviceId", deviceId()).toString()
            conn.outputStream.use { it.write(payload.toByteArray()) }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val status = runCatching { JSONObject(body).optString("status") }.getOrDefault("")
            if (status == "ok") Result.success(Unit)
            else Result.failure(LicenceRejected("Cette licence n'est pas valide."))
        } catch (e: IOException) {
            Result.failure(NetworkUnavailable())
        }
    }
}
