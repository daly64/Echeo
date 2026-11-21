package com.echeo.app.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.echeo.app.EcheoApp
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/** Règles des fichiers de sauvegarde automatique : nom daté, et seulement les dernières copies. */
object BackupFiles {
    /** Nombre de copies gardées : les plus anciennes sont supprimées pour ne pas remplir la mémoire. */
    const val KEEP = 7
    const val FOLDER = "Echeo"
    private const val PREFIX = "echeo-sauvegarde-"
    private const val SUFFIX = ".json"

    /** Exemple : echeo-sauvegarde-2026-11-21.json (le nom se trie dans l'ordre des dates). */
    fun fileName(day: LocalDate): String = "$PREFIX$day$SUFFIX"

    fun isBackupName(name: String): Boolean = name.startsWith(PREFIX) && name.endsWith(SUFFIX)

    /** Noms à supprimer pour ne garder que les [keep] copies les plus récentes. */
    fun namesToDelete(names: List<String>, keep: Int = KEEP): List<String> =
        names.filter(::isBackupName).sortedDescending().drop(keep)

    /** Vrai si la dernière sauvegarde automatique n'a pas été faite aujourd'hui. */
    fun isDue(lastAutoAt: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Boolean =
        lastAutoAt == 0L || Instant.ofEpochMilli(lastAutoAt).atZone(zone).toLocalDate() != today
}

/**
 * Écrit la sauvegarde dans le dossier Documents/Echeo du téléphone (Android 10 et plus), sinon dans le
 * dossier de l'application. Le fichier reste donc sur le téléphone, même si l'app est mise à jour.
 */
class BackupStore(private val context: Context) {

    /** Dossier affiché au gérant. */
    fun locationLabel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "Documents/${BackupFiles.FOLDER}"
        else "Android/data/${context.packageName}/files/${BackupFiles.FOLDER}"

    /** Écrit la copie du jour (remplace celle du même jour) puis supprime les plus anciennes. */
    fun save(json: String, day: LocalDate): Boolean {
        val name = BackupFiles.fileName(day)
        val written = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveInDocuments(json, name) else saveInAppFolder(json, name)
        }.getOrDefault(false)
        if (written) runCatching { rotate() }
        return written
    }

    // --- Android 10 et plus : MediaStore, sans permission ---

    @get:RequiresApi(Build.VERSION_CODES.Q)
    private val collection get() = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    private val relativePath get() = "Documents/${BackupFiles.FOLDER}/"

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveInDocuments(json: String, name: String): Boolean {
        val resolver = context.contentResolver
        ownFiles().filter { it.second == name }.forEach { resolver.delete(it.first, null, null) }
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: return false
        return try {
            val stream = resolver.openOutputStream(uri, "wt") ?: error("Écriture impossible")
            stream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    /** Fichiers de sauvegarde créés par cette app dans Documents/Echeo : (adresse, nom). */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ownFiles(): List<Pair<android.net.Uri, String>> {
        val result = mutableListOf<Pair<android.net.Uri, String>>()
        context.contentResolver.query(
            collection,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
            "${MediaStore.MediaColumns.RELATIVE_PATH} = ?",
            arrayOf(relativePath),
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(1)
                if (BackupFiles.isBackupName(name)) result.add(ContentUris.withAppendedId(collection, cursor.getLong(0)) to name)
            }
        }
        return result
    }

    // --- Android 8 et 9 : dossier de l'application ---

    private fun appFolder(): File =
        File(context.getExternalFilesDir(null) ?: context.filesDir, BackupFiles.FOLDER).also { it.mkdirs() }

    private fun saveInAppFolder(json: String, name: String): Boolean {
        File(appFolder(), name).writeText(json, Charsets.UTF_8)
        return true
    }

    private fun rotate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val files = ownFiles()
            BackupFiles.namesToDelete(files.map { it.second }).forEach { old ->
                files.filter { it.second == old }.forEach { runCatching { context.contentResolver.delete(it.first, null, null) } }
            }
        } else {
            val files = appFolder().listFiles().orEmpty()
            BackupFiles.namesToDelete(files.map { it.name }).forEach { old -> files.firstOrNull { it.name == old }?.delete() }
        }
    }
}

/** Sauvegarde automatique : une copie par jour, tant qu'elle est activée dans Sauvegarde. */
object AutoBackup {
    /** Fait la sauvegarde. Renvoie faux seulement si l'écriture a échoué. */
    suspend fun run(app: EcheoApp): Boolean {
        val snapshot = app.repository.snapshot()
        // Aucune donnée (app neuve) : on ne crée rien, pour ne pas pousser dehors de bonnes copies.
        if (snapshot.clients.isEmpty()) return true
        val ok = BackupStore(app).save(BackupCodec.encode(snapshot), LocalDate.now())
        if (ok) app.settings.setLastAutoBackup(System.currentTimeMillis())
        return ok
    }
}

/** Travail de fond : une sauvegarde chaque nuit, même si l'app n'est pas ouverte. */
class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as EcheoApp
        if (!app.settings.autoBackupEnabled.value) return Result.success()
        if (AutoBackup.run(app)) return Result.success()
        return if (runAttemptCount < 3) Result.retry() else Result.success()
    }

    companion object {
        private const val NIGHTLY = "auto-backup"
        private const val CATCH_UP = "auto-backup-now"
        private const val HOUR = 3

        /** Planifie la sauvegarde de chaque nuit à 3 h, puis rattrape tout de suite un jour manqué. */
        fun schedule(context: Context, settings: Settings) {
            val now = ZonedDateTime.now()
            var next = now.withHour(HOUR).withMinute(0).withSecond(0).withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(NIGHTLY, ExistingPeriodicWorkPolicy.KEEP, request)
            catchUp(context, settings)
        }

        /** Lance une sauvegarde maintenant si celle d'aujourd'hui n'a pas encore été faite (téléphone éteint la nuit, par exemple). */
        fun catchUp(context: Context, settings: Settings) {
            if (!settings.autoBackupEnabled.value) return
            if (!BackupFiles.isDue(settings.lastAutoBackupAt.value, LocalDate.now())) return
            WorkManager.getInstance(context)
                .enqueueUniqueWork(CATCH_UP, ExistingWorkPolicy.KEEP, OneTimeWorkRequestBuilder<AutoBackupWorker>().build())
        }
    }
}
