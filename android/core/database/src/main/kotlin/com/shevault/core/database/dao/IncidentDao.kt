package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shevault.core.database.entity.IncidentEntity
import com.shevault.core.database.entity.IncidentSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): IncidentEntity?

    @Query("SELECT * FROM incidents WHERE id = :id")
    fun getIncidentFlow(id: String): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE status IN ('ACTIVE', 'ESCALATING') ORDER BY createdAt DESC")
    fun getActiveIncidents(): Flow<List<IncidentEntity>>

    @Query("UPDATE incidents SET lastKnownLatitude = :lat, lastKnownLongitude = :lon, lastKnownAccuracy = :acc, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateLastKnownLocation(id: String, lat: Double, lon: Double, acc: Float, timestamp: Long)

    @Query("UPDATE incidents SET status = :status, cancelType = :cancelType, batteryAtEnd = :batteryAtEnd, updatedAt = :timestamp WHERE id = :id")
    suspend fun resolveIncident(id: String, status: String, cancelType: String?, batteryAtEnd: Int?, timestamp: Long)

    // Legacy incident_sessions backward compatibility methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: IncidentSessionEntity)

    @Query("SELECT * FROM incident_sessions WHERE incidentId = :incidentId")
    suspend fun getSessionById(incidentId: String): IncidentSessionEntity?

    @Query("SELECT * FROM incident_sessions ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestSession(): IncidentSessionEntity?

    @Query("SELECT * FROM incident_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<IncidentSessionEntity>>
}
