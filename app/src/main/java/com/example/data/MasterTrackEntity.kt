package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "master_tracks")
data class MasterTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val formatLabel: String,
    val sampleRate: Int,
    val bitDepth: Int,
    val durationMs: Long,
    val finalLufs: Float,
    val finalPeakDb: Float,
    val profileName: String,
    val filePath: String,
    val createdAtMs: Long = System.currentTimeMillis()
)
