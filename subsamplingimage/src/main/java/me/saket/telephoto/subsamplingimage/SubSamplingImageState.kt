@file:Suppress("NAME_SHADOWING")

package me.saket.telephoto.subsamplingimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.saket.telephoto.subsamplingimage.internal.BitmapTileGrid
import me.saket.telephoto.subsamplingimage.internal.SkiaImageRegionDecoder
import me.saket.telephoto.subsamplingimage.internal.generateBitmapTileGrid
import me.saket.telephoto.zoomable.ZoomableState
import java.io.IOException

@Composable
fun rememberSubSamplingImageState(
  zoomableState: ZoomableState,
  imageSource: ImageSource,
  stateListener: SubSamplingImageEventListener = SubSamplingImageEventListener.Empty
): SubSamplingImageState {
  val context = LocalContext.current
  val stateListener by rememberUpdatedState(stateListener)

  val decoder by produceState<SkiaImageRegionDecoder?>(initialValue = null, key1 = imageSource) {
    try {
      value = SkiaImageRegionDecoder.create(context, imageSource).also {
        stateListener.onImageLoaded()
        zoomableState.setUnscaledContentSize(it.imageSize)
      }
    } catch (e: IOException) {
      stateListener.onImageLoadingFailed(e)
    }
  }

  val tileGrids = remember {
    MutableStateFlow<BitmapTileGrid>(emptyMap())
  }

  LaunchedEffect(zoomableState, decoder) {
    val decoder = snapshotFlow { decoder }.filterNotNull().first()
    val transformations = snapshotFlow { zoomableState.contentTransformations }

    transformations
      .map { it.viewportSize }
      .distinctUntilChanged()
      .collect { viewportSize ->
        tileGrids.update {
          generateBitmapTileGrid(
            viewportSize = viewportSize,
            unscaledImageSize = decoder.imageSize
          )
        }
      }
  }

  return remember {
    SubSamplingImageState(tileGrids)
  }
}

@Stable
class SubSamplingImageState internal constructor(
  internal val tileGrids: StateFlow<BitmapTileGrid>,
)

private operator fun IntSize.times(other: Float): Size =
  Size(width = width * other, height = height * other)