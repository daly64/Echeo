package com.echeo.app

import com.echeo.app.data.SerialKey
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Les valeurs attendues viennent d'un calcul de référence indépendant (Python) : Echeo et Forge Licence doivent les retrouver. */
class SerialKeyTest {
    private val secret = ByteArray(32) { it.toByte() }
    private val device = "7RFV-JX2C"
    private val key = "051GR-YB6WG-XQG2Z-GH6KP"
    private val day = LocalDate.of(2026, 11, 20)

    @Test
    fun code_du_telephone_de_reference() {
        assertEquals(device, SerialKey.deviceCode("abcdef0123456789", "echeo"))
    }

    @Test
    fun cle_de_reference() {
        assertEquals(key, SerialKey.issue(secret, "echeo", device, day, 12))
    }

    @Test
    fun lecture_de_la_cle_de_reference() {
        val licence = SerialKey.read(secret, "echeo", device, key)
        assertNotNull(licence)
        assertEquals(day, licence!!.issued)
        assertEquals(day.plusMonths(12), licence.expiry)
    }

    @Test
    fun saisie_tolerante() {
        assertNotNull(SerialKey.read(secret, "echeo", device, key.lowercase().replace("-", " ")))
        assertNotNull(SerialKey.read(secret, "echeo", "7rfv jx2c", key.replace("051GR", "O51GR")))
    }

    @Test
    fun cle_refusee_pour_un_autre_telephone_une_autre_app_ou_un_autre_secret() {
        assertNull(SerialKey.read(secret, "echeo", "AAAA-AAAA", key))
        assertNull(SerialKey.read(secret, "autre", device, key))
        assertNull(SerialKey.read(ByteArray(32) { 1 }, "echeo", device, key))
    }

    @Test
    fun cle_modifiee_ou_incomplete_refusee() {
        assertNull(SerialKey.read(secret, "echeo", device, "051GR-YB6WG-XQG2Z-GH6KQ"))
        assertNull(SerialKey.read(secret, "echeo", device, "051GR-YB6WG-XQG2Z"))
        assertNull(SerialKey.read(secret, "echeo", device, ""))
    }

    @Test
    fun duree_et_date_respectees() {
        val k = SerialKey.issue(secret, "echeo", device, LocalDate.of(2027, 1, 31), 1)
        val licence = SerialKey.read(secret, "echeo", device, k)!!
        assertEquals(LocalDate.of(2027, 2, 28), licence.expiry)
    }
}
