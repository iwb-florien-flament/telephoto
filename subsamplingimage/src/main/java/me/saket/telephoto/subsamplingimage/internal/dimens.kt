package me.saket.telephoto.subsamplingimage.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.ceil

internal fun Size.coerceAtLeast(other: Size): Size {
  return Size(
    width = width.coerceAtLeast(other.width),
    height = height.coerceAtLeast(other.height)
  )
}

// todo: these two functions can be removed by storing an IntRect in BitmapTile.
internal fun Size.discardFractionalParts(): IntSize {
  return IntSize(width = width.toInt(), height = height.toInt())
}

internal fun Offset.discardFractionalParts(): IntOffset {
  return IntOffset(x.toInt(), y.toInt())
}

internal fun Float.toCeilInt(): Int {
  return ceil(this).toInt()
}