package com.echeo.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de données locale d'Echeo. Tout reste sur le téléphone.
 *
 * Mises à jour : à chaque changement de structure, on augmente [version] et on ajoute
 * une migration (AutoMigration ou Migration). Jamais de suppression des données.
 */
@Database(
    entities = [
        ClientEntity::class,
        SubscriptionTypeEntity::class,
        SubscriptionEntity::class,
        RenewalEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class EcheoDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun subscriptionTypeDao(): SubscriptionTypeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun renewalDao(): RenewalDao

    companion object {
        fun build(context: Context): EcheoDatabase =
            Room.databaseBuilder(context, EcheoDatabase::class.java, "echeo.db")
                .addCallback(SeedDefaultTypes)
                .build()
    }
}

/** Liste par défaut des types d'abonnement, créée au premier lancement. */
internal val DEFAULT_TYPES: List<SubscriptionTypeEntity> = listOf(
    SubscriptionTypeEntity(name = "Netflix", durations = "1,3,12", icon = "N", color = 0xFFE50914),
    SubscriptionTypeEntity(name = "IPTV", durations = "1,3,6,12", icon = "TV", color = 0xFF0F2A4A),
    SubscriptionTypeEntity(name = "Alpha", durations = "3,12", icon = "A", color = 0xFF19B5A5),
    SubscriptionTypeEntity(name = "Disney Plus", icon = "D", color = 0xFF113CCF),
    SubscriptionTypeEntity(name = "Prime Video", icon = "P", color = 0xFF00A8E1),
    SubscriptionTypeEntity(name = "Spotify", icon = "S", color = 0xFF1DB954),
    SubscriptionTypeEntity(name = "Canva", icon = "C", color = 0xFF7D2AE8),
    SubscriptionTypeEntity(name = "YouTube Premium", icon = "Y", color = 0xFFFF0033),
    SubscriptionTypeEntity(name = "ChatGPT", icon = "G", color = 0xFF10A37F),
    SubscriptionTypeEntity(name = "Claude", icon = "C", color = 0xFFD97757),
    SubscriptionTypeEntity(name = "Personnalisé", icon = "+", color = 0xFF5B6B80, isCustom = true),
).mapIndexed { index, type -> type.copy(sortOrder = index) }

private object SeedDefaultTypes : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        DEFAULT_TYPES.forEach { type ->
            val values = ContentValues().apply {
                put("name", type.name)
                put("durations", type.durations)
                putNull("price")
                put("icon", type.icon)
                put("color", type.color)
                put("enabled", 1)
                put("is_custom", if (type.isCustom) 1 else 0)
                put("sort_order", type.sortOrder)
            }
            db.insert("subscription_types", SQLiteDatabase.CONFLICT_IGNORE, values)
        }
    }
}
