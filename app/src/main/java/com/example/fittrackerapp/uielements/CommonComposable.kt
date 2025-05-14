package com.example.fittrackerapp.uielements

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ClickableRow(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clickable { onClick() }
            .background(Color.LightGray)
            .padding(vertical = 12.dp)
    ) {
        Text(text)
    }
}

@Composable
fun CenteredPicker(
    items: List<Int>,
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 50.dp
    val visibleItemsCount = 5
    val centerIndex = visibleItemsCount/2

    Box(modifier = modifier.height(itemHeight * visibleItemsCount)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = (itemHeight * centerIndex+25.dp)),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            coroutineScope.launch {
                listState.animateScrollToItem(selectedIndex)
            }

            items(items) { item ->
                val center = listState.firstVisibleItemIndex
                val isSelected = item == center

                CenteredListItem(onItemSelected, item, isSelected, itemHeight)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color(0xFF1B9AAA).copy(alpha = 0.2f))
        )
    }
}

@Composable
fun CenteredListItem(onItemSelected: (Int) -> Unit, item: Int, isSelected: Boolean, itemHeight: Dp) {
    Text(
        text = "$item",
        fontSize = if (isSelected) 24.sp else 16.sp,
        color = if (isSelected) Color.White else Color.Gray,
        modifier = Modifier
            .height(itemHeight)
            .fillMaxWidth()
            .wrapContentHeight(),
        textAlign = TextAlign.Center
    )
    if (isSelected) {
        onItemSelected(item)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayerFromFile(file: File) {
    val context = LocalContext.current
    val uri = file.toUri()

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Обрезает края, чтобы сохранить 16:9
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f) // Устанавливаем нужное соотношение
            .clipToBounds()
    )
}

@Composable
fun FileIcon(file: File) {
    if (file.exists()) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(36.dp) // круглый аватар
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Log.e("ExerciseChangeWindow", "Файл иконки не найден: ${file.absolutePath}")
    }
}