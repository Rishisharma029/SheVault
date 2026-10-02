package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shevault.core.database.entity.DeliveryAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryAttemptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: DeliveryAttemptEntity)

    @Update
    suspend fun updateAttempt(attempt: DeliveryAttemptEntity)

    @Query("SELECT * FROM delivery_attempts WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getAttemptsForIncident(incidentId: String): Flow<List<DeliveryAttemptEntity>>

    @Query("SELECT * FROM delivery_attempts WHERE incidentId = :incidentId AND status = :status ORDER BY timestamp ASC")
    suspend fun getAttemptsByStatus(incidentId: String, status: String): List<DeliveryAttemptEntity>

    @Query("SELECT * FROM delivery_attempts WHERE status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingAttempts(): List<DeliveryAttemptEntity>

    @Query("UPDATE delivery_attempts SET status = :status, error = :error, attemptCount = attemptCount + 1, timestamp = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, error: String?, timestamp: Long)
}
