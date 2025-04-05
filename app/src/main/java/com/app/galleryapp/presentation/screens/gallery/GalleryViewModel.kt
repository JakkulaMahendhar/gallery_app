package com.app.galleryapp.presentation.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.galleryapp.domain.model.Album
import com.app.galleryapp.domain.model.MediaItem
import com.app.galleryapp.domain.usecase.GetAlbumsUseCase
import com.app.galleryapp.domain.usecase.GetAllImagesUseCase
import com.app.galleryapp.domain.usecase.GetAllVideosUseCase
import com.app.galleryapp.domain.usecase.GetCameraImagesUseCase
import com.app.galleryapp.domain.usecase.GetMediaByAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getMediaByAlbumUseCase: GetMediaByAlbumUseCase,
    private val getAllImagesUseCase: GetAllImagesUseCase,
    private val getAllVideosUseCase: GetAllVideosUseCase,
    private val getCameraImagesUseCase: GetCameraImagesUseCase
) : ViewModel() {

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums

    private val _currentAlbumMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentAlbumMedia: StateFlow<List<MediaItem>> = _currentAlbumMedia

    private val _selectedAlbum = MutableStateFlow<Album?>(null)
    val selectedAlbum: StateFlow<Album?> = _selectedAlbum

    private val _selectedMediaItem = MutableStateFlow<MediaItem?>(null)
    val selectedMediaItem: StateFlow<MediaItem?> = _selectedMediaItem

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAlbums() {
        viewModelScope.launch {
            _isLoading.value = true

            getAlbumsUseCase()
                .catch { e ->
                    // Handle error
                    _isLoading.value = false
                }
                .collectLatest { albums ->
                    _albums.value = albums
                    _isLoading.value = false
                }
        }
    }

    fun selectedMediaItem(mediaItem: MediaItem?) {
        _selectedMediaItem.value = mediaItem
    }

    fun selectAlbum(album: Album) {
        _selectedAlbum.value = album
        loadMediaForAlbum(album)
    }

    private fun loadMediaForAlbum(album: Album) {
        viewModelScope.launch {
            _isLoading.value = true
            val flow = when (album.id) {
                "__ALL_IMAGES__" -> getAllImagesUseCase()
                "__ALL_VIDEOS__" -> getAllVideosUseCase()
                "__CAMERA__" -> getCameraImagesUseCase()
                else -> getMediaByAlbumUseCase(album.id)
            }

            flow.catch { e ->
//                handleError(e)
            }.collectLatest { media ->
                _currentAlbumMedia.value = media
                _isLoading.value = false
            }
        }
    }

    fun clearSelectedAlbum() {
        _selectedAlbum.value = null
        _currentAlbumMedia.value = emptyList()
    }
}