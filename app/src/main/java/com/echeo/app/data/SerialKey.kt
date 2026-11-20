package com.echeo.app.data

import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Clé lue et vérifiée : émise le « issued », valable jusqu'au « expiry » inclus. */
data class SerialLicence(val issued: LocalDate, val expiry: LocalDate)

/**
 * Clé d'activation au style numéro de série : 20 caractères en 4 blocs de 5 (ex. K7QM2-X9TRA-4WNBH-6ZPE3).
 * Même code dans Echeo (vérification) et Forge Licence (génération) : à garder identique.
 *
 * Contenu (100 bits) : date d'émission (16 bits, en jours depuis le 01/01/2026) + durée en mois (8 bits)
 * + signature de contrôle (76 bits) calculée avec un secret partagé, liée à l'app et au code du téléphone.
 * Alphabet sans I, L, O, U : les erreurs de lecture (O/0, I/L/1) sont corrigées à la saisie.
 */
object SerialKey {
    private const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val KEY_CHARS = 20
    private const val DEVICE_CHARS = 8
    private const val TAG_BITS = 76
    private val BASE_DAY = LocalDate.of(2026, 1, 1)

    /** Majuscules, sans séparateurs, avec les caractères ambigus corrigés (O→0, I/L→1). */
    fun normalize(text: String): String = buildString {
        for (c in text.uppercase()) {
            when {
                c == 'O' -> append('0')
                c == 'I' || c == 'L' -> append('1')
                c.isLetterOrDigit() -> append(c)
            }
        }
    }

    /** Remet un texte en blocs de « size » caractères séparés par des tirets (pour l'affichage). */
    fun group(text: String, size: Int): String = text.chunked(size).joinToString("-")

    /** Code du téléphone affiché à l'utilisateur : 8 caractères en 2 blocs (ex. A7K2-9QXM). */
    fun deviceCode(androidId: String, app: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest("$app:$androidId".toByteArray(Charsets.UTF_8))
        return group(encode(BigInteger(1, hash.copyOf(5)), DEVICE_CHARS), 4)
    }

    /** Émet une clé pour un téléphone. Durée : 1 à 255 mois. */
    fun issue(secret: ByteArray, app: String, device: String, issued: LocalDate, months: Int): String {
        require(months in 1..255) { "Durée de 1 à 255 mois." }
        val days = ChronoUnit.DAYS.between(BASE_DAY, issued)
        require(days in 0..65535) { "Date d'émission hors limites." }
        val data = (days.toInt() shl 8) or months
        val tag = tag(secret, app, normalize(device), data)
        val value = BigInteger.valueOf(data.toLong()).shiftLeft(TAG_BITS).or(tag)
        return group(encode(value, KEY_CHARS), 5)
    }

    /** Lit une clé : renvoie null si elle est mal écrite, ou pas faite pour cette app, ce téléphone et ce secret. */
    fun read(secret: ByteArray, app: String, device: String, key: String): SerialLicence? {
        val clean = normalize(key)
        if (clean.length != KEY_CHARS || clean.any { ALPHABET.indexOf(it) < 0 }) return null
        var value = BigInteger.ZERO
        for (c in clean) value = value.shiftLeft(5).or(BigInteger.valueOf(ALPHABET.indexOf(c).toLong()))
        val data = value.shiftRight(TAG_BITS).toInt()
        val given = value.and(BigInteger.ONE.shiftLeft(TAG_BITS).subtract(BigInteger.ONE))
        val expected = tag(secret, app, normalize(device), data)
        if (!MessageDigest.isEqual(given.toByteArray(), expected.toByteArray())) return null
        val issued = BASE_DAY.plusDays((data shr 8).toLong())
        return SerialLicence(issued, issued.plusMonths((data and 0xFF).toLong()))
    }

    private fun tag(secret: ByteArray, app: String, device: String, data: Int): BigInteger {
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(secret, "HmacSHA256")) }
        mac.update("$app|$device|".toByteArray(Charsets.UTF_8))
        mac.update(byteArrayOf((data shr 16).toByte(), (data shr 8).toByte(), data.toByte()))
        return BigInteger(1, mac.doFinal().copyOf(10)).shiftRight(80 - TAG_BITS)
    }

    private fun encode(value: BigInteger, chars: Int): String = buildString {
        for (i in chars - 1 downTo 0) append(ALPHABET[value.shiftRight(5 * i).and(BigInteger.valueOf(31)).toInt()])
    }
}
