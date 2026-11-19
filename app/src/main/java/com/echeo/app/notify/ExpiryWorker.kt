package com.echeo.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker.Result
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.echeo.app.EcheoApp
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/** Vérifie chaque jour à l'heure choisie et envoie les notifications. */
class ExpiryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as EcheoApp
        if (app.settings.notifyEnabled.value) ExpiryNotifier.run(app)
        return Result.success()
    }

    companion object {
        fun schedule(context: Context, hour: Int) {
            val now = ZonedDateTime.now()
            var next = now.withHour(hour).withMinute(0).withSecond(0).withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            val delay = Duration.between(now, next).toMillis()
            val request = PeriodicWorkRequestBuilder<ExpiryWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("expiry", ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}
