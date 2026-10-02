package com.shevault.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shevault.core.database.dao.DeliveryAttemptDao
import com.shevault.core.database.dao.IncidentDao
import com.shevault.core.database.dao.IncidentEventDao
import com.shevault.core.database.dao.LocationSampleDao
import com.shevault.core.database.dao.SensorSampleDao
import com.shevault.core.database.dao.TrustedContactDao
import com.shevault.core.database.entity.DeliveryAttemptEntity
import com.shevault.core.database.entity.IncidentEntity
import com.shevault.core.database.entity.IncidentEventEntity
import com.shevault.core.database.entity.IncidentSessionEntity
import com.shevault.core.database.entity.LocationSampleEntity
import com.shevault.core.database.entity.SensorSampleEntity
import com.shevault.core.database.entity.TrustedContactEntity

// Backward compatibility typealiases
typealias TrustedContactEntity = com.shevault.core.database.entity.TrustedContactEntity
typealias IncidentEntity = com.shevault.core.database.entity.IncidentEntity
typealias IncidentEventEntity = com.shevault.core.database.entity.IncidentEventEntity
typealias IncidentSessionEntity = com.shevault.core.database.entity.IncidentSessionEntity
typealias LocationSampleEntity = com.shevault.core.database.entity.LocationSampleEntity
typealias DeliveryAttemptEntity = com.shevault.core.database.entity.DeliveryAttemptEntity
typealias SensorSampleEntity = com.shevault.core.database.entity.SensorSampleEntity

typealias IncidentDao = com.shevault.core.database.dao.IncidentDao
typealias IncidentEventDao = com.shevault.core.database.dao.IncidentEventDao
typealias LocationSampleDao = com.shevault.core.database.dao.LocationSampleDao
typealias DeliveryAttemptDao = com.shevault.core.database.dao.DeliveryAttemptDao
typealias SensorSampleDao = com.shevault.core.database.dao.SensorSampleDao
typealias TrustedContactDao = com.shevault.core.database.dao.TrustedContactDao

@Database(
    entities = [
        IncidentEntity::class,
        IncidentEventEntity::class,
        LocationSampleEntity::class,
        DeliveryAttemptEntity::class,
        SensorSampleEntity::class,
        TrustedContactEntity::class,
        IncidentSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SheVaultDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun incidentEventDao(): IncidentEventDao
    abstract fun locationSampleDao(): LocationSampleDao
    abstract fun deliveryAttemptDao(): DeliveryAttemptDao
    abstract fun sensorSampleDao(): SensorSampleDao
    abstract fun trustedContactDao(): TrustedContactDao
}
