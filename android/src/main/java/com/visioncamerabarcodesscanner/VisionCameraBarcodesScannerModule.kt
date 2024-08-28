package com.visioncamerabarcodesscanner

import android.media.Image
import com.facebook.react.bridge.ReadableNativeMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy
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

class VisionCameraBarcodesScannerModule(
    proxy: VisionCameraProxy, options: MutableMap<String, Any>?
) : FrameProcessorPlugin() {
    private val optionsBuilder: BarcodeScannerOptions.Builder = BarcodeScannerOptions.Builder()
    private val barcodeOptions = options?.values?.firstOrNull() as? List<*> ?: listOf("all")

    init {
        barcodeOptions.forEach { format ->
            when (format) {
                "code_128" -> optionsBuilder.setBarcodeFormats(FORMAT_CODE_128)
                "code_39" -> optionsBuilder.setBarcodeFormats(FORMAT_CODE_39)
                "code_93" -> optionsBuilder.setBarcodeFormats(FORMAT_CODE_93)
                "codabar" -> optionsBuilder.setBarcodeFormats(FORMAT_CODABAR)
                "ean_13" -> optionsBuilder.setBarcodeFormats(FORMAT_EAN_13)
                "ean_8" -> optionsBuilder.setBarcodeFormats(FORMAT_EAN_8)
                "itf" -> optionsBuilder.setBarcodeFormats(FORMAT_ITF)
                "upc_e" -> optionsBuilder.setBarcodeFormats(FORMAT_UPC_E)
                "upc_a" -> optionsBuilder.setBarcodeFormats(FORMAT_UPC_A)
                "qr" -> optionsBuilder.setBarcodeFormats(FORMAT_QR_CODE)
                "pdf_417" -> optionsBuilder.setBarcodeFormats(FORMAT_PDF417)
                "aztec" -> optionsBuilder.setBarcodeFormats(FORMAT_AZTEC)
                "data-matrix" -> optionsBuilder.setBarcodeFormats(FORMAT_DATA_MATRIX)
                "all" -> optionsBuilder.setBarcodeFormats(FORMAT_ALL_FORMATS)
                else -> optionsBuilder.setBarcodeFormats(FORMAT_ALL_FORMATS)
            }
        }
    }

    override fun callback(frame: Frame, arguments: Map<String, Any>?): Any {
        try {
            val scanner = BarcodeScanning.getClient(optionsBuilder.build())
            val mediaImage: Image = frame.image
            val image =
                InputImage.fromMediaImage(mediaImage, frame.imageProxy.imageInfo.rotationDegrees)
            val task: Task<List<Barcode>> = scanner.process(image)
            val barcodes: List<Barcode> = Tasks.await(task)
            val array = WritableNativeArray()
            for (barcode in barcodes) {
                val map = processData(barcode)
                array.pushMap(map)
            }
            return array.toArrayList()
        } catch (e: Exception) {
            throw Exception("Error processing barcode scanner: $e ")
        }
    }


    companion object {
        fun processData(barcode: Barcode): ReadableNativeMap {
            val map = WritableNativeMap()
            val bounds = barcode.boundingBox
            if (bounds != null) {
                map.putInt("width", bounds.width())
                map.putInt("height", bounds.height())
                map.putInt("top", bounds.top)
                map.putInt("bottom", bounds.bottom)
                map.putInt("left", bounds.left)
                map.putInt("right", bounds.right)
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
}


