package com.echeo.app

import com.echeo.app.data.SerialKey
import org.junit.Assert.assertEquals
import org.junit.Test

/** Forge Licence reconnaît le téléphone à ce code : le calcul ne doit pas changer. */
class SerialKeyTest {
    @Test
    fun code_du_telephone_de_reference() {
        assertEquals("7RFV-JX2C", SerialKey.deviceCode("abcdef0123456789", "echeo"))
    }

    @Test
    fun code_dependant_de_l_app() {
        assertEquals(9, SerialKey.deviceCode("abcdef0123456789", "autre").length)
    }
}
