package com.app.galleryapp.presentation.screens.gallery
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.galleryapp.presentation.screens.media.MediaGrid
import com.app.galleryapp.presentation.screens.media.MediaViewScreen
import com.app.galleryapp.presentation.screens.album.AlbumsGrid

@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val albums by viewModel.albums.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedMediaItem by viewModel.selectedMediaItem.collectAsState()
    val currentAlbumMedia by viewModel.currentAlbumMedia.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            selectedMediaItem != null -> {
                MediaViewScreen(
                    mediaItem = selectedMediaItem!!,
                    onBackPressed = {
                        viewModel.selectedMediaItem(null)
                    }
                )
            }

            selectedAlbum == null -> {
                AlbumsGrid(
                    albums = albums,
                    onAlbumClick = { album ->
                        viewModel.selectAlbum(album)
                    }
                )
            }

            else -> {
                MediaGrid(
                    albumName = selectedAlbum!!.name,
                    mediaItems = currentAlbumMedia,
                    onBackPressed = {
                        viewModel.clearSelectedAlbum()
                    },
                    onMediaClick = { mediaItem ->
                        viewModel.selectedMediaItem(mediaItem)
                    }
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}