package com.app.galleryapp.presentation.screens.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.galleryapp.R
import com.app.galleryapp.domain.model.MediaItem
import com.app.galleryapp.domain.model.MediaType
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ViewMode {
    GRID,
    LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGrid(
    albumName: String,
    mediaItems: List<MediaItem>,
    onBackPressed: () -> Unit,
    onMediaClick: (MediaItem) -> Unit
) {
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(albumName) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }) {
                        Icon(
                            painter = if (viewMode == ViewMode.GRID) painterResource(id = R.drawable.baseline_grid_view_24) else painterResource(id = R.drawable.baseline_list_24), //List icon and Grid icon
                            contentDescription = "Change view mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (viewMode == ViewMode.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 80.dp),
                contentPadding = PaddingValues(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(mediaItems) { mediaItem ->
                    MediaGridItem(mediaItem = mediaItem, onClick = { onMediaClick(mediaItem) })
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(mediaItems) { mediaItem ->
                    MediaListItem(mediaItem = mediaItem, onClick = { onMediaClick(mediaItem) })
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaGridItem(mediaItem: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlideImage(
                model = mediaItem.uri,
                contentDescription = mediaItem.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (mediaItem.type == MediaType.VIDEO) {
                Icon(
                    painter = painterResource(R.drawable.baseline_play_circle_outline_24), //Play Circle
                    contentDescription = "Video",
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center),
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MediaListItem(mediaItem: MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
            ) {
                GlideImage(
                    model = mediaItem.uri,
                    contentDescription = mediaItem.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (mediaItem.type == MediaType.VIDEO) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_play_circle_outline_24), //PlayCircle
                        contentDescription = "Video",
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = mediaItem.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = Date(mediaItem.dateAdded * 1000)

                Text(
                    text = sdf.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val fileSize = when {
                    mediaItem.size < 1024 -> "${mediaItem.size} B"
                    mediaItem.size < 1024 * 1024 -> "${mediaItem.size / 1024} KB"
                    else -> "${mediaItem.size / (1024 * 1024)} MB"
                }

                Text(
                    text = fileSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}