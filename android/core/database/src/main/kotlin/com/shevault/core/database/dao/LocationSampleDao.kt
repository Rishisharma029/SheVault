package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shevault.core.database.entity.LocationSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationSampleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSample(sample: LocationSampleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<LocationSampleEntity>)

    @Query("SELECT * FROM location_samples WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getSamplesForIncident(incidentId: String): Flow<List<LocationSampleEntity>>

    @Query("SELECT * FROM location_samples WHERE incidentId = :incidentId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSampleForIncident(incidentId: String): LocationSampleEntity?

    @Query("SELECT * FROM location_samples ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSample(): LocationSampleEntity?

    @Query("SELECT * FROM location_samples ORDER BY timestamp DESC")
    fun getAllSamples(): Flow<List<LocationSampleEntity>>
}
