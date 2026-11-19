package com.echeo.app.domain

import com.echeo.app.data.ClientWithSubscriptions
import com.echeo.app.data.SubscriptionWithType

/** État d'un abonnement ou d'un client, du plus urgent au moins urgent. */
enum class SubStatus { EXPIRED, SOON, ACTIVE }

/** État d'un abonnement selon sa date d'expiration et le seuil d'alerte (en jours). */
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

fun durationLabel(months: Int): String = "$months mois"

fun subscriptionLabel(sub: SubscriptionWithType): String =
    "${sub.type.name} ${durationLabel(sub.subscription.durationMonths)}"

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

fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase()
    }
}

fun ClientWithSubscriptions.summary(today: Long, warnDays: Int): ClientSummary {
    val next = subscriptions.minByOrNull { it.subscription.expiryDay }
    return ClientSummary(
        id = client.id,
        name = client.name,
        phone = client.phone,
        initials = initialsOf(client.name),
        subscriptionCount = subscriptions.size,
        status = next?.let { statusOf(it.subscription.expiryDay, today, warnDays) },
        nextSubscription = next?.let { subscriptionLabel(it) },
        nextExpiryDay = next?.subscription?.expiryDay,
        nextLabel = next?.let { relativeLabel(it.subscription.expiryDay, today) },
    )
}

/** Compteurs du tableau de bord : nombre de clients par état. */
data class DashboardCounts(val active: Int = 0, val soon: Int = 0, val expired: Int = 0)

fun List<ClientSummary>.counts(): DashboardCounts = DashboardCounts(
    active = count { it.status == SubStatus.ACTIVE },
    soon = count { it.status == SubStatus.SOON },
    expired = count { it.status == SubStatus.EXPIRED },
)

fun abonnementsLabel(count: Int): String =
    if (count <= 1) "$count abonnement" else "$count abonnements"

fun clientsLabel(count: Int): String =
    if (count <= 1) "$count client" else "$count clients"
