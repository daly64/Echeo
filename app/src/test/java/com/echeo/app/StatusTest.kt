package com.echeo.app

import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.initialsOf
import com.echeo.app.domain.relativeLabel
import com.echeo.app.domain.statusOf
import com.echeo.app.ui.clients.ClientFilter
import com.echeo.app.ui.clients.filterClients
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusTest {
    @Test
    fun etats() {
        assertEquals(SubStatus.EXPIRED, statusOf(expiryDay = 9, today = 10, warnDays = 7))
        assertEquals(SubStatus.SOON, statusOf(10, 10, 7))
        assertEquals(SubStatus.SOON, statusOf(17, 10, 7))
        assertEquals(SubStatus.ACTIVE, statusOf(18, 10, 7))
    }

    @Test
    fun libelles() {
        assertEquals("expiré hier", relativeLabel(9, 10))
        assertEquals("expiré il y a 4 jours", relativeLabel(6, 10))
        assertEquals("expire aujourd'hui", relativeLabel(10, 10))
        assertEquals("demain", relativeLabel(11, 10))
        assertEquals("dans 3 jours", relativeLabel(13, 10))
    }

    @Test
    fun initiales() {
        assertEquals("KB", initialsOf("Karim B."))
        assertEquals("AM", initialsOf("amine"))
        assertEquals("?", initialsOf("  "))
    }

    @Test
    fun recherche() {
        val list = listOf(
            ClientSummary(1, "Karim B.", "+216 20 123 456", "KB", 1, SubStatus.SOON, null, null, null),
            ClientSummary(2, "Sami M.", "+216 22 456 789", "SM", 1, SubStatus.EXPIRED, null, null, null),
        )
        assertEquals(listOf(1L), filterClients(list, "kar", ClientFilter.All).map { it.id })
        assertEquals(listOf(2L), filterClients(list, "22 456", ClientFilter.All).map { it.id })
        assertEquals(listOf(2L), filterClients(list, "", ClientFilter.Expired).map { it.id })
    }
}
