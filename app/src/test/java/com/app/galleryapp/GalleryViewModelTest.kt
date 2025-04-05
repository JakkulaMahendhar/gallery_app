package com.app.galleryapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.app.galleryapp.domain.model.Album
import com.app.galleryapp.domain.model.MediaItem
import com.app.galleryapp.domain.model.MediaType
import com.app.galleryapp.domain.usecase.GetAlbumsUseCase
import com.app.galleryapp.domain.usecase.GetAllImagesUseCase
import com.app.galleryapp.domain.usecase.GetAllVideosUseCase
import com.app.galleryapp.domain.usecase.GetCameraImagesUseCase
import com.app.galleryapp.domain.usecase.GetMediaByAlbumUseCase
import com.app.galleryapp.presentation.screens.gallery.GalleryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GalleryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val getAlbumsUseCase = mock(GetAlbumsUseCase::class.java)
    private val getMediaByAlbumUseCase = mock(GetMediaByAlbumUseCase::class.java)
    private val getAllImagesUseCase = mock(GetAllImagesUseCase::class.java)
    private val getAllVideosUseCase = mock(GetAllVideosUseCase::class.java)
    private val getCameraImagesUseCase = mock(GetCameraImagesUseCase::class.java)

    private lateinit var viewModel: GalleryViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GalleryViewModel(
            getAlbumsUseCase,
            getMediaByAlbumUseCase,
            getAllImagesUseCase,
            getAllVideosUseCase,
            getCameraImagesUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAlbums should update albums state`() = runTest {
        val albumList = listOf(Album("1", "Camera", 0, ""))
        `when`(getAlbumsUseCase()).thenReturn(flowOf(albumList))

        viewModel.loadAlbums()

        Assert.assertEquals(albumList, viewModel.albums.value)
    }

    @Test
    fun `selectAlbum should update selected album and load media`() = runTest {
        val album = Album("1", "Camera", 0, "")
        val mediaItems = listOf(MediaItem("1", "uri", "image/jpeg", "123L", 1L,2L, MediaType.IMAGE))

        `when`(getMediaByAlbumUseCase("1")).thenReturn(flowOf(mediaItems))

        viewModel.selectAlbum(album)

        Assert.assertEquals(album, viewModel.selectedAlbum.value)
        Assert.assertEquals(mediaItems, viewModel.currentAlbumMedia.value)
    }

    @Test
    fun `selectAlbum with __ALL_IMAGES__ should invoke getAllImagesUseCase`() = runTest {
        val album = Album("__ALL_IMAGES__", "All Images", 0, "")
        val mediaItems = listOf(MediaItem("1", "uri", "image/jpeg", "123L", 1L,2L, MediaType.IMAGE))

        `when`(getAllImagesUseCase()).thenReturn(flowOf(mediaItems))

        viewModel.selectAlbum(album)

        Assert.assertEquals(mediaItems, viewModel.currentAlbumMedia.value)
    }

    @Test
    fun `selectedMediaItem should update selected media item`() {
        val mediaItem = MediaItem("1", "uri", "image/jpeg", "123L", 1L,2L, MediaType.IMAGE)

        viewModel.selectedMediaItem(mediaItem)

        Assert.assertEquals(mediaItem, viewModel.selectedMediaItem.value)
    }

    @Test
    fun `clearSelectedAlbum should reset album and media`() {
        val album = Album("1", "Sample Album", 0, "")
        viewModel.selectAlbum(album)
        viewModel.clearSelectedAlbum()

        Assert.assertNull(viewModel.selectedAlbum.value)
        Assert.assertTrue(viewModel.currentAlbumMedia.value.isEmpty())
    }
}
