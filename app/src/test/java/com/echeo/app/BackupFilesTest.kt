package com.echeo.app

import com.echeo.app.data.BackupFiles
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupFilesTest {

    @Test
    fun le_nom_contient_la_date() {
        assertEquals("echeo-sauvegarde-2026-11-21.json", BackupFiles.fileName(LocalDate.of(2026, 11, 21)))
    }

    @Test
    fun on_garde_les_sept_dernieres_copies() {
        val names = (1..10).map { BackupFiles.fileName(LocalDate.of(2026, 11, it)) }.shuffled()
        val toDelete = BackupFiles.namesToDelete(names)
        assertEquals(
            listOf(
                BackupFiles.fileName(LocalDate.of(2026, 11, 3)),
                BackupFiles.fileName(LocalDate.of(2026, 11, 2)),
                BackupFiles.fileName(LocalDate.of(2026, 11, 1)),
            ),
            toDelete,
        )
    }

    @Test
    fun moins_de_sept_copies_rien_a_supprimer() {
        val names = (1..5).map { BackupFiles.fileName(LocalDate.of(2026, 11, it)) }
        assertTrue(BackupFiles.namesToDelete(names).isEmpty())
    }

    @Test
    fun les_autres_fichiers_ne_sont_jamais_touches() {
        val names = listOf("photo.jpg", "notes.json") + (1..9).map { BackupFiles.fileName(LocalDate.of(2026, 11, it)) }
        val toDelete = BackupFiles.namesToDelete(names)
        assertEquals(2, toDelete.size)
        assertTrue(toDelete.all(BackupFiles::isBackupName))
    }

    @Test
    fun une_sauvegarde_par_jour() {
        val zone = ZoneId.of("Africa/Tunis")
        val today = LocalDate.of(2026, 11, 21)
        val earlier = today.minusDays(1).atTime(23, 0).atZone(zone).toInstant().toEpochMilli()
        val sameDay = today.atTime(3, 0).atZone(zone).toInstant().toEpochMilli()
        assertTrue(BackupFiles.isDue(0L, today, zone))
        assertTrue(BackupFiles.isDue(earlier, today, zone))
        assertFalse(BackupFiles.isDue(sameDay, today, zone))
    }
}
