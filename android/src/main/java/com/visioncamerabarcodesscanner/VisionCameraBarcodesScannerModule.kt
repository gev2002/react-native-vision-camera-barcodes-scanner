package com.visioncamerabarcodesscanner

import android.media.Image
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin
import com.mrousavy.camera.frameprocessors.VisionCameraProxy

class VisionCameraBarcodesScannerModule (proxy : VisionCameraProxy, options: Map<String, Any>?): FrameProcessorPlugin() {
    override fun callback(frame: Frame, arguments: Map<String, Any>?): Any {
    try {
        val mediaImage: Image = frame.image
        val image = InputImage.fromMediaImage(mediaImage, frame.imageProxy.imageInfo.rotationDegrees)
        val task: Task<List<Barcode>> = labeler.process(image)
        val labels: List<Barcode> = Tasks.await(task)
        val array = WritableNativeArray()

        return array.toArrayList()
      } catch (e: Exception) {
           throw Exception("Error processing image labeler: $e")
      }
  }
}


