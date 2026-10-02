package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shevault.core.database.entity.SensorSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorSampleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: SensorSampleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SensorSampleEntity>)

    @Query("SELECT * FROM sensor_samples WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getSamplesForIncident(incidentId: String): Flow<List<SensorSampleEntity>>

    @Query("SELECT * FROM sensor_samples WHERE incidentId = :incidentId AND sensorType = :sensorType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestSamplesByType(incidentId: String, sensorType: String, limit: Int = 10): List<SensorSampleEntity>
}
