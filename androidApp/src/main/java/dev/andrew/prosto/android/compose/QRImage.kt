package dev.andrew.prosto.android.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import dev.andrew.prosto.ProstoTheme
import io.github.g0dkar.qrcode.QRCode
import io.github.g0dkar.qrcode.QRCodeDataType

object QRGenCache {
    private val cache = HashMap<String, ImageBitmap>(4)
    fun getCache(data: String): ImageBitmap? {
        return cache.getOrDefault(data, null)
    }
    fun addCache(data: String, bitmap: ImageBitmap) {
        cache.put(data, bitmap)
    }
}

@Composable
fun QRImage(modifier: Modifier = Modifier, data: String) {
        QRGenCache.getCache(data).let { cachedBitmap ->
            val bitmapToDraw = if (cachedBitmap == null) {
                val qrCodeNative = QRCode(data = data,
                    dataType = QRCodeDataType.DEFAULT)
                    .render(
                        brightColor = 0x00FFFFFF
                    )
                    .nativeImage()
                (qrCodeNative as Bitmap).asImageBitmap().also {
                    QRGenCache.addCache(data, it)
                }
            } else {
                cachedBitmap
            }
            Image(modifier = modifier,
                bitmap = bitmapToDraw, contentDescription = "")
        }
}

@Preview
@Composable
fun QRCodePreview() {
    ProstoTheme {
        QRImage(data = "Keyboard start witch QWERTY")
    }
}