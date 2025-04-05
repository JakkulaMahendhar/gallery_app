package com.app.galleryapp.domain.usecase

import com.app.galleryapp.domain.model.Album
import com.app.galleryapp.domain.model.MediaItem
import com.app.galleryapp.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlbumsUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(): Flow<List<Album>> = repository.getAlbums()
}

class GetMediaByAlbumUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(albumId: String): Flow<List<MediaItem>> = repository.getMediaByAlbumId(albumId)
}

class GetAllImagesUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getAllImages()
}

class GetAllVideosUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getAllVideos()
}

class GetCameraImagesUseCase @Inject constructor(private val repository: MediaRepository) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getCameraImages()
}
