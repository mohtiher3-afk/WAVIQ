package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MasterTrackDao {
    @Query("SELECT * FROM master_tracks ORDER BY createdAtMs DESC")
    fun getAllTracks(): Flow<List<MasterTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: MasterTrackEntity): Long

    @Query("SELECT COUNT(*) FROM master_tracks")
    suspend fun getTrackCount(): Int

    @Query("DELETE FROM master_tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Int)
}
