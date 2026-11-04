package com.example.tabletworkspace
import com.example.tabletworkspace.data.pages.PageStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads a page image off the main thread; PageStore keeps a per-size LRU bitmap cache. */
@Composable
fun rememberStoredImage(store: PageStore, name: String, targetPx: Int = 1600): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, store, name, targetPx) {
        value = withContext(Dispatchers.IO) { store.image(name, targetPx)?.asImageBitmap() }
    }
    return bitmap
}
