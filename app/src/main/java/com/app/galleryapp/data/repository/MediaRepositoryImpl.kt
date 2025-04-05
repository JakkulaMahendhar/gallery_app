package com.app.galleryapp.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import com.app.galleryapp.domain.model.Album
import com.app.galleryapp.domain.model.MediaItem
import com.app.galleryapp.domain.model.MediaType
import com.app.galleryapp.domain.repository.MediaRepository
import dagger.hilt.android.qualifiers.ApplicationContext

class MediaRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaRepository {

    override fun getAlbums(): Flow<List<Album>> = flow {
        val albums = mutableMapOf<String, Album>()

        // Add special albums
        albums["__ALL_IMAGES__"] = Album(
            id = "__ALL_IMAGES__",
            name = "All Images",
            mediaCount = 0,
            coverPath = ""
        )

        albums["__ALL_VIDEOS__"] = Album(
            id = "__ALL_VIDEOS__",
            name = "All Videos",
            mediaCount = 0,
            coverPath = ""
        )

        albums["__CAMERA__"] = Album(
            id = "__CAMERA__",
            name = "Camera",
            mediaCount = 0,
            coverPath = ""
        )

        // Query image albums
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            var allImagesCount = 0
            var cameraCount = 0

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val bucketId = cursor.getString(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"
                val path = cursor.getString(pathColumn)

                // Skip thumbnails and system folders
                if (path.contains("/.thumbnail") || path.contains("/.cache") || path.contains("/.nomedia")) {
                    continue
                }

                // Count all images
                allImagesCount++

                // Check if it's from camera
                if (bucketName.equals("Camera", ignoreCase = true) ||
                    bucketName.equals("DCIM", ignoreCase = true)) {
                    cameraCount++
                }

                // Update or create album
                val album = albums[bucketId] ?: Album(
                    id = bucketId,
                    name = bucketName,
                    mediaCount = 0,
                    coverPath = ""
                )

                albums[bucketId] = album.copy(
                    mediaCount = album.mediaCount + 1,
                    coverPath = if (album.coverPath.isEmpty()) path else album.coverPath
                )
            }

            // Update special albums
            albums["__ALL_IMAGES__"] = albums["__ALL_IMAGES__"]!!.copy(
                mediaCount = allImagesCount,
                coverPath = if (cursor.moveToFirst()) cursor.getString(pathColumn) else ""
            )

            albums["__CAMERA__"] = albums["__CAMERA__"]!!.copy(
                mediaCount = cameraCount
            )
        }

        // Query video albums
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            var allVideosCount = 0

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val bucketId = cursor.getString(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"
                val path = cursor.getString(pathColumn)

                // Skip thumbnails and system folders
                if (path.contains("/.thumbnail") || path.contains("/.cache") || path.contains("/.nomedia")) {
                    continue
                }

                // Count all videos
                allVideosCount++

                // Update or create album
                val album = albums[bucketId] ?: Album(
                    id = bucketId,
                    name = bucketName,
                    mediaCount = 0,
                    coverPath = ""
                )

                albums[bucketId] = album.copy(
                    mediaCount = album.mediaCount + 1,
                    coverPath = if (album.coverPath.isEmpty()) path else album.coverPath
                )
            }

            // Update all videos album
            albums["__ALL_VIDEOS__"] = albums["__ALL_VIDEOS__"]!!.copy(
                mediaCount = allVideosCount,
                coverPath = if (cursor.moveToFirst()) cursor.getString(pathColumn) else ""
            )
        }

        emit(albums.values.toList())
    }.flowOn(Dispatchers.IO)

    override fun getMediaByAlbumId(albumId: String): Flow<List<MediaItem>> = flow {
        val mediaItems = mutableListOf<MediaItem>()

        val imageSelection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
        val videoSelection = "${MediaStore.Video.Media.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(albumId)

        // Query images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            imageSelection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.IMAGE
                    )
                )
            }
        }

        // Query videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection,
            videoSelection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.VIDEO
                    )
                )
            }
        }

        emit(mediaItems.sortedByDescending { it.dateAdded })
    }.flowOn(Dispatchers.IO)

    override fun getAllImages(): Flow<List<MediaItem>> = flow {
        val mediaItems = mutableListOf<MediaItem>()

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA
        )

        // Exclude thumbnails, cache, and .nomedia folders
        val selection = "${MediaStore.Images.Media.DATA} NOT LIKE ? AND " +
                "${MediaStore.Images.Media.DATA} NOT LIKE ? AND " +
                "${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%/.thumbnail%", "%/.cache%", "%/.nomedia%")

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.IMAGE
                    )
                )
            }
        }

        emit(mediaItems)
    }.flowOn(Dispatchers.IO)

    override fun getAllVideos(): Flow<List<MediaItem>> = flow {
        val mediaItems = mutableListOf<MediaItem>()

        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
        )

        // Exclude thumbnails, cache, and .nomedia folders
        val selection = "${MediaStore.Video.Media.DATA} NOT LIKE ? AND " +
                "${MediaStore.Video.Media.DATA} NOT LIKE ? AND " +
                "${MediaStore.Video.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%/.thumbnail%", "%/.cache%", "%/.nomedia%")

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.VIDEO
                    )
                )
            }
        }

        emit(mediaItems)
    }.flowOn(Dispatchers.IO)

    override fun getCameraImages(): Flow<List<MediaItem>> = flow {
        val mediaItems = mutableListOf<MediaItem>()

        // Query for camera images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection = "(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ? OR " +
                "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?)"
        val selectionArgs = arrayOf("Camera", "DCIM")

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.IMAGE
                    )
                )
            }
        }

        // Query for camera videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        )

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            videoProjection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val path = cursor.getString(pathColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                mediaItems.add(
                    MediaItem(
                        id = id.toString(),
                        name = name,
                        uri = contentUri.toString(),
                        path = path,
                        dateAdded = dateAdded,
                        size = size,
                        type = MediaType.VIDEO
                    )
                )
            }
        }

        emit(mediaItems.sortedByDescending { it.dateAdded })
    }.flowOn(Dispatchers.IO)
}