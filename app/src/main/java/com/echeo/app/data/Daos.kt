package com.echeo.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Transaction
    @Query("SELECT * FROM clients ORDER BY name COLLATE NOCASE")
    fun observeAllWithSubscriptions(): Flow<List<ClientWithSubscriptions>>

    @Transaction
    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeWithSubscriptions(id: Long): Flow<ClientWithSubscriptions?>

    @Insert
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}

@Dao
interface SubscriptionTypeDao {
    @Query("SELECT * FROM subscription_types ORDER BY sort_order, name COLLATE NOCASE")
    fun observeAll(): Flow<List<SubscriptionTypeEntity>>

    @Insert
    suspend fun insert(type: SubscriptionTypeEntity): Long

    @Update
    suspend fun update(type: SubscriptionTypeEntity)
}

@Dao
interface SubscriptionDao {
    @Insert
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("SELECT COUNT(*) FROM subscriptions")
    fun observeCount(): Flow<Int>
}

@Dao
interface RenewalDao {
    @Insert
    suspend fun insert(renewal: RenewalEntity): Long

    @Query("SELECT * FROM renewals WHERE subscription_id = :subscriptionId ORDER BY renewed_at DESC")
    fun observeForSubscription(subscriptionId: Long): Flow<List<RenewalEntity>>
}
