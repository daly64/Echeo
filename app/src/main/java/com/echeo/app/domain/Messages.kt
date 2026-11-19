package com.echeo.app.domain

fun fillTemplate(template: String, name: String, subscription: String, date: String, days: Long): String =
    template
        .replace("{nom}", name)
        .replace("{abonnement}", subscription)
        .replace("{date}", date)
        .replace("{jours}", days.toString())

fun phoneDigits(phone: String): String = phone.filter { it.isDigit() }

/** Texte du message de relance, choisi selon que l'abonnement est expiré ou bientôt expiré. */
fun reminderText(view: SubView, today: Long, before: String, after: String): String {
    val template = if (view.status == SubStatus.EXPIRED) after else before
    return fillTemplate(template, view.clientName, view.label, formatDay(view.expiryDay), view.expiryDay - today)
}

fun contactLabel(lastContactAt: Long?, now: Long = System.currentTimeMillis()): String {
    if (lastContactAt == null) return "À relancer · jamais contacté"
    val days = dayOf(now) - dayOf(lastContactAt)
    return when (days) {
        0L -> "Relancé aujourd'hui à ${formatTime(lastContactAt)}"
        1L -> "Relancé hier"
        else -> "Relancé il y a $days jours"
    }
}
