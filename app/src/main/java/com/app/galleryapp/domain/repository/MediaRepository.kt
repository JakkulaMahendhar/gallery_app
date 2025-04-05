package com.app.galleryapp.domain.repository

import com.app.galleryapp.domain.model.Album
import com.app.galleryapp.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getAlbums(): Flow<List<Album>>
    fun getMediaByAlbumId(albumId: String): Flow<List<MediaItem>>
    fun getAllImages(): Flow<List<MediaItem>>
    fun getAllVideos(): Flow<List<MediaItem>>
    fun getCameraImages(): Flow<List<MediaItem>>
}