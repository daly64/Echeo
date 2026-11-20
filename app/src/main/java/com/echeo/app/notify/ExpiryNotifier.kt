package com.echeo.app.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.echeo.app.EcheoApp
import com.echeo.app.MainActivity
import com.echeo.app.R
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.buildSubViews
import com.echeo.app.domain.formatDay
import com.echeo.app.domain.phoneDigits
import java.time.LocalDate

/** Notifications d'expiration : une par client, avec les actions Appeler et WhatsApp. */
object ExpiryNotifier {
    const val CHANNEL = "expirations"
    private const val FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL, "Expirations", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Abonnements qui expirent bientôt"
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private const val LICENCE_NOTIFICATION_ID = 999_999
    private const val LICENCE_WARN_DAYS = 7

    private fun canNotify(app: EcheoApp): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(app, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Prévient le gérant 7 jours avant la fin de la licence, pour qu'il demande une nouvelle clé à temps. */
    @SuppressLint("MissingPermission")
    fun runLicence(app: EcheoApp) {
        if (!canNotify(app)) return
        val info = app.licence.info(app.settings.licenceKey.value) ?: return
        val left = info.expiry.toEpochDay() - LocalDate.now().toEpochDay()
        if (left < 0 || left > LICENCE_WARN_DAYS) return
        val title = if (left == 0L) "Votre licence Echeo expire aujourd'hui" else "Votre licence Echeo expire dans $left jours"
        val tap = PendingIntent.getActivity(app, LICENCE_NOTIFICATION_ID, Intent(app, MainActivity::class.java), FLAGS)
        val notification = NotificationCompat.Builder(app, CHANNEL)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle(title)
            .setContentText("Demandez votre nouvelle clé avant le ${formatDay(info.expiry.toEpochDay())}.")
            .setContentIntent(tap)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(app).notify(LICENCE_NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    suspend fun run(app: EcheoApp) {
        if (!canNotify(app)) return

        val db = app.database
        val today = LocalDate.now().toEpochDay()
        val views = buildSubViews(
            db.clientDao().getAll(),
            db.subscriptionDao().getAll(),
            db.subscriptionTypeDao().getAll(),
            today,
            app.settings.warnDays.value,
        )
        val manager = NotificationManagerCompat.from(app)
        views.filter { it.status != SubStatus.ACTIVE }
            .groupBy { it.clientId }
            .forEach { (clientId, list) ->
                val sub = list.minBy { it.expiryDay }
                val left = sub.expiryDay - today
                val title = when {
                    left < 0 -> "${sub.clientName} a expiré"
                    left == 0L -> "${sub.clientName} expire aujourd'hui"
                    else -> "${sub.clientName} expire dans $left jours"
                }
                val digits = phoneDigits(sub.clientPhone)
                val tap = PendingIntent.getActivity(app, clientId.toInt(), Intent(app, MainActivity::class.java), FLAGS)
                val call = PendingIntent.getActivity(
                    app, clientId.toInt() + 100_000,
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:+$digits")), FLAGS,
                )
                val wa = PendingIntent.getActivity(
                    app, clientId.toInt() + 200_000,
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")), FLAGS,
                )
                val notification = NotificationCompat.Builder(app, CHANNEL)
                    .setSmallIcon(R.drawable.ic_bell)
                    .setContentTitle(title)
                    .setContentText("${sub.label} · ${formatDay(sub.expiryDay)}")
                    .setContentIntent(tap)
                    .setAutoCancel(true)
                    .addAction(R.drawable.ic_phone, "Appeler", call)
                    .addAction(R.drawable.ic_chat, "WhatsApp", wa)
                    .build()
                manager.notify(clientId.toInt(), notification)
            }
    }
}
