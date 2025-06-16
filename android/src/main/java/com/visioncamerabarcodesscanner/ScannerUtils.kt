package com.visioncamerabarcodesscanner

import android.graphics.Rect
import android.graphics.RectF

import com.facebook.react.bridge.ReadableNativeMap
import com.facebook.react.bridge.WritableNativeMap

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E

data class Size(
    val width: Int = 0,
    val height: Int = 0,
)

data class Ratio(
    val width: Float = 1f,
    val height: Float = 1f,
)

data class BoxRatio(
    val leftRatio: Double,
    val topRatio: Double,
    val widthRatio: Double,
    val heightRatio: Double,
)

data class Scaling(
    val scale: Double,
    val dx: Double,
    val dy: Double,
)

enum class Orientation (val value: String) {
    PORTRAIT("portrait"),
    LANDSCAPE_RIGHT("landscape-right"),
    PORTRAIT_UPSIDE_DOWN("portrait-upside-down"),
    LANDSCAPE_LEFT("landscape-left"),
}

object ScannerUtils {
    fun getOptionsRatio(options: MutableMap<String, Any>?): Ratio {
        val ratio = options?.get("ratio") as? Map<*, *>
        val width = ((ratio?.get("width") as? Number)?.toFloat() ?: 1f).coerceIn(0f, 1f)
        val height = ((ratio?.get("height") as? Number)?.toFloat() ?: 1f).coerceIn(0f, 1f)
        return Ratio(width, height)
    }

    fun getOptionsOrientation(options: MutableMap<String, Any>?): Orientation {
        return when (options?.get("orientation") as? String) {
            "portrait" -> Orientation.PORTRAIT
            "landscape-left" -> Orientation.LANDSCAPE_LEFT
            "portrait-upside-down" -> Orientation.PORTRAIT_UPSIDE_DOWN
            "landscape-right" -> Orientation.LANDSCAPE_RIGHT
            else -> Orientation.PORTRAIT
        }
    }

    fun getOptionsViewSize(options: MutableMap<String, Any>?): Size? {
        val viewSize = options?.get("viewSize") as? Map<*, *>
        val width = (viewSize?.get("width") as? Number)?.toInt()
        val height = (viewSize?.get("height") as? Number)?.toInt()
        return if (width != null && height != null) {
            Size(width, height)
        } else {
            null
        }
    }

    fun getOptionsBarcodeFormats(options: MutableMap<String, Any>?): List<*> {
        val formats = options?.get("formats") as? List<*>
        return if (formats.isNullOrEmpty()) {
            listOf("all")
        } else {
            formats
        }
    }

    fun getSafeBarcodeFormats(formats: List<*>): List<Int> {
        return formats.mapNotNull { format ->
            if (format is String) {
                when (format) {
                    "code_128" -> FORMAT_CODE_128
                    "code_39" -> FORMAT_CODE_39
                    "code_93" -> FORMAT_CODE_93
                    "codabar" -> FORMAT_CODABAR
                    "ean_13" -> FORMAT_EAN_13
                    "ean_8" -> FORMAT_EAN_8
                    "itf" -> FORMAT_ITF
                    "upc_e" -> FORMAT_UPC_E
                    "upc_a" -> FORMAT_UPC_A
                    "qr" -> FORMAT_QR_CODE
                    "pdf_417" -> FORMAT_PDF417
                    "aztec" -> FORMAT_AZTEC
                    "data_matrix" -> FORMAT_DATA_MATRIX
                    "all" -> FORMAT_ALL_FORMATS
                    else -> FORMAT_ALL_FORMATS
                }
            } else {
                FORMAT_ALL_FORMATS
            }
        }
    }

    // ViewSize is used for scaling in the code.
    // The UI side should always be assumed as portrait.
    fun getSafeViewSize(imageSize: Size, viewSize: Size?): Size {
        if (viewSize != null) {
            val width = minOf(viewSize.width, viewSize.height)
            val height = maxOf(viewSize.width, viewSize.height)
            return Size(width, height)
        } else {
            return imageSize
        }
    }

    fun getImageSizeBasedOnRotation(imageSizeRaw: Size, imageRotationDegrees: Int): Size {
        return when (imageRotationDegrees) {
            90, 270 -> Size(imageSizeRaw.height, imageSizeRaw.width)
            else -> imageSizeRaw
        }
    }

    private fun getImageScalingBasedOnViewSize(imageSize: Size, viewSize: Size): Scaling {
        val imageWidth = imageSize.width.toDouble()
        val imageHeight = imageSize.height.toDouble()
        val imageAspect = imageWidth / imageHeight

        val viewWidth = viewSize.width.toDouble()
        val viewHeight = viewSize.height.toDouble()
        val viewAspect = viewWidth / viewHeight

        val scale: Double
        val scaledImageWidth: Double
        val scaledImageHeight: Double
        val dx: Double
        val dy: Double
        if (imageAspect > viewAspect) {
            // Image is wider -> crop left/right
            scale = viewHeight / imageHeight
            scaledImageWidth = imageWidth * scale
            scaledImageHeight = viewHeight
            dx = (scaledImageWidth - viewWidth) / 2.0
            dy = 0.0
        } else if (viewAspect > imageAspect) {
            // Image is taller → crop top/bottom
            scale = viewWidth / imageWidth
            scaledImageWidth = viewWidth
            scaledImageHeight = imageHeight * scale
            dx = 0.0
            dy = (scaledImageHeight - viewHeight) / 2.0
        } else {
            // Full compatibility -> crop nothing
            // Default if user does not provide viewSize value
            scale = 1.0
            scaledImageWidth = viewWidth
            scaledImageHeight = viewHeight
            dx = 0.0
            dy = 0.0
        }

        return Scaling(scale, dx, dy)
    }

    private fun getBoxRatioBasedOnOrientationForUI(
        boxScaled: RectF,
        viewSize: Size,
        orientation: Orientation
    ): BoxRatio {
        val viewWidth = viewSize.width.toDouble()
        val viewHeight = viewSize.height.toDouble()

        // Normalize to viewSize
        val leftRatio = (boxScaled.left / viewWidth).coerceIn(0.0, 1.0)
        val topRatio = (boxScaled.top / viewHeight).coerceIn(0.0, 1.0)
        val widthRatio = (boxScaled.width() / viewWidth).coerceIn(0.0, 1.0 - leftRatio)
        val heightRatio = (boxScaled.height() / viewHeight).coerceIn(0.0, 1.0 - topRatio)

        return when (orientation) {
            Orientation.PORTRAIT -> BoxRatio(leftRatio, topRatio, widthRatio, heightRatio)
            Orientation.LANDSCAPE_RIGHT -> BoxRatio(
                topRatio,
                1 - leftRatio - widthRatio,
                heightRatio,
                widthRatio
            )
            Orientation.PORTRAIT_UPSIDE_DOWN -> BoxRatio(
                1 - leftRatio - widthRatio,
                1- topRatio - heightRatio,
                widthRatio,
                heightRatio
            )
            Orientation.LANDSCAPE_LEFT -> BoxRatio(
                1 - topRatio - heightRatio,
                leftRatio,
                heightRatio,
                widthRatio
            )
        }
    }

    private fun scaleBoxBasedOnViewSize(box: Rect, imageSize: Size, viewSize: Size): RectF {
        val (scale, dx, dy) = getImageScalingBasedOnViewSize(imageSize, viewSize)
        return RectF(
            (box.left * scale - dx).toFloat(),
            (box.top * scale - dy).toFloat(),
            (box.right * scale - dx).toFloat(),
            (box.bottom * scale - dy).toFloat()
        )
    }

    fun filterBarcodes(
        barcodes: List<Barcode>,
        imageSize: Size,
        viewSize: Size,
        ratio: Ratio
    ): List<Barcode> {
        val viewWidth = viewSize.width.toFloat()
        val viewHeight = viewSize.height.toFloat()

        val scanViewWidth = ratio.width * viewWidth
        val scanViewHeight = ratio.height * viewHeight

        // Calculate the current scan area for viewSize.
        val scanLeft = (viewWidth - scanViewWidth) / 2
        val scanTop = (viewHeight - scanViewHeight) / 2
        val scanRight = scanLeft + scanViewWidth
        val scanBottom = scanTop + scanViewHeight

        return barcodes.filter { barcode ->
            val box = barcode.boundingBox ?: return@filter false
            val boxScaled = scaleBoxBasedOnViewSize(box, imageSize, viewSize)
            return@filter boxScaled.left >= scanLeft &&
                    boxScaled.top >= scanTop &&
                    boxScaled.right <= scanRight &&
                    boxScaled.bottom <= scanBottom
        }
    }

    fun formatBarcode(
        barcode: Barcode,
        imageSize: Size,
        viewSize: Size,
        orientation: Orientation
    ): ReadableNativeMap {
        val map = WritableNativeMap()
        val box = barcode.boundingBox

        if (box != null) {
            val boxScaled = scaleBoxBasedOnViewSize(box, imageSize, viewSize)
            val boxScaledRatio = getBoxRatioBasedOnOrientationForUI(boxScaled, viewSize, orientation)

            // Raw values
            map.putInt("width", boxScaled.width().toInt())
            map.putInt("height", boxScaled.height().toInt())
            map.putInt("left", boxScaled.left.toInt())
            map.putInt("top", boxScaled.top.toInt())
            map.putInt("right", boxScaled.right.toInt())
            map.putInt("bottom", boxScaled.bottom.toInt())

            // Normalized values
            map.putDouble("leftRatio", boxScaledRatio.leftRatio)
            map.putDouble("topRatio", boxScaledRatio.topRatio)
            map.putDouble("widthRatio", boxScaledRatio.widthRatio)
            map.putDouble("heightRatio", boxScaledRatio.heightRatio)
        }

        val rawValue = barcode.rawValue
        map.putString("rawValue", rawValue)
        val displayValue = barcode.displayValue
        map.putString("displayValue", displayValue)
        val valueType = barcode.valueType

        when (valueType) {
            Barcode.TYPE_WIFI -> {
                val ssid = barcode.wifi!!.ssid
                map.putString("ssid", ssid)
                val password = barcode.wifi!!.password
                map.putString("password", password)
                val encryptionType = barcode.wifi!!.encryptionType
                map.putInt("encryptionType", encryptionType)
            }

            Barcode.TYPE_URL -> {
                val title = barcode.url!!.title
                map.putString("title", title)
                val url = barcode.url!!.url
                map.putString("url", url)
            }
        }

        return map
    }
}
