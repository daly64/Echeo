package com.echeo.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients")
    suspend fun getAll(): List<ClientEntity>

    @Insert
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)

    @Query("DELETE FROM clients")
    suspend fun deleteAll()
}

@Dao
interface SubscriptionTypeDao {
    @Query("SELECT * FROM subscription_types ORDER BY sort_order, name COLLATE NOCASE")
    fun observeAll(): Flow<List<SubscriptionTypeEntity>>

    @Query("SELECT * FROM subscription_types")
    suspend fun getAll(): List<SubscriptionTypeEntity>

    @Insert
    suspend fun insert(type: SubscriptionTypeEntity): Long

    @Update
    suspend fun update(type: SubscriptionTypeEntity)

    @Query("DELETE FROM subscription_types")
    suspend fun deleteAll()
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions")
    suspend fun getAll(): List<SubscriptionEntity>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Long): SubscriptionEntity?

    @Insert
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()
}

@Dao
interface RenewalDao {
    @Query("SELECT * FROM renewals")
    suspend fun getAll(): List<RenewalEntity>

    @Insert
    suspend fun insert(renewal: RenewalEntity): Long

    @Query("DELETE FROM renewals")
    suspend fun deleteAll()

    @Query("SELECT * FROM renewals WHERE subscription_id = :subscriptionId ORDER BY renewed_at DESC")
    fun observeForSubscription(subscriptionId: Long): Flow<List<RenewalEntity>>
}
