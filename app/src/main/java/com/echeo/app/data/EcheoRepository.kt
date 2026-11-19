package com.echeo.app.data

import androidx.room.withTransaction
import com.echeo.app.domain.addMonths

/** Toutes les écritures de la base, regroupées au même endroit. */
class EcheoRepository(private val db: EcheoDatabase) {

    suspend fun addClientWithSubscription(name: String, phone: String, typeId: Long, months: Int, startDay: Long): Long =
        db.withTransaction {
            val clientId = db.clientDao().insert(ClientEntity(name = name.trim(), phone = phone.trim()))
            db.subscriptionDao().insert(newSubscription(clientId, typeId, months, startDay))
            clientId
        }

    suspend fun addSubscription(clientId: Long, typeId: Long, months: Int, startDay: Long) {
        db.subscriptionDao().insert(newSubscription(clientId, typeId, months, startDay))
    }

    /** Prolonge un abonnement et garde une trace dans l'historique. */
    suspend fun renew(sub: SubscriptionEntity, newExpiryDay: Long, amount: Double?) {
        db.withTransaction {
            db.renewalDao().insert(
                RenewalEntity(
                    subscriptionId = sub.id,
                    previousExpiryDay = sub.expiryDay,
                    newExpiryDay = newExpiryDay,
                    amount = amount,
                ),
            )
            db.subscriptionDao().update(sub.copy(expiryDay = newExpiryDay))
        }
    }

    suspend fun subscriptionById(id: Long): SubscriptionEntity? = db.subscriptionDao().getById(id)

    suspend fun markContacted(subId: Long, at: Long = System.currentTimeMillis()) {
        val sub = db.subscriptionDao().getById(subId) ?: return
        db.subscriptionDao().update(sub.copy(lastContactAt = at))
    }

    suspend fun updateClient(client: ClientEntity) = db.clientDao().update(client)

    /** Supprime le client ; ses abonnements et leur historique partent avec lui. */
    suspend fun deleteClient(client: ClientEntity) = db.clientDao().delete(client)

    suspend fun saveType(type: SubscriptionTypeEntity) {
        if (type.id == 0L) db.subscriptionTypeDao().insert(type) else db.subscriptionTypeDao().update(type)
    }

    suspend fun snapshot(): Snapshot = Snapshot(
        types = db.subscriptionTypeDao().getAll(),
        clients = db.clientDao().getAll(),
        subs = db.subscriptionDao().getAll(),
        renewals = db.renewalDao().getAll(),
    )

    /** Remplace toutes les données par celles de la sauvegarde, en une seule transaction. */
    suspend fun restore(s: Snapshot) {
        db.withTransaction {
            db.renewalDao().deleteAll()
            db.subscriptionDao().deleteAll()
            db.clientDao().deleteAll()
            db.subscriptionTypeDao().deleteAll()
            s.types.forEach { db.subscriptionTypeDao().insert(it) }
            s.clients.forEach { db.clientDao().insert(it) }
            s.subs.forEach { db.subscriptionDao().insert(it) }
            s.renewals.forEach { db.renewalDao().insert(it) }
        }
    }

    private fun newSubscription(clientId: Long, typeId: Long, months: Int, startDay: Long) = SubscriptionEntity(
        clientId = clientId,
        typeId = typeId,
        durationMonths = months,
        startDay = startDay,
        expiryDay = addMonths(startDay, months),
    )
}
