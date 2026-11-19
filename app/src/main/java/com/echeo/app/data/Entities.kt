package com.echeo.app.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Un client de la boutique : nom et téléphone. */
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Type d'abonnement (Netflix, IPTV, Alpha…). Les durées proposées sont stockées
 * en mois séparés par des virgules, par exemple "1,3,6,12".
 */
@Entity(tableName = "subscription_types")
data class SubscriptionTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val durations: String = "1,3,6,12",
    /** Prix facultatif, en dinars. */
    val price: Double? = null,
    /** Lettre ou symbole affiché comme icône. */
    val icon: String,
    /** Couleur ARGB de l'icône. */
    val color: Long,
    val enabled: Boolean = true,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
) {
    val durationList: List<Int>
        get() = durations.split(",").mapNotNull { it.trim().toIntOrNull() }
}

/** Un abonnement d'un client, avec sa propre date d'expiration (en jours depuis 1970). */
@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SubscriptionTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["type_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("client_id"), Index("type_id"), Index("expiry_day")],
)
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "client_id") val clientId: Long,
    @ColumnInfo(name = "type_id") val typeId: Long,
    @ColumnInfo(name = "duration_months") val durationMonths: Int,
    @ColumnInfo(name = "start_day") val startDay: Long,
    @ColumnInfo(name = "expiry_day") val expiryDay: Long,
    /** Dernière relance (appel ou WhatsApp), en millisecondes. */
    @ColumnInfo(name = "last_contact_at") val lastContactAt: Long? = null,
)

/** Historique : chaque renouvellement prolonge la date d'expiration. */
@Entity(
    tableName = "renewals",
    foreignKeys = [
        ForeignKey(
            entity = SubscriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["subscription_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("subscription_id")],
)
data class RenewalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subscription_id") val subscriptionId: Long,
    @ColumnInfo(name = "renewed_at") val renewedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "previous_expiry_day") val previousExpiryDay: Long,
    @ColumnInfo(name = "new_expiry_day") val newExpiryDay: Long,
    /** Montant payé, facultatif. */
    val amount: Double? = null,
)

data class SubscriptionWithType(
    @Embedded val subscription: SubscriptionEntity,
    @Relation(parentColumn = "type_id", entityColumn = "id")
    val type: SubscriptionTypeEntity,
)

data class ClientWithSubscriptions(
    @Embedded val client: ClientEntity,
    @Relation(
        entity = SubscriptionEntity::class,
        parentColumn = "id",
        entityColumn = "client_id",
    )
    val subscriptions: List<SubscriptionWithType>,
)
