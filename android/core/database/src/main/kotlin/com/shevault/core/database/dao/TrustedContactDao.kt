package com.shevault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shevault.core.database.entity.TrustedContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedContactDao {

    @Query("SELECT * FROM trusted_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<TrustedContactEntity>>

    @Query("SELECT * FROM trusted_contacts WHERE isPrimary = 1")
    suspend fun getPrimaryContacts(): List<TrustedContactEntity>

    @Query("SELECT * FROM trusted_contacts WHERE id = :id")
    suspend fun getContactById(id: String): TrustedContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<TrustedContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContactEntity)

    @Query("DELETE FROM trusted_contacts WHERE id = :id")
    suspend fun deleteContact(id: String)
}
