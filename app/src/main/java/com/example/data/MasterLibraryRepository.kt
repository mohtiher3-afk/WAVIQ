package com.example.data

import kotlinx.coroutines.flow.Flow

class MasterLibraryRepository(private val dao: MasterTrackDao) {
    val tracks: Flow<List<MasterTrackEntity>> = dao.getAllTracks()

    suspend fun saveMaster(track: MasterTrackEntity): Long = dao.insertTrack(track)

    suspend fun deleteMaster(id: Int) = dao.deleteTrackById(id)
}
