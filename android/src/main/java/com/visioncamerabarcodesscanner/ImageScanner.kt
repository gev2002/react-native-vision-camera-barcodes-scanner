package com.visioncamerabarcodesscanner

import android.net.Uri

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableNativeArray

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS

class ImageScanner(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        const val NAME = "ImageScanner"
    }

    private var scanner = BarcodeScanning.getClient()
    private val scannerBuilder = BarcodeScannerOptions.Builder()

    override fun getName() = NAME

    @ReactMethod
    fun process(uri: String, options: MutableMap<String, Any>?, promise: Promise) {
        val scannerBarcodeFormats = ScannerUtils.getOptionsBarcodeFormats(options)
        val scannerRatio = ScannerUtils.getOptionsRatio(options)
        val scannerOrientation = ScannerUtils.getOptionsOrientation(options)
        val scannerViewSize = ScannerUtils.getOptionsViewSize(options)

        val barcodeFormats = ScannerUtils.getSafeBarcodeFormats(scannerBarcodeFormats)
        if (barcodeFormats.contains(FORMAT_ALL_FORMATS)) {
            scannerBuilder.setBarcodeFormats(FORMAT_ALL_FORMATS)
        } else {
            val firstFormat = barcodeFormats.first()
            val otherFormats = barcodeFormats.drop(1).toIntArray()
            scannerBuilder.setBarcodeFormats(firstFormat, *otherFormats)
        }

        scanner = BarcodeScanning.getClient(scannerBuilder.build())

        val array = WritableNativeArray()

        val imageUri = Uri.parse(uri)
        // Loads image and applies EXIF rotation. width/height are already corrected.
        val image = InputImage.fromFilePath(this.reactApplicationContext, imageUri)
        val imageSize = Size(image.width, image.height)
        val viewSize = ScannerUtils.getSafeViewSize(imageSize, scannerViewSize)

        val task: Task<List<Barcode>> = scanner.process(image)
        val barcodes: List<Barcode> = Tasks.await(task)

        // Filter barcodes if scanning ratio is restricted.
        val barcodesFiltered =
            if (scannerRatio.width != 1f || scannerRatio.height != 1f)
                ScannerUtils.filterBarcodes(barcodes, imageSize, viewSize, scannerRatio)
            else
                barcodes

        for (barcode in barcodesFiltered) {
            val map = ScannerUtils.formatBarcode(barcode, imageSize, viewSize, scannerOrientation)
            array.pushMap(map)
        }
        promise.resolve(array)
    }
}
