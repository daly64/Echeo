package com.echeo.app.domain

import com.echeo.app.data.ClientEntity
import com.echeo.app.data.SubscriptionEntity
import com.echeo.app.data.SubscriptionTypeEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** État d'un abonnement ou d'un client, du plus urgent au moins urgent. */
enum class SubStatus { EXPIRED, SOON, ACTIVE }

fun statusOf(expiryDay: Long, today: Long, warnDays: Int): SubStatus {
    val left = expiryDay - today
    return when {
        left < 0 -> SubStatus.EXPIRED
        left <= warnDays -> SubStatus.SOON
        else -> SubStatus.ACTIVE
    }
}

/** « expiré hier », « expire aujourd'hui », « dans 3 jours »… */
fun relativeLabel(expiryDay: Long, today: Long): String {
    val left = expiryDay - today
    return when {
        left < -1 -> "expiré il y a ${-left} jours"
        left == -1L -> "expiré hier"
        left == 0L -> "expire aujourd'hui"
        left == 1L -> "demain"
        else -> "dans $left jours"
    }
}

/** Ajoute des mois à une date (en jours depuis 1970). */
fun addMonths(day: Long, months: Int): Long = LocalDate.ofEpochDay(day).plusMonths(months.toLong()).toEpochDay()

fun durationLabel(months: Int): String = "$months mois"

fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

fun abonnementsLabel(count: Int): String = if (count <= 1) "$count abonnement" else "$count abonnements"

fun clientsLabel(count: Int): String = if (count <= 1) "$count client" else "$count clients"

private val DAY_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH)
private val DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.FRENCH)
private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.FRENCH)

fun formatDay(day: Long): String = LocalDate.ofEpochDay(day).format(DAY_FORMAT)
fun formatDateTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMAT)
fun formatTime(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(TIME_FORMAT)
fun todayEpoch(): Long = LocalDate.now().toEpochDay()
fun dayOf(ms: Long): Long = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()

/** Résumé d'un client pour les listes : son abonnement le plus urgent et son état. */
data class ClientSummary(
    val id: Long,
    val name: String,
    val phone: String,
    val initials: String,
    val subscriptionCount: Int,
    /** null si le client n'a aucun abonnement. */
    val status: SubStatus?,
    /** Abonnement qui expire le plus tôt. */
    val nextSubscription: String?,
    val nextExpiryDay: Long?,
    val nextLabel: String?,
)

/** Compteurs du tableau de bord : nombre de clients par état. */
data class DashboardCounts(val active: Int = 0, val soon: Int = 0, val expired: Int = 0)

fun List<ClientSummary>.counts(): DashboardCounts = DashboardCounts(
    active = count { it.status == SubStatus.ACTIVE },
    soon = count { it.status == SubStatus.SOON },
    expired = count { it.status == SubStatus.EXPIRED },
)

/** Un abonnement, prêt à afficher : nom du client, type, dates et état. */
data class SubView(
    val id: Long,
    val clientId: Long,
    val clientName: String,
    val clientPhone: String,
    val typeId: Long,
    val typeName: String,
    val typeIcon: String,
    val typeColor: Long,
    val durationMonths: Int,
    val startDay: Long,
    val expiryDay: Long,
    val status: SubStatus,
    val lastContactAt: Long?,
    val relative: String,
) {
    val label: String get() = "$typeName ${durationLabel(durationMonths)}"
}

fun buildSubViews(
    clients: List<ClientEntity>,
    subs: List<SubscriptionEntity>,
    types: List<SubscriptionTypeEntity>,
    today: Long,
    warnDays: Int,
): List<SubView> {
    val clientById = clients.associateBy { it.id }
    val typeById = types.associateBy { it.id }
    return subs.mapNotNull { s ->
        val c = clientById[s.clientId] ?: return@mapNotNull null
        val t = typeById[s.typeId]
        SubView(
            id = s.id,
            clientId = c.id,
            clientName = c.name,
            clientPhone = c.phone,
            typeId = s.typeId,
            typeName = t?.name ?: "Abonnement",
            typeIcon = t?.icon ?: "?",
            typeColor = t?.color ?: 0xFF5B6B80,
            durationMonths = s.durationMonths,
            startDay = s.startDay,
            expiryDay = s.expiryDay,
            status = statusOf(s.expiryDay, today, warnDays),
            lastContactAt = s.lastContactAt,
            relative = relativeLabel(s.expiryDay, today),
        )
    }
}

fun clientSummaries(clients: List<ClientEntity>, views: List<SubView>): List<ClientSummary> {
    val byClient = views.groupBy { it.clientId }
    return clients.map { c ->
        val list = byClient[c.id].orEmpty()
        val next = list.minByOrNull { it.expiryDay }
        ClientSummary(
            id = c.id,
            name = c.name,
            phone = c.phone,
            initials = initialsOf(c.name),
            subscriptionCount = list.size,
            status = next?.status,
            nextSubscription = next?.label,
            nextExpiryDay = next?.expiryDay,
            nextLabel = next?.relative,
        )
    }
}
