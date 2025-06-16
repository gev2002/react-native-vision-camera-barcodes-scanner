package com.visioncamerabarcodesscanner

import android.media.Image

import com.facebook.react.bridge.WritableNativeArray

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ALL_FORMATS

import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy

class VisionCameraBarcodesScannerModule(
    proxy: VisionCameraProxy,
    options: MutableMap<String, Any>?
) : FrameProcessorPlugin() {
    private var scanner: BarcodeScanner = BarcodeScanning.getClient()
    private val scannerBuilder: BarcodeScannerOptions.Builder = BarcodeScannerOptions.Builder()
    private val scannerBarcodeFormats = ScannerUtils.getOptionsBarcodeFormats(options)
    private val scannerRatio = ScannerUtils.getOptionsRatio(options)
    private val scannerOrientation = ScannerUtils.getOptionsOrientation(options)
    private val scannerViewSize = ScannerUtils.getOptionsViewSize(options)

    init {
        val barcodeFormats = ScannerUtils.getSafeBarcodeFormats(scannerBarcodeFormats)
        if (barcodeFormats.contains(FORMAT_ALL_FORMATS)) {
            scannerBuilder.setBarcodeFormats(FORMAT_ALL_FORMATS)
        } else {
            val firstFormat = barcodeFormats.first()
            val otherFormats = barcodeFormats.drop(1).toIntArray()
            scannerBuilder.setBarcodeFormats(firstFormat, *otherFormats)
        }

        scanner = BarcodeScanning.getClient(scannerBuilder.build())
    }

    override fun callback(frame: Frame, arguments: Map<String, Any>?): Any {
        try {
            val frameImage: Image = frame.image
            val frameImageRotationDegrees = frame.imageProxy.imageInfo.rotationDegrees

            // Loads raw camera image. width/height may need correction based on rotation.
            val image =
                InputImage.fromMediaImage(frameImage, frameImageRotationDegrees)

            // Adjusts image size for portrait rotations (90 or 270 degrees).
            val imageSizeRaw = Size(image.width, image.height)
            val imageSize = ScannerUtils.getImageSizeBasedOnRotation(imageSizeRaw, frameImageRotationDegrees)
            val viewSize = ScannerUtils.getSafeViewSize(imageSize, scannerViewSize)

            val array = WritableNativeArray()
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
            return array.toArrayList()
        } catch (e: Exception) {
            throw Exception("Error processing barcode scanner: $e ")
        }
    }
}
