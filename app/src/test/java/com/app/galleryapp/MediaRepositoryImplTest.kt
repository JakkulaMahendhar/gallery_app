package com.app.galleryapp

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.app.galleryapp.data.repository.MediaRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaRepositoryImplTest {

    private lateinit var repository: MediaRepositoryImpl
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var contentResolver: ContentResolver

    @Mock
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Ensure context.contentResolver returns the mocked contentResolver
        Mockito.`when`(context.contentResolver).thenReturn(contentResolver)
        cursor = mock(Cursor::class.java)
        repository = MediaRepositoryImpl(context = context)
    }

    @Test
    fun `getAllImages returns list of MediaItem`() = runTest(UnconfinedTestDispatcher()) {
        val uri = mock(Uri::class.java)

        whenever(contentResolver.query(
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(cursor)

        whenever(cursor.moveToNext()).thenReturn(true, false)
        whenever(cursor.getString(anyInt())).thenReturn("/storage/emulated/0/DCIM/Camera/photo1.jpg")
        whenever(cursor.getLong(anyInt())).thenReturn(123456L)
        whenever(cursor.getInt(anyInt())).thenReturn(1)

        val result = repository.getAllImages().first()

        assertEquals(1, result.size)
        assertTrue(result[0].path.endsWith("photo1.jpg") ?: false)
    }

    @Test
    fun `getAllVideos returns list of MediaItem`() = runTest {
        whenever(contentResolver.query(
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(cursor)

        whenever(cursor.moveToNext()).thenReturn(true, false)
        whenever(cursor.getString(anyInt())).thenReturn("/storage/emulated/0/DCIM/Camera/video1.mp4")
        whenever(cursor.getLong(anyInt())).thenReturn(123456L)
        whenever(cursor.getInt(anyInt())).thenReturn(3)

        val result = repository.getAllVideos().first()

        assertEquals(1, result.size)
        assertTrue(result[0].path.endsWith("video1.mp4") ?: false)
    }

    @Test
    fun `getAllAlbums returns list of Album`() = runTest {
        whenever(contentResolver.query(
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(cursor)

        whenever(cursor.moveToNext()).thenReturn(true, false)
        whenever(cursor.getString(anyInt())).thenReturn("Camera")
        whenever(cursor.getInt(anyInt())).thenReturn(1)

        val result = repository.getAlbums().first()

        assertTrue(result.any { it.name == "Camera" })
    }

    @Test
    fun `getImagesFromAlbum returns media items of specific album`() = runTest {
        val albumId = "album123"
        whenever(contentResolver.query(
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(cursor)

        whenever(cursor.moveToNext()).thenReturn(true, false)
        whenever(cursor.getString(anyInt())).thenReturn("/storage/emulated/0/Albums/album123/image.jpg")
        whenever(cursor.getLong(anyInt())).thenReturn(123456L)
        whenever(cursor.getInt(anyInt())).thenReturn(1)

        val result = repository.getMediaByAlbumId(albumId).first()

        assertEquals(1, result.size)
        assertTrue(result[0].path.contains(albumId) ?: false)
    }
}
