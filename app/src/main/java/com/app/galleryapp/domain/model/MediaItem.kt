package com.app.galleryapp.domain.model


enum class MediaType {
    IMAGE, VIDEO
}

data class MediaItem(
    val id: String,
    val name: String,
    val uri: String,
    val path: String,
    val dateAdded: Long,
    val size: Long,
    val type: MediaType
)

data class Album(
    val id: String,
    val name: String,
    val mediaCount: Int,
    val coverPath: String
)