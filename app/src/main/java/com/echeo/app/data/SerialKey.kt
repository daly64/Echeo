package com.echeo.app.data

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Code du téléphone affiché à l'utilisateur : 8 caractères en 2 blocs (ex. A7K2-9QXM).
 * Forge Licence reconnaît le téléphone à ce code : le calcul ne doit pas changer.
 * Alphabet sans I, L, O, U : les erreurs de lecture sont évitées.
 */
object SerialKey {
    private const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val DEVICE_CHARS = 8

    /** Remet un texte en blocs de « size » caractères séparés par des tirets (pour l'affichage). */
    fun group(text: String, size: Int): String = text.chunked(size).joinToString("-")

    fun deviceCode(androidId: String, app: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest("$app:$androidId".toByteArray(Charsets.UTF_8))
        return group(encode(BigInteger(1, hash.copyOf(5)), DEVICE_CHARS), 4)
    }

    private fun encode(value: BigInteger, chars: Int): String = buildString {
        for (i in chars - 1 downTo 0) append(ALPHABET[value.shiftRight(5 * i).and(BigInteger.valueOf(31)).toInt()])
    }
}
