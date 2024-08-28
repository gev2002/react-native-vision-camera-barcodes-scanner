package com.visioncamerabarcodesscanner

import android.net.Uri
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableNativeArray
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
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
import com.google.mlkit.vision.common.InputImage

class ImageScanner(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    private var barcodeOptions = BarcodeScannerOptions.Builder()


    @ReactMethod
    fun process(uri: String, options: ReadableArray?, promise: Promise) {
        options?.toArrayList()?.forEach {
            when (it) {
                "code_128" -> barcodeOptions.setBarcodeFormats(FORMAT_CODE_128)
                "code_39" -> barcodeOptions.setBarcodeFormats(FORMAT_CODE_39)
                "code_93" -> barcodeOptions.setBarcodeFormats(FORMAT_CODE_93)
                "codabar" -> barcodeOptions.setBarcodeFormats(FORMAT_CODABAR)
                "ean_13" -> barcodeOptions.setBarcodeFormats(FORMAT_EAN_13)
                "ean_8" -> barcodeOptions.setBarcodeFormats(FORMAT_EAN_8)
                "itf" -> barcodeOptions.setBarcodeFormats(FORMAT_ITF)
                "upc_e" -> barcodeOptions.setBarcodeFormats(FORMAT_UPC_E)
                "upc_a" -> barcodeOptions.setBarcodeFormats(FORMAT_UPC_A)
                "qr" -> barcodeOptions.setBarcodeFormats(FORMAT_QR_CODE)
                "pdf_417" -> barcodeOptions.setBarcodeFormats(FORMAT_PDF417)
                "aztec" -> barcodeOptions.setBarcodeFormats(FORMAT_AZTEC)
                "data-matrix" -> barcodeOptions.setBarcodeFormats(FORMAT_DATA_MATRIX)
                "all" -> barcodeOptions.setBarcodeFormats(FORMAT_ALL_FORMATS)
            }
        }
        val scanner = BarcodeScanning.getClient(barcodeOptions.build())
        val parsedUri = Uri.parse(uri)
        val data = WritableNativeArray()
        val image = InputImage.fromFilePath(this.reactApplicationContext, parsedUri)
        val task: Task<List<Barcode>> = scanner.process(image)
        val barcodes: List<Barcode> = Tasks.await(task)
        for (barcode in barcodes) {
            val map = VisionCameraBarcodesScannerModule.processData(barcode)
            data.pushMap(map)
        }
        promise.resolve(data)
    }


    override fun getName() = NAME

    companion object {
        const val NAME = "ImageScanner"
    }
}