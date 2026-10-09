package ru.yourname.events.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun observeAll(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertAll(events: List<EventEntity>)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("SELECT * FROM events WHERE contactId = :contactId LIMIT 1")
    suspend fun findByContact(contactId: Long): EventEntity?
}
