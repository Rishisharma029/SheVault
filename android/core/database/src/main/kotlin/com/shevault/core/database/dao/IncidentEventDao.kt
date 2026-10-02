package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shevault.core.database.entity.IncidentEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IncidentEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<IncidentEventEntity>)

    @Query("SELECT * FROM incident_events WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getEventsForIncident(incidentId: String): Flow<List<IncidentEventEntity>>

    @Query("SELECT * FROM incident_events WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    suspend fun getEventsForIncidentSync(incidentId: String): List<IncidentEventEntity>

    @Query("SELECT * FROM incident_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IncidentEventEntity>>

    @Query("SELECT * FROM incident_events WHERE incidentId = :incidentId AND type = :type ORDER BY timestamp DESC")
    suspend fun getEventsByType(incidentId: String, type: String): List<IncidentEventEntity>
}
